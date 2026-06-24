package com.example.requestlab.feature.history.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.requestlab.R
import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.FailureKind
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.RequestBody
import com.example.requestlab.core.common.model.RequestConfig
import com.example.requestlab.core.common.model.HttpResponse
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.core.designsystem.components.BadgeStatus
import com.example.requestlab.core.designsystem.components.MethodChip
import com.example.requestlab.core.designsystem.components.MonospaceCodeBlock
import com.example.requestlab.core.designsystem.components.StatusBadge
import com.example.requestlab.core.designsystem.theme.AppTheme
import com.example.requestlab.core.designsystem.theme.spacing
import com.example.requestlab.feature.history.domain.model.HistoryDetail
import com.example.requestlab.feature.history.domain.model.HistoryEntry
import com.example.requestlab.feature.history.domain.model.HistoryStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─── Stateful screen ────────────────────────────────────────────────────────────

@Composable
fun HistoryDetailScreen(
    entryId: String,
    onReplay: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(entryId) { viewModel.init(entryId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToBuilder.collect { onReplay() }
    }

    HistoryDetailContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                HistoryDetailEvent.OnBack -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

// ─── Stateless content ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailContent(
    uiState: HistoryDetailUiState,
    onEvent: (HistoryDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(HistoryDetailEvent.OnBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
                actions = {
                    if (uiState is HistoryDetailUiState.Content) {
                        Button(
                            onClick = { onEvent(HistoryDetailEvent.OnReuseInBuilder) },
                            modifier = Modifier.padding(end = MaterialTheme.spacing.sm),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null)
                            Spacer(Modifier.width(MaterialTheme.spacing.xs))
                            Text(stringResource(R.string.reuse_in_builder))
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            HistoryDetailUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            is HistoryDetailUiState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            is HistoryDetailUiState.Content -> HistoryDetailBody(
                detail = uiState.detail,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

@Composable
private fun HistoryDetailBody(
    detail: HistoryDetail,
    modifier: Modifier = Modifier,
) {
    var headersExpanded by remember { mutableStateOf(false) }
    var responseHeadersExpanded by remember { mutableStateOf(false) }

    val timestampFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        // ── Header row ──────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                MethodChip(method = detail.request.method)
                SelectionContainer {
                    Text(
                        text = detail.entry.resolvedUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // ── Status row ───────────────────────────────────────────────────────
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                StatusBadge(status = detail.entry.status.toBadgeStatus())
                if (detail.entry.latencyMs != null) {
                    Text(
                        text = "${detail.entry.latencyMs} ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = timestampFormatter.format(detail.entry.sentAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Environment ──────────────────────────────────────────────────────
        if (detail.entry.environmentName != null) {
            item {
                Text(
                    text = stringResource(R.string.history_environment, detail.entry.environmentName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item { HorizontalDivider() }

        // ── Request section ──────────────────────────────────────────────────
        item {
            SectionHeader(
                title = stringResource(R.string.history_section_request),
                expanded = headersExpanded,
                count = detail.request.headers.size,
                onToggle = { headersExpanded = !headersExpanded },
            )
        }

        if (headersExpanded) {
            items(detail.request.headers) { header ->
                KeyValueDetailRow(kv = header)
            }
        }

        if (detail.request.params.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.history_params),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                )
            }
            items(detail.request.params) { param ->
                KeyValueDetailRow(kv = param)
            }
        }

        val requestBodyText = when (val b = detail.request.body) {
            is RequestBody.Json -> b.text
            is RequestBody.RawText -> b.text
            else -> null
        }
        if (requestBodyText != null) {
            item {
                Text(
                    text = stringResource(R.string.history_request_body),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                )
                MonospaceCodeBlock(
                    text = requestBodyText,
                    contentDescription = "Request body",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item { HorizontalDivider() }

        // ── Response section ─────────────────────────────────────────────────
        if (detail.response != null) {
            item {
                SectionHeader(
                    title = stringResource(R.string.history_section_response),
                    expanded = responseHeadersExpanded,
                    count = detail.response.headers.size,
                    onToggle = { responseHeadersExpanded = !responseHeadersExpanded },
                )
            }

            if (responseHeadersExpanded) {
                items(detail.response.headers) { header ->
                    KeyValueDetailRow(kv = header)
                }
            }

            item {
                if (detail.response.body.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.history_response_body),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                    )
                    MonospaceCodeBlock(
                        text = detail.response.body.take(20_000),
                        contentDescription = "Response body",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else if (detail.failure != null) {
            item {
                Text(
                    text = stringResource(R.string.history_section_response),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                Text(
                    text = detail.failure.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    expanded: Boolean,
    count: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (count > 0) "$title ($count)" else title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            IconButton(onClick = onToggle) {
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun KeyValueDetailRow(kv: KeyValue, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.xs),
    ) {
        Text(
            text = kv.key,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        SelectionContainer {
            Text(
                text = kv.value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(2f),
            )
        }
    }
}

private fun HistoryStatus.toBadgeStatus(): BadgeStatus = when (this) {
    is HistoryStatus.Http -> BadgeStatus.Http(code, "")
    is HistoryStatus.Failed -> BadgeStatus.Failed(reason = reason)
}

// ─── Previews ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HistoryDetailContent_200() {
    AppTheme {
        HistoryDetailContent(
            uiState = HistoryDetailUiState.Content(
                HistoryDetail(
                    entry = HistoryEntry(
                        id = "1",
                        method = "GET",
                        resolvedUrl = "https://api.example.com/users",
                        status = HistoryStatus.Http(200),
                        latencyMs = 342,
                        sentAt = Instant.now(),
                        environmentName = "Staging",
                    ),
                    request = PreparedRequest(
                        method = HttpMethod.GET,
                        resolvedUrl = "https://api.example.com/users",
                        headers = listOf(KeyValue("Accept", "application/json")),
                        params = emptyList(),
                        body = RequestBody.None,
                        auth = AuthConfig.None,
                        config = RequestConfig(30, true),
                    ),
                    response = HttpResponse(
                        statusCode = 200,
                        statusMessage = "OK",
                        headers = listOf(KeyValue("Content-Type", "application/json")),
                        body = """{"id":1,"name":"Alice"}""",
                        bodySizeBytes = 22,
                        latencyMs = 342,
                        isJson = true,
                    ),
                    failure = null,
                ),
            ),
            onEvent = {},
        )
    }
}
