package eu.mihaibadea.requestlab.feature.environments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.feature.environments.domain.model.Environment
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.CreateEnvironmentUseCase
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.DeleteEnvironmentUseCase
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.ObserveEnvironmentsUseCase
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.RenameEnvironmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EnvironmentsUiState {
    data object Loading : EnvironmentsUiState
    data object Empty : EnvironmentsUiState
    data class Content(val rows: List<Environment>, val activeId: String?) : EnvironmentsUiState
    data class Error(val message: String) : EnvironmentsUiState
}

sealed interface EnvironmentsEvent {
    data class OnEnvironmentClicked(val id: String) : EnvironmentsEvent
    data class OnNewEnvironment(val name: String) : EnvironmentsEvent
    data class OnRename(val id: String, val name: String) : EnvironmentsEvent
    data class OnDelete(val id: String) : EnvironmentsEvent
    data object OnRetry : EnvironmentsEvent
}

@HiltViewModel
class EnvironmentsViewModel @Inject constructor(
    observeEnvironments: ObserveEnvironmentsUseCase,
    private val createEnvironment: CreateEnvironmentUseCase,
    private val renameEnvironment: RenameEnvironmentUseCase,
    private val deleteEnvironment: DeleteEnvironmentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EnvironmentsUiState>(EnvironmentsUiState.Loading)
    val uiState: StateFlow<EnvironmentsUiState> = _uiState.asStateFlow()

    init {
        loadEnvironments(observeEnvironments)
    }

    private fun loadEnvironments(observeEnvironments: ObserveEnvironmentsUseCase) {
        observeEnvironments()
            .onEach { envs ->
                _uiState.value = if (envs.isEmpty()) {
                    EnvironmentsUiState.Empty
                } else {
                    val activeId = envs.firstOrNull { it.isActive }?.id
                    EnvironmentsUiState.Content(rows = envs, activeId = activeId)
                }
            }
            .catch { _uiState.value = EnvironmentsUiState.Error("Couldn't load environments.") }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: EnvironmentsEvent) {
        when (event) {
            is EnvironmentsEvent.OnNewEnvironment -> viewModelScope.launch {
                createEnvironment(event.name)
            }
            is EnvironmentsEvent.OnRename -> viewModelScope.launch {
                renameEnvironment(event.id, event.name)
            }
            is EnvironmentsEvent.OnDelete -> viewModelScope.launch {
                deleteEnvironment(event.id)
            }
            else -> Unit
        }
    }
}
