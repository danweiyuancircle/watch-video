package com.watchvideo.data

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
