package com.watchvideo.data.model

data class SearchResult(
    val id: String,
    val title: String,
    val cover: String,   // 绝对 URL
    val siteKey: String
)

data class Episode(
    val name: String,    // "第01集"
    val playUrl: String  // 站点播放页 URL（完整 URL）
)

data class Route(
    val name: String,    // "小宝影院" / "线路1"
    val episodes: List<Episode>
)

data class PlayInfo(
    val m3u8Url: String,
    val title: String
)
