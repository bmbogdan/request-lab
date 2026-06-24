package eu.mihaibadea.requestlab.feature.docs.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.docs.domain.model.Article
import eu.mihaibadea.requestlab.feature.docs.domain.usecase.GetArticleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DocsArticleUiState {
    data object Loading : DocsArticleUiState
    data class Content(val article: Article) : DocsArticleUiState
    data class Error(val message: String) : DocsArticleUiState
}

@HiltViewModel
class DocsArticleViewModel @Inject constructor(
    private val getArticle: GetArticleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DocsArticleUiState>(DocsArticleUiState.Loading)
    val uiState: StateFlow<DocsArticleUiState> = _uiState.asStateFlow()

    private var loadedId: String = ""

    fun init(id: String) {
        if (loadedId == id) return
        loadedId = id
        _uiState.update { DocsArticleUiState.Loading }
        viewModelScope.launch {
            when (val result = getArticle(id)) {
                is AppResult.Success -> _uiState.update { DocsArticleUiState.Content(result.value) }
                is AppResult.Failure -> _uiState.update { DocsArticleUiState.Error("Couldn't load article.") }
            }
        }
    }
}
