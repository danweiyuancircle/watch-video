package com.watchvideo

import android.content.Context
import android.content.Intent

lateinit var appContext: Context

actual fun openPlayer(m3u8Url: String, title: String) {
    val intent = Intent(appContext, PlayerActivity::class.java).apply {
        putExtra("m3u8Url", m3u8Url)
        putExtra("title", title)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent)
}
