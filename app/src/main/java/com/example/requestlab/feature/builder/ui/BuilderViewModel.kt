package com.example.requestlab.feature.builder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.FailureKind
import com.example.requestlab.core.common.model.FileRef
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.RequestBody
import com.example.requestlab.core.designsystem.components.BadgeStatus
import com.example.requestlab.feature.builder.domain.model.DraftSource
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import com.example.requestlab.feature.builder.domain.model.emptyDraft
import com.example.requestlab.feature.builder.domain.usecase.GetHeaderSuggestionsUseCase
import com.example.requestlab.feature.builder.domain.usecase.GetHeaderValueSuggestionsUseCase
import com.example.requestlab.feature.builder.domain.usecase.LoadDraftUseCase
import com.example.requestlab.feature.builder.domain.usecase.ObserveConnectivityUseCase
import com.example.requestlab.feature.builder.domain.usecase.ResolveDraftUseCase
import com.example.requestlab.feature.builder.domain.usecase.SendRequestUseCase
import com.example.requestlab.feature.builder.domain.usecase.UpdateDraftUseCase
import com.example.requestlab.feature.environments.domain.usecase.ObserveActiveEnvironmentUseCase
import com.example.requestlab.feature.environments.domain.usecase.ObserveEnvironmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BuilderTab { HEADERS, PARAMS, BODY, AUTH }
enum class BuilderPane { REQUEST, RESPONSE }
enum class BodyType { NONE, JSON, RAW, FORM, MULTIPART, BINARY }
enum class AuthType { NONE, BASIC, BEARER }
enum class AuthField { USERNAME, PASSWORD, TOKEN }

sealed interface DraftStatus {
    data object Loading : DraftStatus
    data object Ready : DraftStatus
    data class HydrateError(val message: String) : DraftStatus
}

sealed interface SendState {
    data object Idle : SendState
    data object Sending : SendState
    data object Done : SendState
}

sealed interface ResponseUiState {
    data object Empty : ResponseUiState
    data object Loading : ResponseUiState
    data class Content(
        val status: BadgeStatus,
        val latencyMs: Long,
        val headers: List<KeyValue>,
        val body: BodyUi,
        val showRaw: Boolean,
    ) : ResponseUiState
    data class Failure(val reason: String, val kind: FailureKind) : ResponseUiState
}

sealed interface BodyUi {
    data class Pretty(val text: String) : BodyUi
    data class Raw(val text: String) : BodyUi
    data class Truncated(val shown: String, val totalBytes: Long) : BodyUi
}

data class BuilderUiState(
    val draftStatus: DraftStatus = DraftStatus.Loading,
    val draft: RequestDraft = emptyDraft(),
    val activeTab: BuilderTab = BuilderTab.HEADERS,
    val phonePane: BuilderPane = BuilderPane.REQUEST,
    val responseAvailable: Boolean = false,
    val sendState: SendState = SendState.Idle,
    val response: ResponseUiState = ResponseUiState.Empty,
    val inlineError: String? = null,
    val isOffline: Boolean = false,
    val sendEnabled: Boolean = false,
    val activeEnvironmentId: String? = null,
    val activeEnvironmentName: String? = null,
    val showSwitcherSheet: Boolean = false,
    val showCurlSheet: Boolean = false,
    val showSaveSheet: Boolean = false,
    val headerSuggestions: List<String> = emptyList(),
)

