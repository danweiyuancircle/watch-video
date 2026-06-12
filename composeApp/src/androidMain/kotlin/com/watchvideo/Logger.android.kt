package com.watchvideo

import android.util.Log
import com.watchvideo.BuildConfig

actual fun logD(tag: String, msg: String) {
    if (BuildConfig.DEBUG) Log.e(tag, msg)
}
