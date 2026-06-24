@file:OptIn(ExperimentalMaterial3Api::class)

package eu.mihaibadea.requestlab.feature.builder.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.mihaibadea.requestlab.R
import eu.mihaibadea.requestlab.core.common.model.AuthConfig
import eu.mihaibadea.requestlab.core.common.model.HttpMethod
import eu.mihaibadea.requestlab.core.common.model.KeyValue
import eu.mihaibadea.requestlab.core.common.model.RequestBody
import eu.mihaibadea.requestlab.core.designsystem.components.AutocompleteDropdown
import eu.mihaibadea.requestlab.core.designsystem.components.KeyValueRow
import eu.mihaibadea.requestlab.core.designsystem.components.MethodChip
import eu.mihaibadea.requestlab.core.designsystem.components.OfflineBanner
import eu.mihaibadea.requestlab.core.designsystem.components.TokenHighlightField
import eu.mihaibadea.requestlab.core.designsystem.theme.AppTheme
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.builder.domain.model.emptyDraft
import eu.mihaibadea.requestlab.feature.environments.ui.EnvironmentSwitcherSheet

// ─── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun BuilderScreen(
    onNavigateToEnvironments: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: BuilderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BuilderContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToEnvironments = onNavigateToEnvironments,
        modifier = modifier,
    )
}

// ─── Stateless content ─────────────────────────────────────────────────────────

@Composable
fun BuilderContent(
    uiState: BuilderUiState,
    onEvent: (BuilderEvent) -> Unit,
    onNavigateToEnvironments: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.inlineError) {
        val msg = uiState.inlineError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        onEvent(BuilderEvent.OnDismissInlineError)
    }

    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = modifier,
        topBar = {
            BuilderTopBar(
                activeEnvironmentName = uiState.activeEnvironmentName,
                onOpenSwitcher = { onEvent(BuilderEvent.OnOpenSwitcher) },
                onRequestSave = { onEvent(BuilderEvent.OnRequestSave) },
                onRequestCurl = { onEvent(BuilderEvent.OnRequestCurl) },
                onNavigateToSettings = {},
            )
        },
        floatingActionButton = {
            if (isExpanded || uiState.phonePane == BuilderPane.REQUEST) {
                SendFab(
                    sendState = uiState.sendState,
                    enabled = uiState.sendEnabled && !uiState.isOffline,
                    onClick = { onEvent(BuilderEvent.OnSendClicked) },
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.isOffline) OfflineBanner()

            if (isExpanded) {
                ExpandedLayout(uiState = uiState, onEvent = onEvent)
            } else {
                PhoneLayout(uiState = uiState, onEvent = onEvent)
            }
        }
    }

    if (uiState.showSwitcherSheet) {
        EnvironmentSwitcherSheet(
            onDismiss = { onEvent(BuilderEvent.OnDismissSwitcher) },
            onManageEnvironments = onNavigateToEnvironments,
        )
    }
    if (uiState.showCurlSheet) {
        CurlExportSheet(onDismiss = { onEvent(BuilderEvent.OnDismissCurlSheet) })
    }
    if (uiState.showSaveSheet) {
        SaveToCollectionSheet(onDismiss = { onEvent(BuilderEvent.OnDismissSaveSheet) })
    }
}

// ─── Layouts ───────────────────────────────────────────────────────────────────

