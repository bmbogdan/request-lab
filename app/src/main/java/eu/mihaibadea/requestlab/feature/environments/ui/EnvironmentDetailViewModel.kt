package eu.mihaibadea.requestlab.feature.environments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.environments.domain.model.Variable
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.GetEnvironmentDetailUseCase
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.RenameEnvironmentUseCase
import eu.mihaibadea.requestlab.feature.environments.domain.usecase.SaveVariablesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EnvironmentDetailUiState {
    data object Loading : EnvironmentDetailUiState
    data class Content(
        val name: String,
        val rows: List<Variable>,
        val validationErrors: Map<Int, String>,
        val saving: Boolean,
        val saveError: String?,
    ) : EnvironmentDetailUiState
    data class Error(val message: String) : EnvironmentDetailUiState
}

sealed interface EnvironmentDetailEvent {
    data object OnBack : EnvironmentDetailEvent
    data class OnNameChanged(val name: String) : EnvironmentDetailEvent
    data object OnAddVariable : EnvironmentDetailEvent
    data class OnVariableEdited(val index: Int, val variable: Variable) : EnvironmentDetailEvent
    data class OnDeleteVariable(val index: Int) : EnvironmentDetailEvent
    data object OnSave : EnvironmentDetailEvent
    data object OnRetry : EnvironmentDetailEvent
}

@HiltViewModel
class EnvironmentDetailViewModel @Inject constructor(
    private val getEnvironmentDetail: GetEnvironmentDetailUseCase,
    private val saveVariables: SaveVariablesUseCase,
    private val renameEnvironment: RenameEnvironmentUseCase,
) : ViewModel() {

    private var environmentId: String = ""

    private val _uiState = MutableStateFlow<EnvironmentDetailUiState>(EnvironmentDetailUiState.Loading)
    val uiState: StateFlow<EnvironmentDetailUiState> = _uiState.asStateFlow()

    fun init(id: String) {
        if (environmentId == id) return
        environmentId = id
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = EnvironmentDetailUiState.Loading
            when (val result = getEnvironmentDetail(environmentId)) {
                is AppResult.Success -> _uiState.value = EnvironmentDetailUiState.Content(
                    name = result.value.name,
                    rows = result.value.variables,
                    validationErrors = emptyMap(),
                    saving = false,
                    saveError = null,
                )
                is AppResult.Failure -> _uiState.value = EnvironmentDetailUiState.Error("Couldn't load environment.")
            }
        }
    }

    fun onEvent(event: EnvironmentDetailEvent) {
        val current = _uiState.value as? EnvironmentDetailUiState.Content ?: return
        when (event) {
            is EnvironmentDetailEvent.OnNameChanged ->
                _uiState.value = current.copy(name = event.name, validationErrors = emptyMap())
            is EnvironmentDetailEvent.OnAddVariable ->
                _uiState.value = current.copy(rows = current.rows + Variable("", ""))
            is EnvironmentDetailEvent.OnVariableEdited -> {
                val updated = current.rows.toMutableList().also { it[event.index] = event.variable }
                _uiState.value = current.copy(rows = updated, validationErrors = computeErrors(updated))
            }
            is EnvironmentDetailEvent.OnDeleteVariable -> {
                val updated = current.rows.toMutableList().also { it.removeAt(event.index) }
                _uiState.value = current.copy(rows = updated, validationErrors = computeErrors(updated))
            }
            is EnvironmentDetailEvent.OnSave -> save(current)
            is EnvironmentDetailEvent.OnRetry -> load()
            else -> Unit
        }
    }

    private fun save(current: EnvironmentDetailUiState.Content) {
        if (current.validationErrors.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.value = current.copy(saving = true, saveError = null)
            val nameResult = renameEnvironment(environmentId, current.name)
            val varsResult = saveVariables(environmentId, current.rows.filter { it.key.isNotBlank() })
            val error = when {
                nameResult is AppResult.Failure -> "Couldn't save name."
                varsResult is AppResult.Failure -> "Couldn't save variables."
                else -> null
            }
            _uiState.value = current.copy(saving = false, saveError = error)
        }
    }

    private fun computeErrors(rows: List<Variable>): Map<Int, String> {
        val seen = mutableSetOf<String>()
        val errors = mutableMapOf<Int, String>()
        rows.forEachIndexed { i, v ->
            if (v.key.isNotBlank()) {
                if (!seen.add(v.key)) errors[i] = "Key already exists"
            }
        }
        return errors
    }
}
