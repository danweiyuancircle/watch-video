package com.watchvideo.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.watchvideo.data.model.Episode
import com.watchvideo.data.model.PlayInfo
import com.watchvideo.data.model.Route

@Composable
fun DetailScreen(
    siteKey: String,
    vodId: String,
    title: String,
    viewModel: DetailViewModel = remember { DetailViewModel() },
    onPlay: (PlayInfo) -> Unit
) {
    LaunchedEffect(siteKey, vodId) {
        viewModel.loadDetail(siteKey, vodId)
    }

    val routes by viewModel.routes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playInfo by viewModel.playInfo.collectAsState()
    val isLoadingPlay by viewModel.isLoadingPlay.collectAsState()

    LaunchedEffect(playInfo) {
        playInfo?.let { onPlay(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Text(text = error!!, color = MaterialTheme.colorScheme.error)
            routes.isNotEmpty() -> RouteTabs(
                routes = routes,
                isLoadingPlay = isLoadingPlay,
                onEpisodeClick = { episode ->
                    viewModel.loadPlayInfo(siteKey, episode.playUrl)
                }
            )
        }
    }
}

@Composable
private fun RouteTabs(
    routes: List<Route>,
    isLoadingPlay: Boolean,
    onEpisodeClick: (Episode) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    TabRow(selectedTabIndex = selectedTab) {
        routes.forEachIndexed { index, route ->
            Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(route.name) }
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (isLoadingPlay) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    val currentEpisodes = routes.getOrNull(selectedTab)?.episodes ?: emptyList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(currentEpisodes) { episode ->
            OutlinedButton(
                onClick = { onEpisodeClick(episode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = episode.name, maxLines = 1)
            }
        }
    }
}
