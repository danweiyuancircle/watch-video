package com.watchvideo

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 强制横屏全屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val m3u8Url = intent.getStringExtra("m3u8Url") ?: run { finish(); return }
        val title = intent.getStringExtra("title") ?: ""

        setContent {
            MaterialTheme {
                PlayerScreen(m3u8Url = m3u8Url, title = title)
            }
        }
    }
}

private val SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
private val SPEED_LABELS = listOf("0.5×", "0.75×", "1×", "1.25×", "1.5×", "2×")

@Composable
private fun PlayerScreen(m3u8Url: String, title: String) {
    val context = LocalContext.current
    var speedIndex by remember { mutableIntStateOf(2) } // 默认 1×
    var showSpeedMenu by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(
                MediaItem.Builder()
                    .setUri(m3u8Url)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
            )
            prepare()
            playWhenReady = true
        }
    }

    LaunchedEffect(speedIndex) {
        exoPlayer.setPlaybackSpeed(SPEEDS[speedIndex])
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 倍速按钮（右上角）
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Text(
                text = SPEED_LABELS[speedIndex],
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x88000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .clickable { showSpeedMenu = true }
            )
            DropdownMenu(
                expanded = showSpeedMenu,
                onDismissRequest = { showSpeedMenu = false }
            ) {
                SPEED_LABELS.forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            speedIndex = index
                            showSpeedMenu = false
                        }
                    )
                }
            }
        }
    }
}
