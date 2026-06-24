package com.example.requestlab.feature.collections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.builder.domain.model.DraftSource
import com.example.requestlab.feature.builder.domain.usecase.LoadDraftUseCase
import com.example.requestlab.feature.builder.domain.usecase.UpdateDraftUseCase
import com.example.requestlab.feature.collections.domain.model.Collection
import com.example.requestlab.feature.collections.domain.model.SavedRequest
import com.example.requestlab.feature.collections.domain.usecase.DeleteCollectionUseCase
import com.example.requestlab.feature.collections.domain.usecase.ObserveCollectionRequestsUseCase
import com.example.requestlab.feature.collections.domain.usecase.ObserveCollectionsUseCase
import com.example.requestlab.feature.collections.domain.usecase.RemoveSavedRequestUseCase
import com.example.requestlab.feature.collections.domain.usecase.RenameCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CollectionDetailUiState {
    data object Loading : CollectionDetailUiState
    data class Content(
        val collection: Collection?,
        val requests: List<SavedRequest>,
        val dialog: CollectionDetailDialog? = null,
    ) : CollectionDetailUiState
    data class Error(val message: String) : CollectionDetailUiState
}

sealed interface CollectionDetailDialog {
    data class Rename(val nameInput: String) : CollectionDetailDialog
    data object DeleteCollectionConfirm : CollectionDetailDialog
}

sealed interface CollectionDetailEvent {
    data class OnOpenRequest(val id: String) : CollectionDetailEvent
    data class OnDeleteRequest(val id: String) : CollectionDetailEvent
    data object OnRequestRename : CollectionDetailEvent
    data class OnRenameNameChanged(val name: String) : CollectionDetailEvent
    data object OnConfirmRename : CollectionDetailEvent
    data object OnRequestDeleteCollection : CollectionDetailEvent
    data object OnConfirmDeleteCollection : CollectionDetailEvent
    data object OnDismissDialog : CollectionDetailEvent
    data object OnBack : CollectionDetailEvent
}

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val observeCollections: ObserveCollectionsUseCase,
    private val observeRequests: ObserveCollectionRequestsUseCase,
    private val renameCollection: RenameCollectionUseCase,
    private val deleteCollection: DeleteCollectionUseCase,
    private val removeRequest: RemoveSavedRequestUseCase,
    private val loadDraft: LoadDraftUseCase,
    private val updateDraft: UpdateDraftUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionDetailUiState>(CollectionDetailUiState.Loading)
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()

    private val _navigateToBuilder = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToBuilder = _navigateToBuilder.asSharedFlow()

    private val _navigateBack = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateBack = _navigateBack.asSharedFlow()

    private var collectionId: String = ""

    fun init(id: String) {
        if (collectionId == id) return
        collectionId = id
        combine(observeCollections(), observeRequests(id)) { collections, requests ->
            val collection = collections.firstOrNull { it.id == id }
            val dialog = (_uiState.value as? CollectionDetailUiState.Content)?.dialog
            CollectionDetailUiState.Content(collection, requests, dialog)
        }
            .onEach { state -> _uiState.update { state } }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CollectionDetailEvent) {
        when (event) {
            is CollectionDetailEvent.OnOpenRequest -> openInBuilder(event.id)
            is CollectionDetailEvent.OnDeleteRequest -> viewModelScope.launch { removeRequest(event.id) }
            CollectionDetailEvent.OnRequestRename -> {
                val name = currentContent()?.collection?.name ?: return
                setDialog(CollectionDetailDialog.Rename(name))
            }
            is CollectionDetailEvent.OnRenameNameChanged -> _uiState.update { state ->
                val content = state as? CollectionDetailUiState.Content ?: return
                val dialog = content.dialog as? CollectionDetailDialog.Rename ?: return
                content.copy(dialog = dialog.copy(nameInput = event.name))
            }
            CollectionDetailEvent.OnConfirmRename -> {
                val dialog = (currentContent()?.dialog as? CollectionDetailDialog.Rename) ?: return
                if (dialog.nameInput.isBlank()) return
                dismissDialog()
                viewModelScope.launch { renameCollection(collectionId, dialog.nameInput.trim()) }
            }
            CollectionDetailEvent.OnRequestDeleteCollection -> setDialog(CollectionDetailDialog.DeleteCollectionConfirm)
            CollectionDetailEvent.OnConfirmDeleteCollection -> {
                dismissDialog()
                viewModelScope.launch {
                    deleteCollection(collectionId)
                    _navigateBack.tryEmit(Unit)
                }
            }
            CollectionDetailEvent.OnDismissDialog -> dismissDialog()
            CollectionDetailEvent.OnBack -> Unit
        }
    }

    private fun openInBuilder(savedRequestId: String) {
        viewModelScope.launch {
            val result = loadDraft(DraftSource.FromSavedRequest(savedRequestId))
            if (result is AppResult.Success) {
                updateDraft(result.value)
                _navigateToBuilder.tryEmit(Unit)
            }
        }
    }

    private fun currentContent() = uiState.value as? CollectionDetailUiState.Content

    private fun setDialog(dialog: CollectionDetailDialog) {
        _uiState.update { state ->
            (state as? CollectionDetailUiState.Content)?.copy(dialog = dialog) ?: state
        }
    }

    private fun dismissDialog() {
        _uiState.update { state ->
            (state as? CollectionDetailUiState.Content)?.copy(dialog = null) ?: state
        }
    }
}
