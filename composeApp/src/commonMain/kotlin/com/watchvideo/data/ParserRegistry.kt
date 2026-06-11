package com.watchvideo.data

import com.watchvideo.data.parsers.XiaobaoParser
import io.ktor.client.HttpClient

object ParserRegistry {
    private val client = createHttpClient()
    private val parsers: List<SiteParser> = listOf(
        XiaobaoParser(client)
    )

    fun all(): List<SiteParser> = parsers
    fun get(siteKey: String): SiteParser = parsers.first { it.siteKey == siteKey }
}
