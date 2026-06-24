package com.example.requestlab.feature.environments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.requestlab.feature.environments.domain.model.Environment
import com.example.requestlab.feature.environments.domain.usecase.ObserveActiveEnvironmentUseCase
import com.example.requestlab.feature.environments.domain.usecase.ObserveEnvironmentsUseCase
import com.example.requestlab.feature.environments.domain.usecase.SetActiveEnvironmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SwitcherUiState(
    val environments: List<Environment> = emptyList(),
    val activeId: String? = null,
)

sealed interface SwitcherEvent {
    data class OnSelect(val id: String?) : SwitcherEvent
    data object OnManageEnvironments : SwitcherEvent
}

@HiltViewModel
class EnvironmentSwitcherViewModel @Inject constructor(
    observeEnvironments: ObserveEnvironmentsUseCase,
    observeActiveEnvironment: ObserveActiveEnvironmentUseCase,
    private val setActiveEnvironment: SetActiveEnvironmentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SwitcherUiState())
    val uiState: StateFlow<SwitcherUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeEnvironments(),
            observeActiveEnvironment(),
        ) { envs, activeId ->
            SwitcherUiState(environments = envs, activeId = activeId)
        }
            .onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SwitcherEvent) {
        when (event) {
            is SwitcherEvent.OnSelect -> viewModelScope.launch {
                setActiveEnvironment(event.id)
            }
            SwitcherEvent.OnManageEnvironments -> Unit
        }
    }
}