sealed interface BuilderEvent {
    data class OnMethodSelected(val method: HttpMethod) : BuilderEvent
    data class OnUrlChanged(val url: String) : BuilderEvent
    data class OnTabSelected(val tab: BuilderTab) : BuilderEvent
    data class OnHeaderEdited(val index: Int, val kv: KeyValue) : BuilderEvent
    data object OnAddHeader : BuilderEvent
    data class OnDeleteHeader(val index: Int) : BuilderEvent
    data class OnHeaderKeyTyping(val index: Int, val prefix: String) : BuilderEvent
    data object OnDismissHeaderSuggestions : BuilderEvent
    data class OnParamEdited(val index: Int, val kv: KeyValue) : BuilderEvent
    data object OnAddParam : BuilderEvent
    data class OnDeleteParam(val index: Int) : BuilderEvent
    data class OnBodyTypeChanged(val bodyType: BodyType) : BuilderEvent
    data class OnBodyTextChanged(val text: String) : BuilderEvent
    data class OnBodyRawContentTypeChanged(val contentType: String) : BuilderEvent
    data class OnFormFieldEdited(val index: Int, val kv: KeyValue) : BuilderEvent
    data object OnAddFormField : BuilderEvent
    data class OnDeleteFormField(val index: Int) : BuilderEvent
    data object OnPickFile : BuilderEvent
    data class OnFilePicked(val file: FileRef) : BuilderEvent
    data class OnAuthTypeChanged(val authType: AuthType) : BuilderEvent
    data class OnAuthFieldChanged(val field: AuthField, val value: String) : BuilderEvent
    data object OnSendClicked : BuilderEvent
    data object OnCancelSend : BuilderEvent
    data object OnRetrySend : BuilderEvent
    data class OnPhonePaneChanged(val pane: BuilderPane) : BuilderEvent
    data object OnToggleRawBody : BuilderEvent
    data object OnCopyBody : BuilderEvent
    data object OnRequestSave : BuilderEvent
    data object OnRequestCurl : BuilderEvent
    data object OnDismissInlineError : BuilderEvent
    data object OnOpenSwitcher : BuilderEvent
    data object OnDismissSwitcher : BuilderEvent
    data object OnDismissCurlSheet : BuilderEvent
    data object OnDismissSaveSheet : BuilderEvent
}

private const val BODY_TRUNCATION_BYTES = 500_000L

