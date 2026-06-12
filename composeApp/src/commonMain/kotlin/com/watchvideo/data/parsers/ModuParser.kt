package com.watchvideo.data.parsers

import com.watchvideo.data.SiteParser
import com.watchvideo.data.model.Episode
import com.watchvideo.data.model.PlayInfo
import com.watchvideo.data.model.Route
import com.watchvideo.data.model.SearchResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ModuParser(private val client: HttpClient) : SiteParser {
    override val siteKey = "modu"
    override val siteName = "模板影视"
    override val baseUrl = "https://caiji.moduapi.cc"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun search(keyword: String): List<SearchResult> {
        val body = fetch("$baseUrl/api.php/provide/vod/?ac=list&wd=${keyword.encodeURLParameter()}")
        val root = json.parseToJsonElement(body).jsonObject
        val list = root["list"]?.jsonArray ?: return emptyList()
        return list.map { el ->
            val obj = el.jsonObject
            val id = obj["vod_id"]?.jsonPrimitive?.int?.toString() ?: return@map null
            val title = obj["vod_name"]?.jsonPrimitive?.content ?: ""
            val cover = obj["vod_pic"]?.jsonPrimitive?.content ?: ""
            SearchResult(id = id, title = title, cover = cover, siteKey = siteKey)
        }.filterNotNull()
    }

    override suspend fun detail(id: String): List<Route> {
        val body = fetch("$baseUrl/api.php/provide/vod/?ac=detail&ids=$id")
        val root = json.parseToJsonElement(body).jsonObject
        val vod = root["list"]?.jsonArray?.firstOrNull()?.jsonObject ?: return emptyList()

        val fromNames = vod["vod_play_from"]?.jsonPrimitive?.content?.split("\$\$\$") ?: emptyList()
        val playUrlBlock = vod["vod_play_url"]?.jsonPrimitive?.content ?: return emptyList()
        val routes = playUrlBlock.split("\$\$\$")

        return routes.mapIndexed { index, block ->
            val episodes = block.split("#").mapNotNull { ep ->
                val parts = ep.split("\$")
                if (parts.size < 2) return@mapNotNull null
                Episode(name = parts[0].trim(), playUrl = parts[1].trim())
            }
            Route(name = fromNames.getOrElse(index) { "线路${index + 1}" }, episodes = episodes)
        }.filter { it.episodes.isNotEmpty() }
    }

    // 详情接口已直接返回 m3u8，playInfo 从 detail 结果取，此接口不需要再请求
    override suspend fun playInfo(playPageUrl: String): PlayInfo {
        // playPageUrl 在此 parser 就是 m3u8 直链
        return PlayInfo(m3u8Url = playPageUrl, title = "")
    }

    private suspend fun fetch(url: String): String {
        return client.get(url) {
            headers {
                append("User-Agent", "okhttp/4.9.0")
            }
        }.bodyAsText()
    }
}
