package com.watchvideo.ui.detail

import com.watchvideo.data.ParserRegistry
import com.watchvideo.data.model.PlayInfo
import com.watchvideo.data.model.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _playInfo = MutableStateFlow<PlayInfo?>(null)
    val playInfo: StateFlow<PlayInfo?> = _playInfo.asStateFlow()

    private val _isLoadingPlay = MutableStateFlow(false)
    val isLoadingPlay: StateFlow<Boolean> = _isLoadingPlay.asStateFlow()

    fun loadDetail(siteKey: String, id: String) {
        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val parser = ParserRegistry.get(siteKey)
                _routes.value = parser.detail(id)
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPlayInfo(siteKey: String, playPageUrl: String) {
        scope.launch {
            _isLoadingPlay.value = true
            _error.value = null
            try {
                val parser = ParserRegistry.get(siteKey)
                _playInfo.value = parser.playInfo(playPageUrl)
            } catch (e: Exception) {
                _error.value = "获取播放地址失败: ${e.message}"
            } finally {
                _isLoadingPlay.value = false
            }
        }
    }
}
