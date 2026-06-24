package eu.mihaibadea.requestlab.feature.builder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.builder.domain.RequestDraftRepository
import eu.mihaibadea.requestlab.feature.builder.domain.model.CurlCommand
import eu.mihaibadea.requestlab.feature.builder.domain.usecase.GenerateCurlUseCase
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.ObserveActiveEnvironmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CurlUiState {
    data object Generating : CurlUiState
    data class Content(
        val command: CurlCommand,
        val includeCredentials: Boolean,
        val unresolvedWarning: String?,
    ) : CurlUiState
}

sealed interface CurlEvent {
    data object OnToggleIncludeCredentials : CurlEvent
    data object OnCopy : CurlEvent
    data object OnShare : CurlEvent
    data object OnDismiss : CurlEvent
}

@HiltViewModel
class CurlExportViewModel @Inject constructor(
    private val generateCurl: GenerateCurlUseCase,
    private val draftRepository: RequestDraftRepository,
    observeActiveEnvironment: ObserveActiveEnvironmentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CurlUiState>(CurlUiState.Generating)
    val uiState: StateFlow<CurlUiState> = _uiState.asStateFlow()

    private val _includeCredentials = MutableStateFlow(false)
    private val _copySignal = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val copySignal = _copySignal.asSharedFlow()
    private val _shareSignal = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val shareSignal = _shareSignal.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                draftRepository.observeWorkingDraft(),
                observeActiveEnvironment(),
                _includeCredentials,
            ) { draft, activeEnvId, includeCredentials ->
                Triple(draft, activeEnvId, includeCredentials)
            }.collect { (draft, activeEnvId, includeCredentials) ->
                if (draft == null) return@collect
                _uiState.update { CurlUiState.Generating }
                val result = generateCurl(draft, activeEnvId, includeCredentials)
                if (result is AppResult.Success) {
                    val cmd = result.value
                    _uiState.update {
                        CurlUiState.Content(
                            command = cmd,
                            includeCredentials = includeCredentials,
                            unresolvedWarning = if (cmd.unresolvedVariables.isNotEmpty()) {
                                "Unresolved variables: ${cmd.unresolvedVariables.joinToString()}"
                            } else null,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: CurlEvent) {
        when (event) {
            CurlEvent.OnToggleIncludeCredentials -> _includeCredentials.update { !it }
            CurlEvent.OnCopy -> {
                val cmd = (uiState.value as? CurlUiState.Content)?.command?.text ?: return
                _copySignal.tryEmit(cmd)
            }
            CurlEvent.OnShare -> {
                val cmd = (uiState.value as? CurlUiState.Content)?.command?.text ?: return
                _shareSignal.tryEmit(cmd)
            }
            CurlEvent.OnDismiss -> Unit
        }
    }
}
