package com.watchvideo.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.compression.ContentEncoding
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            followRedirects(true)
            cookieJar(object : CookieJar {
                private val store = mutableMapOf<String, List<Cookie>>()
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    store[url.host] = cookies
                }
                override fun loadForRequest(url: HttpUrl): List<Cookie> =
                    store[url.host] ?: emptyList()
            })
        }
    }
    install(HttpRedirect) {
        checkHttpMethod = false
        allowHttpsDowngrade = true
    }
    install(ContentEncoding) {
        gzip()
        deflate()
    }
}
