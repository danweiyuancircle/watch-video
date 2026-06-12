package com.watchvideo

actual fun logD(tag: String, msg: String) {
    println("[$tag] $msg")
}
