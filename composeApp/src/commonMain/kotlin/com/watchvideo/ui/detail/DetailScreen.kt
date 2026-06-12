package com.watchvideo.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailScreen(
    siteKey: String,
    vodId: String,
    title: String,
    viewModel: DetailViewModel = remember { DetailViewModel() },
    onBack: () -> Unit
) {
    LaunchedEffect(siteKey, vodId) {
        viewModel.loadDetail(siteKey, vodId)
    }

    val routes by viewModel.routes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playInfo by viewModel.playInfo.collectAsState()
    val currentRouteIndex by viewModel.currentRouteIndex.collectAsState()
    val currentEpisodeIndex by viewModel.currentEpisodeIndex.collectAsState()
    var isFullscreen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentRoute = routes.getOrNull(selectedTab)
    val episodeCount = currentRoute?.episodes?.size ?: 0

    val onPrev: (() -> Unit)? = if (currentEpisodeIndex > 0 && currentRouteIndex == selectedTab) {
        {
            val idx = currentEpisodeIndex - 1
            val ep = currentRoute?.episodes?.getOrNull(idx)
            if (ep != null) viewModel.selectEpisode(siteKey, selectedTab, idx, ep.playUrl)
        }
    } else null

    val onNext: (() -> Unit)? = if (currentEpisodeIndex >= 0 && currentEpisodeIndex < episodeCount - 1 && currentRouteIndex == selectedTab) {
        {
            val idx = currentEpisodeIndex + 1
            val ep = currentRoute?.episodes?.getOrNull(idx)
            if (ep != null) viewModel.selectEpisode(siteKey, selectedTab, idx, ep.playUrl)
        }
    } else null

    // 单一 Column，VideoPlayerArea 始终存在于同一 composition slot，避免全屏切换时销毁重建播放器
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        VideoPlayerArea(
            m3u8Url = playInfo?.m3u8Url,
            title = title,
            isFullscreen = isFullscreen,
            onFullscreenToggle = { isFullscreen = !isFullscreen },
            onBack = onBack,
            onPrevEpisode = onPrev,
            onNextEpisode = onNext,
            modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
        )

        // 全屏时隐藏集数区域
        if (!isFullscreen) {
            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { Text(text = error!!, color = MaterialTheme.colorScheme.error) }

                routes.isNotEmpty() -> Column(modifier = Modifier.weight(1f)) {
                    if (routes.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 8.dp
                        ) {
                            routes.forEachIndexed { index, route ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(route.name) }
                                )
                            }
                        }
                    }

                    val episodes = routes.getOrNull(selectedTab)?.episodes ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(episodes) { index, episode ->
                            val isSelected = selectedTab == currentRouteIndex && index == currentEpisodeIndex
                            OutlinedButton(
                                onClick = {
                                    viewModel.selectEpisode(siteKey, selectedTab, index, episode.playUrl)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) else ButtonDefaults.outlinedButtonColors(),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text(
                                    text = episode.name,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
expect fun VideoPlayerArea(
    m3u8Url: String?,
    title: String,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    onBack: () -> Unit,
    onPrevEpisode: (() -> Unit)?,
    onNextEpisode: (() -> Unit)?,
    modifier: Modifier = Modifier
)
