package eu.mihaibadea.requestlab.feature.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryEntry
import eu.mihaibadea.requestlab.feature.history.domain.usecase.ClearHistoryUseCase
import eu.mihaibadea.requestlab.feature.history.domain.usecase.DeleteHistoryEntryUseCase
import eu.mihaibadea.requestlab.feature.history.domain.usecase.ObserveHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoryListUiState {
    data object Loading : HistoryListUiState
    data object Empty : HistoryListUiState
    data class Content(
        val entries: List<HistoryEntry>,
        val showClearConfirm: Boolean = false,
    ) : HistoryListUiState
}

sealed interface HistoryListEvent {
    data class OnEntryClicked(val id: String) : HistoryListEvent
    data class OnDeleteEntry(val id: String) : HistoryListEvent
    data object OnRequestClearAll : HistoryListEvent
    data object OnConfirmClearAll : HistoryListEvent
    data object OnDismissClearConfirm : HistoryListEvent
}

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    private val observeHistory: ObserveHistoryUseCase,
    private val deleteEntry: DeleteHistoryEntryUseCase,
    private val clearHistory: ClearHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryListUiState>(HistoryListUiState.Loading)
    val uiState: StateFlow<HistoryListUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
            .onEach { entries ->
                _uiState.update {
                    if (entries.isEmpty()) {
                        HistoryListUiState.Empty
                    } else {
                        val showConfirm = (it as? HistoryListUiState.Content)?.showClearConfirm ?: false
                        HistoryListUiState.Content(entries, showConfirm)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HistoryListEvent) {
        when (event) {
            is HistoryListEvent.OnEntryClicked -> Unit
            is HistoryListEvent.OnDeleteEntry -> viewModelScope.launch { deleteEntry(event.id) }
            HistoryListEvent.OnRequestClearAll -> _uiState.update { state ->
                (state as? HistoryListUiState.Content)?.copy(showClearConfirm = true) ?: state
            }
            HistoryListEvent.OnConfirmClearAll -> {
                _uiState.update { state ->
                    (state as? HistoryListUiState.Content)?.copy(showClearConfirm = false) ?: state
                }
                viewModelScope.launch { clearHistory() }
            }
            HistoryListEvent.OnDismissClearConfirm -> _uiState.update { state ->
                (state as? HistoryListUiState.Content)?.copy(showClearConfirm = false) ?: state
            }
        }
    }
}
