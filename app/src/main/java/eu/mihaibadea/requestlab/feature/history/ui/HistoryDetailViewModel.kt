package eu.mihaibadea.requestlab.feature.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.builder.domain.model.DraftSource
import eu.mihaibadea.requestlab.feature.builder.domain.usecase.LoadDraftUseCase
import eu.mihaibadea.requestlab.feature.builder.domain.usecase.UpdateDraftUseCase
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryDetail
import eu.mihaibadea.requestlab.feature.history.domain.usecase.GetHistoryDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryDetailUiState {
    data object Loading : HistoryDetailUiState
    data class Content(val detail: HistoryDetail) : HistoryDetailUiState
    data class Error(val message: String) : HistoryDetailUiState
}

sealed interface HistoryDetailEvent {
    data object OnBack : HistoryDetailEvent
    data object OnReuseInBuilder : HistoryDetailEvent
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val getDetail: GetHistoryDetailUseCase,
    private val loadDraft: LoadDraftUseCase,
    private val updateDraft: UpdateDraftUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    private val _navigateToBuilder = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToBuilder = _navigateToBuilder.asSharedFlow()

    private var entryId: String = ""

    fun init(id: String) {
        if (entryId == id) return
        entryId = id
        viewModelScope.launch {
            _uiState.update { HistoryDetailUiState.Loading }
            when (val result = getDetail(id)) {
                is AppResult.Success -> _uiState.update { HistoryDetailUiState.Content(result.value) }
                is AppResult.Failure -> _uiState.update { HistoryDetailUiState.Error("Couldn't load entry.") }
            }
        }
    }

    fun onEvent(event: HistoryDetailEvent) {
        when (event) {
            HistoryDetailEvent.OnBack -> Unit
            HistoryDetailEvent.OnReuseInBuilder -> reuseInBuilder()
        }
    }

    private fun reuseInBuilder() {
        viewModelScope.launch {
            val draftResult = loadDraft(DraftSource.FromHistory(entryId))
            if (draftResult is AppResult.Success) {
                updateDraft(draftResult.value)
                _navigateToBuilder.tryEmit(Unit)
            }
        }
    }
}
