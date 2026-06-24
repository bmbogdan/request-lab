package eu.mihaibadea.requestlab.feature.builder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.builder.domain.RequestDraftRepository
import eu.mihaibadea.requestlab.feature.builder.domain.usecase.SaveRequestToCollectionUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.model.Collection
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.ObserveCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SaveSheetUiState {
    data object Loading : SaveSheetUiState
    data class Empty(val requestName: String) : SaveSheetUiState
    data class Content(
        val collections: List<Collection>,
        val selectedId: String?,
        val newName: String,
        val requestName: String,
        val saving: Boolean,
        val error: String?,
    ) : SaveSheetUiState
}

sealed interface SaveSheetEvent {
    data class OnSelectCollection(val id: String?) : SaveSheetEvent
    data class OnNewCollectionNameChanged(val name: String) : SaveSheetEvent
    data class OnRequestNameChanged(val name: String) : SaveSheetEvent
    data object OnSaveConfirmed : SaveSheetEvent
    data object OnDismiss : SaveSheetEvent
}

@HiltViewModel
class SaveToCollectionViewModel @Inject constructor(
    private val draftRepository: RequestDraftRepository,
    private val observeCollections: ObserveCollectionsUseCase,
    private val saveToCollection: SaveRequestToCollectionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SaveSheetUiState>(SaveSheetUiState.Loading)
    val uiState: StateFlow<SaveSheetUiState> = _uiState.asStateFlow()

    private val _dismissSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dismissSignal = _dismissSignal.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                draftRepository.observeWorkingDraft(),
                observeCollections(),
            ) { draft, collections ->
                draft to collections
            }.collect { (draft, collections) ->
                val requestName = draft?.let { "${it.method.name} ${it.url}" } ?: ""
                val current = _uiState.value
                val selectedId = (current as? SaveSheetUiState.Content)?.selectedId
                val newName = (current as? SaveSheetUiState.Content)?.newName ?: ""
                val saving = (current as? SaveSheetUiState.Content)?.saving ?: false
                _uiState.update {
                    if (collections.isEmpty()) {
                        SaveSheetUiState.Empty(requestName)
                    } else {
                        SaveSheetUiState.Content(
                            collections = collections,
                            selectedId = selectedId,
                            newName = newName,
                            requestName = requestName,
                            saving = saving,
                            error = null,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: SaveSheetEvent) {
        when (event) {
            is SaveSheetEvent.OnSelectCollection -> _uiState.update { state ->
                when (state) {
                    is SaveSheetUiState.Content -> state.copy(selectedId = event.id, newName = "")
                    else -> state
                }
            }
            is SaveSheetEvent.OnNewCollectionNameChanged -> _uiState.update { state ->
                when (state) {
                    is SaveSheetUiState.Content -> state.copy(newName = event.name, selectedId = null)
                    else -> state
                }
            }
            is SaveSheetEvent.OnRequestNameChanged -> _uiState.update { state ->
                when (state) {
                    is SaveSheetUiState.Content -> state.copy(requestName = event.name)
                    is SaveSheetUiState.Empty -> state.copy(requestName = event.name)
                    else -> state
                }
            }
            SaveSheetEvent.OnSaveConfirmed -> doSave()
            SaveSheetEvent.OnDismiss -> _dismissSignal.tryEmit(Unit)
        }
    }

    private fun doSave() {
        viewModelScope.launch {
            val draft = draftRepository.observeWorkingDraft().first() ?: return@launch

            val state = _uiState.value
            val collectionId: String?
            val newCollectionName: String?
            val requestName: String
            when (state) {
                is SaveSheetUiState.Content -> {
                    collectionId = state.selectedId
                    newCollectionName = state.newName.ifBlank { null }
                    requestName = state.requestName
                }
                is SaveSheetUiState.Empty -> {
                    collectionId = null
                    newCollectionName = null
                    requestName = state.requestName
                }
                else -> return@launch
            }

            if (state is SaveSheetUiState.Content) {
                _uiState.update { (it as SaveSheetUiState.Content).copy(saving = true, error = null) }
            }

            val result = saveToCollection(draft, collectionId, newCollectionName, requestName)
            when (result) {
                is AppResult.Success -> _dismissSignal.tryEmit(Unit)
                is AppResult.Failure -> {
                    if (state is SaveSheetUiState.Content) {
                        _uiState.update {
                            (it as SaveSheetUiState.Content).copy(saving = false, error = "Couldn't save. Try again.")
                        }
                    }
                }
            }
        }
    }
}
