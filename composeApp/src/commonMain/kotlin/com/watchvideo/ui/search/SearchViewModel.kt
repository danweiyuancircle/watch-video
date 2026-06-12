package com.watchvideo.ui.search

import com.watchvideo.data.ParserRegistry
import com.watchvideo.data.model.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel {
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchResult>>(emptyList())
    val results: StateFlow<List<SearchResult>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    fun onQueryChange(query: String) {
        com.watchvideo.logD("WV", "onQueryChange: '$query'")
        _query.value = query
    }

    fun removeHistory(keyword: String) {
        _history.value = _history.value.filter { it != keyword }
    }

    fun search() {
        val keyword = _query.value.trim()
        if (keyword.isEmpty()) return

        // 更新历史（去重，新的排最前，最多保留 20 条）
        _history.value = (listOf(keyword) + _history.value.filter { it != keyword }).take(20)

        scope.launch {
            _isLoading.value = true
            _error.value = null
            _results.value = emptyList()

            val errors = mutableListOf<String>()
            val parsers = try {
                ParserRegistry.all()
            } catch (e: Exception) {
                _error.value = "初始化失败(${e::class.simpleName}): ${e.message}"
                _isLoading.value = false
                return@launch
            }

            val allResults = parsers
                .map { parser ->
                    async {
                        try {
                            parser.search(keyword)
                        } catch (e: Exception) {
                            val msg = "${parser.siteName}失败\n类型:${e::class.simpleName}\n原因:${e.message}"
                            errors.add(msg)
                            emptyList()
                        }
                    }
                }
                .awaitAll()
                .flatten()

            _results.value = allResults
            if (allResults.isEmpty()) {
                _error.value = if (errors.isNotEmpty()) errors.joinToString("\n\n")
                               else "未找到结果(parsers=${parsers.size}, kw=$keyword)"
            }
            _isLoading.value = false
        }
    }
}
