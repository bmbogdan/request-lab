package eu.mihaibadea.requestlab.feature.collections.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.feature.collections.domain.model.Collection
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.CreateCollectionUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.DeleteCollectionUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.ObserveCollectionsUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.RenameCollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CollectionsListUiState {
    data object Loading : CollectionsListUiState
    data object Empty : CollectionsListUiState
    data class Content(
        val collections: List<Collection>,
        val dialog: CollectionsDialog? = null,
    ) : CollectionsListUiState
}

sealed interface CollectionsDialog {
    data class NewCollection(val nameInput: String = "") : CollectionsDialog
    data class Rename(val id: String, val currentName: String, val nameInput: String = "") : CollectionsDialog
    data class DeleteConfirm(val id: String, val name: String) : CollectionsDialog
}

sealed interface CollectionsListEvent {
    data class OnCollectionClicked(val id: String) : CollectionsListEvent
    data object OnRequestNewCollection : CollectionsListEvent
    data class OnNewCollectionNameChanged(val name: String) : CollectionsListEvent
    data object OnConfirmNewCollection : CollectionsListEvent
    data class OnRequestRename(val id: String, val currentName: String) : CollectionsListEvent
    data class OnRenameNameChanged(val name: String) : CollectionsListEvent
    data object OnConfirmRename : CollectionsListEvent
    data class OnRequestDelete(val id: String, val name: String) : CollectionsListEvent
    data object OnConfirmDelete : CollectionsListEvent
    data object OnDismissDialog : CollectionsListEvent
}

@HiltViewModel
class CollectionsListViewModel @Inject constructor(
    private val observeCollections: ObserveCollectionsUseCase,
    private val createCollection: CreateCollectionUseCase,
    private val renameCollection: RenameCollectionUseCase,
    private val deleteCollection: DeleteCollectionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionsListUiState>(CollectionsListUiState.Loading)
    val uiState: StateFlow<CollectionsListUiState> = _uiState.asStateFlow()

    init {
        observeCollections()
            .onEach { collections ->
                _uiState.update { current ->
                    val dialog = (current as? CollectionsListUiState.Content)?.dialog
                    if (collections.isEmpty()) {
                        CollectionsListUiState.Empty
                    } else {
                        CollectionsListUiState.Content(collections, dialog)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CollectionsListEvent) {
        when (event) {
            is CollectionsListEvent.OnCollectionClicked -> Unit
            CollectionsListEvent.OnRequestNewCollection -> setDialog(CollectionsDialog.NewCollection())
            is CollectionsListEvent.OnNewCollectionNameChanged -> _uiState.update { state ->
                val content = state as? CollectionsListUiState.Content ?: return
                val dialog = content.dialog as? CollectionsDialog.NewCollection ?: return
                content.copy(dialog = dialog.copy(nameInput = event.name))
            }
            CollectionsListEvent.OnConfirmNewCollection -> {
                val dialog = currentDialog<CollectionsDialog.NewCollection>() ?: return
                if (dialog.nameInput.isBlank()) return
                dismissDialog()
                viewModelScope.launch { createCollection(dialog.nameInput.trim()) }
            }
            is CollectionsListEvent.OnRequestRename -> setDialog(
                CollectionsDialog.Rename(event.id, event.currentName, event.currentName),
            )
            is CollectionsListEvent.OnRenameNameChanged -> _uiState.update { state ->
                val content = state as? CollectionsListUiState.Content ?: return
                val dialog = content.dialog as? CollectionsDialog.Rename ?: return
                content.copy(dialog = dialog.copy(nameInput = event.name))
            }
            CollectionsListEvent.OnConfirmRename -> {
                val dialog = currentDialog<CollectionsDialog.Rename>() ?: return
                if (dialog.nameInput.isBlank()) return
                dismissDialog()
                viewModelScope.launch { renameCollection(dialog.id, dialog.nameInput.trim()) }
            }
            is CollectionsListEvent.OnRequestDelete -> setDialog(
                CollectionsDialog.DeleteConfirm(event.id, event.name),
            )
            CollectionsListEvent.OnConfirmDelete -> {
                val dialog = currentDialog<CollectionsDialog.DeleteConfirm>() ?: return
                dismissDialog()
                viewModelScope.launch { deleteCollection(dialog.id) }
            }
            CollectionsListEvent.OnDismissDialog -> dismissDialog()
        }
    }

    private fun setDialog(dialog: CollectionsDialog) {
        _uiState.update { state ->
            when (state) {
                is CollectionsListUiState.Content -> state.copy(dialog = dialog)
                CollectionsListUiState.Empty -> CollectionsListUiState.Content(emptyList(), dialog)
                else -> state
            }
        }
    }

    private fun dismissDialog() {
        _uiState.update { state ->
            (state as? CollectionsListUiState.Content)?.copy(dialog = null) ?: state
        }
    }

    private inline fun <reified T : CollectionsDialog> currentDialog(): T? {
        return ((uiState.value as? CollectionsListUiState.Content)?.dialog as? T)
    }
}
