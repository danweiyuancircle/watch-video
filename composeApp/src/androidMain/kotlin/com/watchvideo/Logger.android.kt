package com.watchvideo

import android.util.Log

actual fun logD(tag: String, msg: String) {
    Log.e(tag, msg)  // 用 Error 级别，小米不会过滤
}
