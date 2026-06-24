package com.example.requestlab.feature.docs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.docs.domain.model.ArticleSummary
import com.example.requestlab.feature.docs.domain.model.DocCategory
import com.example.requestlab.feature.docs.domain.usecase.GetArticleIndexUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocsIndexUiState(
    val isLoading: Boolean = true,
    val grouped: Map<DocCategory, List<ArticleSummary>> = emptyMap(),
    val query: String = "",
    val error: String? = null,
) {
    val filtered: Map<DocCategory, List<ArticleSummary>>
        get() = if (query.isBlank()) grouped else {
            grouped.mapValues { (category, articles) ->
                articles.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    category.name.contains(query, ignoreCase = true)
                }
            }.filterValues { it.isNotEmpty() }
        }
}

sealed interface DocsIndexEvent {
    data class OnQueryChanged(val query: String) : DocsIndexEvent
    data object OnRetry : DocsIndexEvent
}

@HiltViewModel
class DocsIndexViewModel @Inject constructor(
    private val getIndex: GetArticleIndexUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocsIndexUiState())
    val uiState: StateFlow<DocsIndexUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: DocsIndexEvent) {
        when (event) {
            is DocsIndexEvent.OnQueryChanged -> _uiState.update { it.copy(query = event.query) }
            DocsIndexEvent.OnRetry -> load()
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = getIndex()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        grouped = result.value
                            .groupBy { article -> article.category }
                            .toSortedMap(compareBy { category -> category.ordinal }),
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = "Couldn't load articles.")
                }
            }
        }
    }
}
