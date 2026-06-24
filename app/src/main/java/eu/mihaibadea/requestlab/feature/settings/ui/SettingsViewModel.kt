package eu.mihaibadea.requestlab.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.settings.data.AppResetCoordinator
import eu.mihaibadea.requestlab.feature.settings.domain.model.AppSettings
import eu.mihaibadea.requestlab.feature.settings.domain.usecase.ObserveSettingsUseCase
import eu.mihaibadea.requestlab.feature.settings.domain.usecase.ResetAppUseCase
import eu.mihaibadea.requestlab.feature.settings.domain.usecase.UpdateFollowRedirectsUseCase
import eu.mihaibadea.requestlab.feature.settings.domain.usecase.UpdateTimeoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BusyAction { None, ClearingHistory, Resetting }

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val busyAction: BusyAction = BusyAction.None,
    val actionError: String? = null,
)

sealed interface SettingsEvent {
    data class OnTimeoutChanged(val seconds: Int) : SettingsEvent
    data class OnFollowRedirectsToggled(val enabled: Boolean) : SettingsEvent
    data object OnClearHistoryConfirmed : SettingsEvent
    data object OnResetConfirmed : SettingsEvent
    data object OnDismissError : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val updateTimeout: UpdateTimeoutUseCase,
    private val updateFollowRedirects: UpdateFollowRedirectsUseCase,
    private val resetApp: ResetAppUseCase,
    private val coordinator: AppResetCoordinator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
            .onEach { settings -> _uiState.value = _uiState.value.copy(settings = settings) }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnTimeoutChanged -> viewModelScope.launch {
                updateTimeout(event.seconds)
            }
            is SettingsEvent.OnFollowRedirectsToggled -> viewModelScope.launch {
                updateFollowRedirects(event.enabled)
            }
            is SettingsEvent.OnClearHistoryConfirmed -> clearHistory()
            is SettingsEvent.OnResetConfirmed -> reset()
            is SettingsEvent.OnDismissError -> _uiState.value = _uiState.value.copy(actionError = null)
        }
    }

    private fun clearHistory() {
        if (_uiState.value.busyAction != BusyAction.None) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyAction = BusyAction.ClearingHistory, actionError = null)
            val result = coordinator.clearHistoryOnly()
            _uiState.value = _uiState.value.copy(
                busyAction = BusyAction.None,
                actionError = if (result is AppResult.Failure) "Couldn't clear history." else null,
            )
        }
    }

    private fun reset() {
        if (_uiState.value.busyAction != BusyAction.None) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(busyAction = BusyAction.Resetting, actionError = null)
            val result = resetApp()
            _uiState.value = _uiState.value.copy(
                busyAction = BusyAction.None,
                actionError = if (result is AppResult.Failure) "Couldn't reset app data." else null,
            )
        }
    }
}
