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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class XiaobaoParser(private val client: HttpClient) : SiteParser {
    override val siteKey = "xiaobao"
    override val siteName = "小宝影院"
    override val baseUrl = "https://www.xiaobaotv.com"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun search(keyword: String): List<SearchResult> {
        val html = fetch("$baseUrl/search.html?wd=${keyword.encodeURLParameter()}&submit=")
        return parseSearchResults(html)
    }

    override suspend fun detail(id: String): List<Route> {
        val html = fetch("$baseUrl/vod/detail/$id.html")
        return parseRoutes(html)
    }

    override suspend fun playInfo(playPageUrl: String): PlayInfo {
        val html = fetch(playPageUrl)
        return parsePlayInfo(html)
    }

    private suspend fun fetch(url: String): String {
        return client.get(url) {
            headers {
                append("User-Agent", "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                append("Referer", baseUrl)
            }
        }.bodyAsText()
    }

    private fun parseSearchResults(html: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val linkPattern = Regex("""href="/vod/detail/(\d+)\.html"[^>]+title="([^"]+)"""")
        val imgPattern = Regex("""data-original="([^"]+)"""")

        val matches = linkPattern.findAll(html).toList()
        val imgs = imgPattern.findAll(html).toList()

        matches.forEachIndexed { index, match ->
            val id = match.groupValues[1]
            val title = match.groupValues[2]
            val cover = imgs.getOrNull(index)?.groupValues?.get(1)?.let { path ->
                if (path.startsWith("http")) path else "$baseUrl$path"
            } ?: ""
            results.add(SearchResult(id = id, title = title, cover = cover, siteKey = siteKey))
        }
        return results
    }

    private fun parseRoutes(html: String): List<Route> {
        val tabPattern = Regex("""data-tab="([^"]+)"""")
        val tabNames = tabPattern.findAll(html).map { it.groupValues[1] }.toList()

        val ulPattern = Regex("""<ul[^>]*myui-content__list[^>]*>(.*?)</ul>""", RegexOption.DOT_MATCHES_ALL)
        val episodePattern = Regex("""href="(/vod/play/[^"]+)"[^>]*>([^<]+)</a>""")

        return ulPattern.findAll(html).mapIndexed { index, ulMatch ->
            val blockHtml = ulMatch.groupValues[1]
            val episodes = episodePattern.findAll(blockHtml).map { ep ->
                Episode(
                    name = ep.groupValues[2].trim(),
                    playUrl = "$baseUrl${ep.groupValues[1]}"
                )
            }.toList()
            Route(
                name = tabNames.getOrElse(index) { "线路${index + 1}" },
                episodes = episodes
            )
        }.toList()
    }

    private fun parsePlayInfo(html: String): PlayInfo {
        val playerPattern = Regex("""var player_aaaa\s*=\s*(\{.*?\})\s*</script>""", RegexOption.DOT_MATCHES_ALL)
        val match = playerPattern.find(html)
            ?: throw IllegalStateException("未找到 player_aaaa 配置")

        val playerJson = json.parseToJsonElement(match.groupValues[1]).jsonObject
        val encrypt = playerJson["encrypt"]?.jsonPrimitive?.int ?: 0
        val rawUrl = playerJson["url"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("未找到播放地址")

        val m3u8Url = if (encrypt == 1) {
            decodeBase64(rawUrl)
        } else {
            rawUrl
        }

        val vodData = playerJson["vod_data"]?.jsonObject
        val title = vodData?.get("vod_name")?.jsonPrimitive?.content ?: ""

        return PlayInfo(m3u8Url = m3u8Url, title = title)
    }

    private fun decodeBase64(encoded: String): String {
        return try {
            @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
            String(kotlin.io.encoding.Base64.decode(encoded))
        } catch (e: Exception) {
            encoded
        }
    }
}
