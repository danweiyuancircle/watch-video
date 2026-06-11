package com.watchvideo.data

import com.watchvideo.data.model.PlayInfo
import com.watchvideo.data.model.Route
import com.watchvideo.data.model.SearchResult

interface SiteParser {
    val siteKey: String
    val siteName: String
    val baseUrl: String

    suspend fun search(keyword: String): List<SearchResult>
    suspend fun detail(id: String): List<Route>
    suspend fun playInfo(playPageUrl: String): PlayInfo
}