@HiltViewModel
class BuilderViewModel @Inject constructor(
    private val loadDraft: LoadDraftUseCase,
    private val updateDraft: UpdateDraftUseCase,
    private val resolveDraft: ResolveDraftUseCase,
    private val sendRequest: SendRequestUseCase,
    private val observeConnectivity: ObserveConnectivityUseCase,
    observeActiveEnvironment: ObserveActiveEnvironmentUseCase,
    observeEnvironments: ObserveEnvironmentsUseCase,
    private val getHeaderSuggestions: GetHeaderSuggestionsUseCase,
    private val getHeaderValueSuggestions: GetHeaderValueSuggestionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    private var sendJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = loadDraft(DraftSource.New)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        draftStatus = DraftStatus.Ready,
                        draft = result.value,
                        sendEnabled = result.value.url.isNotBlank(),
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(draftStatus = DraftStatus.HydrateError("Couldn't load draft."))
                }
            }
        }

        observeConnectivity()
            .onEach { isOnline -> _uiState.update { it.copy(isOffline = !isOnline) } }
            .launchIn(viewModelScope)

        combine(observeEnvironments(), observeActiveEnvironment()) { envs, activeId ->
            val name = envs.firstOrNull { it.id == activeId }?.name
            activeId to name
        }
            .onEach { (id, name) ->
                _uiState.update { it.copy(activeEnvironmentId = id, activeEnvironmentName = name) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: BuilderEvent) {
        when (event) {
            is BuilderEvent.OnMethodSelected -> mutateDraft { it.copy(method = event.method) }
            is BuilderEvent.OnUrlChanged -> mutateDraft { it.copy(url = event.url) }
            is BuilderEvent.OnTabSelected -> _uiState.update { it.copy(activeTab = event.tab) }
            is BuilderEvent.OnHeaderEdited -> mutateDraft { draft ->
                draft.copy(headers = draft.headers.toMutableList().also { it[event.index] = event.kv })
            }
            BuilderEvent.OnAddHeader -> mutateDraft { draft ->
                draft.copy(headers = draft.headers + KeyValue("", ""))
            }
            is BuilderEvent.OnDeleteHeader -> mutateDraft { draft ->
                draft.copy(headers = draft.headers.toMutableList().also { it.removeAt(event.index) })
            }
            is BuilderEvent.OnHeaderKeyTyping -> {
                _uiState.update {
                    it.copy(headerSuggestions = getHeaderSuggestions(event.prefix))
                }
            }
            BuilderEvent.OnDismissHeaderSuggestions -> _uiState.update {
                it.copy(headerSuggestions = emptyList())
            }
            is BuilderEvent.OnParamEdited -> mutateDraft { draft ->
                draft.copy(params = draft.params.toMutableList().also { it[event.index] = event.kv })
            }
            BuilderEvent.OnAddParam -> mutateDraft { draft ->
                draft.copy(params = draft.params + KeyValue("", ""))
            }
            is BuilderEvent.OnDeleteParam -> mutateDraft { draft ->
                draft.copy(params = draft.params.toMutableList().also { it.removeAt(event.index) })
            }
            is BuilderEvent.OnBodyTypeChanged -> mutateDraft { draft ->
                val newBody = when (event.bodyType) {
                    BodyType.NONE -> RequestBody.None
                    BodyType.JSON -> RequestBody.Json("")
                    BodyType.RAW -> RequestBody.RawText("", "text/plain")
                    BodyType.FORM -> RequestBody.FormUrlEncoded(emptyList())
                    BodyType.MULTIPART -> RequestBody.Multipart(emptyList())
                    BodyType.BINARY -> if (draft.body is RequestBody.Binary) draft.body else
                        return@mutateDraft null
                }
                draft.copy(body = newBody)
            }
            is BuilderEvent.OnBodyTextChanged -> mutateDraft { draft ->
                draft.copy(
                    body = when (val b = draft.body) {
                        is RequestBody.Json -> b.copy(text = event.text)
                        is RequestBody.RawText -> b.copy(text = event.text)
                        else -> b
                    },
                )
            }
            is BuilderEvent.OnBodyRawContentTypeChanged -> mutateDraft { draft ->
                draft.copy(
                    body = (draft.body as? RequestBody.RawText)?.copy(contentType = event.contentType)
                        ?: draft.body,
                )
            }
            is BuilderEvent.OnFormFieldEdited -> mutateDraft { draft ->
                val formBody = (draft.body as? RequestBody.FormUrlEncoded) ?: return@mutateDraft null
                draft.copy(body = formBody.copy(fields = formBody.fields.toMutableList().also { it[event.index] = event.kv }))
            }
            BuilderEvent.OnAddFormField -> mutateDraft { draft ->
                val formBody = (draft.body as? RequestBody.FormUrlEncoded) ?: return@mutateDraft null
                draft.copy(body = formBody.copy(fields = formBody.fields + KeyValue("", "")))
            }
            is BuilderEvent.OnDeleteFormField -> mutateDraft { draft ->
                val formBody = (draft.body as? RequestBody.FormUrlEncoded) ?: return@mutateDraft null
                draft.copy(body = formBody.copy(fields = formBody.fields.toMutableList().also { it.removeAt(event.index) }))
            }
            BuilderEvent.OnPickFile -> Unit
            is BuilderEvent.OnFilePicked -> mutateDraft { it.copy(body = RequestBody.Binary(event.file)) }
            is BuilderEvent.OnAuthTypeChanged -> mutateDraft { draft ->
                val newAuth = when (event.authType) {
                    AuthType.NONE -> AuthConfig.None
                    AuthType.BASIC -> (draft.auth as? AuthConfig.Basic) ?: AuthConfig.Basic("", "")
                    AuthType.BEARER -> (draft.auth as? AuthConfig.Bearer) ?: AuthConfig.Bearer("")
                }
                draft.copy(auth = newAuth)
            }
            is BuilderEvent.OnAuthFieldChanged -> mutateDraft { draft ->
                val updated = when (event.field) {
                    AuthField.USERNAME -> (draft.auth as? AuthConfig.Basic)?.copy(username = event.value) ?: draft.auth
                    AuthField.PASSWORD -> (draft.auth as? AuthConfig.Basic)?.copy(password = event.value) ?: draft.auth
                    AuthField.TOKEN -> (draft.auth as? AuthConfig.Bearer)?.copy(token = event.value) ?: draft.auth
                }
                draft.copy(auth = updated)
            }
            BuilderEvent.OnSendClicked -> doSend()
            BuilderEvent.OnCancelSend -> {
                sendJob?.cancel()
                sendJob = null
            }
            BuilderEvent.OnRetrySend -> doSend()
            is BuilderEvent.OnPhonePaneChanged -> _uiState.update { it.copy(phonePane = event.pane) }
            BuilderEvent.OnToggleRawBody -> _uiState.update { state ->
                val content = state.response as? ResponseUiState.Content ?: return
                state.copy(response = content.copy(showRaw = !content.showRaw))
            }
            BuilderEvent.OnCopyBody -> Unit
            BuilderEvent.OnRequestSave -> {
                flushDraft()
                _uiState.update { it.copy(showSaveSheet = true) }
            }
            BuilderEvent.OnRequestCurl -> {
                flushDraft()
                _uiState.update { it.copy(showCurlSheet = true) }
            }
            BuilderEvent.OnDismissInlineError -> _uiState.update { it.copy(inlineError = null) }
            BuilderEvent.OnOpenSwitcher -> _uiState.update { it.copy(showSwitcherSheet = true) }
            BuilderEvent.OnDismissSwitcher -> _uiState.update { it.copy(showSwitcherSheet = false) }
            BuilderEvent.OnDismissCurlSheet -> _uiState.update { it.copy(showCurlSheet = false) }
            BuilderEvent.OnDismissSaveSheet -> _uiState.update { it.copy(showSaveSheet = false) }
        }
    }

    private fun mutateDraft(transform: (RequestDraft) -> RequestDraft?) {
        val current = _uiState.value.draft
        val updated = transform(current) ?: return
        _uiState.update { it.copy(draft = updated, sendEnabled = updated.url.isNotBlank()) }
        viewModelScope.launch { updateDraft(updated) }
    }

    private fun flushDraft() {
        viewModelScope.launch { updateDraft(_uiState.value.draft) }
    }

    private fun doSend() {
        val state = _uiState.value
        if (state.sendState == SendState.Sending) return
        if (!state.sendEnabled) return

        if (state.isOffline) {
            _uiState.update {
                it.copy(inlineError = "You're offline — sending requires a connection")
            }
            return
        }

        sendJob = viewModelScope.launch {
            _uiState.update {
                it.copy(sendState = SendState.Sending, response = ResponseUiState.Loading, inlineError = null)
            }

            val draft = _uiState.value.draft
            val activeEnvId = _uiState.value.activeEnvironmentId

            val resolveResult = resolveDraft(draft, activeEnvId)
            if (resolveResult is AppResult.Failure) {
                val message = when (val err = resolveResult.error) {
                    is AppError.Validation -> err.message
                    else -> "Couldn't prepare request"
                }
                _uiState.update {
                    it.copy(sendState = SendState.Idle, response = ResponseUiState.Empty, inlineError = message)
                }
                return@launch
            }

            val prepared = (resolveResult as AppResult.Success).value
            val sendResult = sendRequest(prepared)

            when (sendResult) {
                is AppResult.Success -> {
                    val outcome = sendResult.value
                    val responseUi = if (outcome.response != null) {
                        val body = buildBodyUi(outcome.response.body, outcome.response.bodySizeBytes)
                        ResponseUiState.Content(
                            status = BadgeStatus.Http(outcome.response.statusCode, outcome.response.statusMessage),
                            latencyMs = outcome.response.latencyMs,
                            headers = outcome.response.headers,
                            body = body,
                            showRaw = false,
                        )
                    } else if (outcome.failure != null) {
                        ResponseUiState.Failure(outcome.failure.message, outcome.failure.kind)
                    } else {
                        ResponseUiState.Empty
                    }
                    _uiState.update {
                        it.copy(
                            sendState = SendState.Done,
                            response = responseUi,
                            responseAvailable = true,
                            phonePane = BuilderPane.RESPONSE,
                        )
                    }
                }
                is AppResult.Failure -> {
                    val responseUi = when (val err = sendResult.error) {
                        is AppError.Timeout -> ResponseUiState.Failure(
                            "Timeout after ${err.seconds} s", FailureKind.TIMEOUT,
                        )
                        AppError.NoInternet -> ResponseUiState.Failure(
                            "No internet connection", FailureKind.NO_INTERNET,
                        )
                        AppError.Dns -> ResponseUiState.Failure("Couldn't resolve host", FailureKind.DNS)
                        AppError.Tls -> ResponseUiState.Failure("TLS handshake failed", FailureKind.TLS)
                        AppError.Cancelled -> return@launch
                        else -> ResponseUiState.Failure("Request failed", FailureKind.UNKNOWN)
                    }
                    _uiState.update {
                        it.copy(
                            sendState = SendState.Idle,
                            response = responseUi,
                            responseAvailable = true,
                            phonePane = BuilderPane.RESPONSE,
                        )
                    }
                }
            }
        }.also { job ->
            job.invokeOnCompletion { throwable ->
                if (throwable is CancellationException) {
                    _uiState.update { it.copy(sendState = SendState.Idle, response = ResponseUiState.Empty) }
                }
            }
        }
    }

    private fun buildBodyUi(body: String, sizeBytes: Long): BodyUi =
        if (sizeBytes > BODY_TRUNCATION_BYTES) {
            BodyUi.Truncated(
                shown = body.take(BODY_TRUNCATION_BYTES.toInt()),
                totalBytes = sizeBytes,
            )
        } else {
            BodyUi.Pretty(body)
        }
}