@Composable
private fun PhoneLayout(
    uiState: BuilderUiState,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        RequestSection(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        PhonePaneTabRow(
            selectedPane = uiState.phonePane,
            responseAvailable = uiState.responseAvailable,
            onPaneSelected = { onEvent(BuilderEvent.OnPhonePaneChanged(it)) },
        )
        if (uiState.phonePane == BuilderPane.RESPONSE) {
            ResponsePane(
                state = uiState.response,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun ExpandedLayout(
    uiState: BuilderUiState,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        RequestSection(
            uiState = uiState,
            onEvent = onEvent,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        )
        HorizontalDivider(modifier = Modifier.width(1.dp).fillMaxSize())
        ResponsePane(
            state = uiState.response,
            onEvent = onEvent,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        )
    }
}

// ─── Request section ───────────────────────────────────────────────────────────

@Composable
private fun RequestSection(
    uiState: BuilderUiState,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        UrlRow(
            method = uiState.draft.method,
            url = uiState.draft.url,
            onMethodSelected = { onEvent(BuilderEvent.OnMethodSelected(it)) },
            onUrlChanged = { onEvent(BuilderEvent.OnUrlChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        )

        BuilderTabRow(
            activeTab = uiState.activeTab,
            draft = uiState.draft,
            onTabSelected = { onEvent(BuilderEvent.OnTabSelected(it)) },
        )

        HorizontalDivider()

        when (uiState.activeTab) {
            BuilderTab.HEADERS -> HeadersTab(
                headers = uiState.draft.headers,
                headerSuggestions = uiState.headerSuggestions,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            BuilderTab.PARAMS -> ParamsTab(
                params = uiState.draft.params,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            BuilderTab.BODY -> BodyTab(
                body = uiState.draft.body,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            BuilderTab.AUTH -> AuthTab(
                auth = uiState.draft.auth,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

// ─── URL row ───────────────────────────────────────────────────────────────────

@Composable
private fun UrlRow(
    method: HttpMethod,
    url: String,
    onMethodSelected: (HttpMethod) -> Unit,
    onUrlChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMethodMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Box {
            MethodChip(method = method, onClick = { showMethodMenu = true })
            DropdownMenu(
                expanded = showMethodMenu,
                onDismissRequest = { showMethodMenu = false },
            ) {
                HttpMethod.entries.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.name, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onMethodSelected(m)
                            showMethodMenu = false
                        },
                    )
                }
            }
        }
        TokenHighlightField(
            value = url,
            onValueChange = onUrlChanged,
            label = stringResource(R.string.url_label),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
}

// ─── Tab row ───────────────────────────────────────────────────────────────────

@Composable
private fun BuilderTabRow(
    activeTab: BuilderTab,
    draft: RequestDraft,
    onTabSelected: (BuilderTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = BuilderTab.entries
    PrimaryScrollableTabRow(
        selectedTabIndex = tabs.indexOf(activeTab),
        modifier = modifier,
        edgePadding = MaterialTheme.spacing.md,
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tabLabel(tab, draft),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}

private fun tabLabel(tab: BuilderTab, draft: RequestDraft): String = when (tab) {
    BuilderTab.HEADERS -> "Headers${if (draft.headers.isNotEmpty()) " (${draft.headers.size})" else ""}"
    BuilderTab.PARAMS -> "Params${if (draft.params.isNotEmpty()) " (${draft.params.size})" else ""}"
    BuilderTab.BODY -> "Body${if (draft.body !is RequestBody.None) " •" else ""}"
    BuilderTab.AUTH -> "Auth${if (draft.auth !is AuthConfig.None) " •" else ""}"
}

// ─── Headers tab ───────────────────────────────────────────────────────────────

@Composable
private fun HeadersTab(
    headers: List<KeyValue>,
    headerSuggestions: List<String>,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        itemsIndexed(headers) { index, kv ->
            Box {
                var showSuggestions by remember { mutableStateOf(false) }
                KeyValueRow(
                    key = kv.key,
                    value = kv.value,
                    onKeyChange = { newKey ->
                        onEvent(BuilderEvent.OnHeaderEdited(index, kv.copy(key = newKey)))
                        onEvent(BuilderEvent.OnHeaderKeyTyping(index, newKey))
                        showSuggestions = newKey.isNotEmpty()
                    },
                    onValueChange = { onEvent(BuilderEvent.OnHeaderEdited(index, kv.copy(value = it))) },
                    onDelete = { onEvent(BuilderEvent.OnDeleteHeader(index)) },
                    keyLabel = stringResource(R.string.header_key_label),
                    valueLabel = stringResource(R.string.header_value_label),
                )
                AutocompleteDropdown(
                    suggestions = headerSuggestions,
                    expanded = showSuggestions && headerSuggestions.isNotEmpty(),
                    onSelect = { suggestion ->
                        onEvent(BuilderEvent.OnHeaderEdited(index, kv.copy(key = suggestion)))
                        onEvent(BuilderEvent.OnDismissHeaderSuggestions)
                        showSuggestions = false
                    },
                    onDismiss = {
                        onEvent(BuilderEvent.OnDismissHeaderSuggestions)
                        showSuggestions = false
                    },
                )
            }
        }
        item {
            TextButton(
                onClick = { onEvent(BuilderEvent.OnAddHeader) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                Text(stringResource(R.string.add_header))
            }
        }
    }
}

// ─── Params tab ────────────────────────────────────────────────────────────────

@Composable
private fun ParamsTab(
    params: List<KeyValue>,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.sm,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        itemsIndexed(params) { index, kv ->
            KeyValueRow(
                key = kv.key,
                value = kv.value,
                onKeyChange = { onEvent(BuilderEvent.OnParamEdited(index, kv.copy(key = it))) },
                onValueChange = { onEvent(BuilderEvent.OnParamEdited(index, kv.copy(value = it))) },
                onDelete = { onEvent(BuilderEvent.OnDeleteParam(index)) },
                keyLabel = stringResource(R.string.param_key_label),
                valueLabel = stringResource(R.string.param_value_label),
            )
        }
        item {
            TextButton(
                onClick = { onEvent(BuilderEvent.OnAddParam) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                Text(stringResource(R.string.add_param))
            }
        }
    }
}

// ─── Body tab ──────────────────────────────────────────────────────────────────

@Composable
private fun BodyTab(
    body: RequestBody,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        BodyTypeChipRow(
            selected = body.toBodyType(),
            onSelect = { onEvent(BuilderEvent.OnBodyTypeChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        )
        HorizontalDivider()
        when (body) {
            RequestBody.None -> BodyEmptyHint(modifier = Modifier.weight(1f))
            is RequestBody.Json -> OutlinedTextField(
                value = body.text,
                onValueChange = { onEvent(BuilderEvent.OnBodyTextChanged(it)) },
                label = { Text(stringResource(R.string.body_json_label)) },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(MaterialTheme.spacing.md),
            )
            is RequestBody.RawText -> Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = body.contentType,
                    onValueChange = { onEvent(BuilderEvent.OnBodyRawContentTypeChanged(it)) },
                    label = { Text(stringResource(R.string.body_content_type_label)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                )
                OutlinedTextField(
                    value = body.text,
                    onValueChange = { onEvent(BuilderEvent.OnBodyTextChanged(it)) },
                    label = { Text(stringResource(R.string.body_raw_label)) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(MaterialTheme.spacing.md),
                )
            }
            is RequestBody.FormUrlEncoded -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.md,
                    vertical = MaterialTheme.spacing.sm,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                itemsIndexed(body.fields) { index, kv ->
                    KeyValueRow(
                        key = kv.key,
                        value = kv.value,
                        onKeyChange = { onEvent(BuilderEvent.OnFormFieldEdited(index, kv.copy(key = it))) },
                        onValueChange = { onEvent(BuilderEvent.OnFormFieldEdited(index, kv.copy(value = it))) },
                        onDelete = { onEvent(BuilderEvent.OnDeleteFormField(index)) },
                        keyLabel = stringResource(R.string.form_field_key_label),
                        valueLabel = stringResource(R.string.form_field_value_label),
                    )
                }
                item {
                    TextButton(
                        onClick = { onEvent(BuilderEvent.OnAddFormField) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(MaterialTheme.spacing.sm))
                        Text(stringResource(R.string.add_field))
                    }
                }
            }
            is RequestBody.Multipart -> BodyEmptyHint(
                message = stringResource(R.string.multipart_not_supported),
                modifier = Modifier.weight(1f),
            )
            is RequestBody.Binary -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = body.file.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyTypeChipRow(
    selected: BodyType,
    onSelect: (BodyType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        listOf(BodyType.NONE, BodyType.JSON, BodyType.RAW, BodyType.FORM).forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(type.label(), style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

private fun BodyType.label(): String = when (this) {
    BodyType.NONE -> "None"
    BodyType.JSON -> "JSON"
    BodyType.RAW -> "Raw"
    BodyType.FORM -> "Form"
    BodyType.MULTIPART -> "Multipart"
    BodyType.BINARY -> "Binary"
}

private fun RequestBody.toBodyType(): BodyType = when (this) {
    RequestBody.None -> BodyType.NONE
    is RequestBody.Json -> BodyType.JSON
    is RequestBody.RawText -> BodyType.RAW
    is RequestBody.FormUrlEncoded -> BodyType.FORM
    is RequestBody.Multipart -> BodyType.MULTIPART
    is RequestBody.Binary -> BodyType.BINARY
}

@Composable
private fun BodyEmptyHint(
    message: String = stringResource(R.string.body_none_hint),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Auth tab ──────────────────────────────────────────────────────────────────

@Composable
private fun AuthTab(
    auth: AuthConfig,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            listOf(AuthType.NONE, AuthType.BASIC, AuthType.BEARER).forEach { type ->
                FilterChip(
                    selected = auth.toAuthType() == type,
                    onClick = { onEvent(BuilderEvent.OnAuthTypeChanged(type)) },
                    label = { Text(type.label(), style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
        HorizontalDivider()
        when (auth) {
            AuthConfig.None -> BodyEmptyHint(
                message = stringResource(R.string.auth_none_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            is AuthConfig.Basic -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                OutlinedTextField(
                    value = auth.username,
                    onValueChange = { onEvent(BuilderEvent.OnAuthFieldChanged(AuthField.USERNAME, it)) },
                    label = { Text(stringResource(R.string.auth_username_label)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = auth.password,
                    onValueChange = { onEvent(BuilderEvent.OnAuthFieldChanged(AuthField.PASSWORD, it)) },
                    label = { Text(stringResource(R.string.auth_password_label)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            is AuthConfig.Bearer -> OutlinedTextField(
                value = auth.token,
                onValueChange = { onEvent(BuilderEvent.OnAuthFieldChanged(AuthField.TOKEN, it)) },
                label = { Text(stringResource(R.string.auth_token_label)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.md),
            )
        }
    }
}

private fun AuthType.label(): String = when (this) {
    AuthType.NONE -> "None"
    AuthType.BASIC -> "Basic"
    AuthType.BEARER -> "Bearer"
}

private fun AuthConfig.toAuthType(): AuthType = when (this) {
    AuthConfig.None -> AuthType.NONE
    is AuthConfig.Basic -> AuthType.BASIC
    is AuthConfig.Bearer -> AuthType.BEARER
}

// ─── Phone pane tab row ────────────────────────────────────────────────────────

@Composable
private fun PhonePaneTabRow(
    selectedPane: BuilderPane,
    responseAvailable: Boolean,
    onPaneSelected: (BuilderPane) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider()
    PrimaryTabRow(
        selectedTabIndex = selectedPane.ordinal,
        modifier = modifier,
    ) {
        Tab(
            selected = selectedPane == BuilderPane.REQUEST,
            onClick = { onPaneSelected(BuilderPane.REQUEST) },
            text = { Text(stringResource(R.string.pane_request), style = MaterialTheme.typography.labelMedium) },
        )
        Tab(
            selected = selectedPane == BuilderPane.RESPONSE,
            onClick = { onPaneSelected(BuilderPane.RESPONSE) },
            text = {
                Text(
                    text = if (responseAvailable)
                        stringResource(R.string.pane_response_ready)
                    else
                        stringResource(R.string.pane_response),
                    style = MaterialTheme.typography.labelMedium,
                )
            },
        )
    }
}

// ─── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun BuilderTopBar(
    activeEnvironmentName: String?,
    onOpenSwitcher: () -> Unit,
    onRequestSave: () -> Unit,
    onRequestCurl: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOverflow by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.builder_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (activeEnvironmentName != null) {
                    Text(
                        text = activeEnvironmentName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        modifier = modifier,
        actions = {
            AssistChip(
                onClick = onOpenSwitcher,
                label = {
                    Text(
                        text = activeEnvironmentName ?: stringResource(R.string.no_environment),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                modifier = Modifier
                    .sizeIn(maxWidth = 140.dp)
                    .semantics { contentDescription = "Active environment: ${activeEnvironmentName ?: "none"}" },
            )
            Box {
                IconButton(onClick = { showOverflow = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.cd_overflow_menu),
                    )
                }
                DropdownMenu(
                    expanded = showOverflow,
                    onDismissRequest = { showOverflow = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_save_to_collection)) },
                        leadingIcon = { Icon(Icons.Default.SaveAlt, contentDescription = null) },
                        onClick = {
                            showOverflow = false
                            onRequestSave()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_export_curl)) },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                        onClick = {
                            showOverflow = false
                            onRequestCurl()
                        },
                    )
                }
            }
        },
    )
}

// ─── Send FAB ──────────────────────────────────────────────────────────────────

@Composable
private fun SendFab(
    sendState: SendState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier.semantics {
            contentDescription = when (sendState) {
                SendState.Sending -> "Sending request"
                else -> "Send request"
            }
        },
        icon = {
            if (sendState == SendState.Sending) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        },
        text = {
            Text(
                text = if (sendState == SendState.Sending)
                    stringResource(R.string.sending)
                else
                    stringResource(R.string.send),
                style = MaterialTheme.typography.labelLarge,
            )
        },
        containerColor = if (enabled)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (enabled)
            MaterialTheme.colorScheme.onPrimaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ─── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Builder — empty draft")
@Composable
private fun BuilderContent_Empty() {
    AppTheme {
        BuilderContent(
            uiState = BuilderUiState(draftStatus = DraftStatus.Ready),
            onEvent = {},
            onNavigateToEnvironments = {},
        )
    }
}

@Preview(showBackground = true, name = "Builder — offline")
@Composable
private fun BuilderContent_Offline() {
    AppTheme {
        BuilderContent(
            uiState = BuilderUiState(
                draftStatus = DraftStatus.Ready,
                isOffline = true,
                draft = emptyDraft().copy(url = "https://api.example.com/users"),
            ),
            onEvent = {},
            onNavigateToEnvironments = {},
        )
    }
}

@Preview(showBackground = true, name = "Builder — with env + headers")
@Composable
private fun BuilderContent_WithHeaders() {
    AppTheme {
        BuilderContent(
            uiState = BuilderUiState(
                draftStatus = DraftStatus.Ready,
                activeEnvironmentName = "Staging",
                draft = emptyDraft().copy(
                    method = HttpMethod.POST,
                    url = "https://{{baseUrl}}/api/users",
                    headers = listOf(
                        KeyValue("Content-Type", "application/json"),
                        KeyValue("Authorization", "Bearer token"),
                    ),
                ),
                activeTab = BuilderTab.HEADERS,
                sendEnabled = true,
            ),
            onEvent = {},
            onNavigateToEnvironments = {},
        )
    }
}
