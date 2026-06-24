package com.example.requestlab.feature.builder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.requestlab.R
import com.example.requestlab.core.common.model.FailureKind
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.designsystem.JsonSyntaxColors
import com.example.requestlab.core.designsystem.components.BadgeStatus
import com.example.requestlab.core.designsystem.components.MonospaceCodeBlock
import com.example.requestlab.core.designsystem.components.StatusBadge
import com.example.requestlab.core.designsystem.jsonSyntax
import com.example.requestlab.core.designsystem.theme.AppTheme
import com.example.requestlab.core.designsystem.theme.spacing

@Composable
fun ResponsePane(
    state: ResponseUiState,
    onEvent: (BuilderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when (state) {
            is ResponseUiState.Content -> {
                ResponseToolbar(
                    status = state.status,
                    latencyMs = state.latencyMs,
                    showRaw = state.showRaw,
                    onToggleRaw = { onEvent(BuilderEvent.OnToggleRawBody) },
                    onCopy = { onEvent(BuilderEvent.OnCopyBody) },
                )
                HorizontalDivider()
                ResponseContent(
                    headers = state.headers,
                    body = state.body,
                    showRaw = state.showRaw,
                    modifier = Modifier.weight(1f),
                )
            }
            ResponseUiState.Loading -> ResponseLoading(onCancel = { onEvent(BuilderEvent.OnCancelSend) })
            is ResponseUiState.Failure -> ResponseErrorCard(
                reason = state.reason,
                onRetry = { onEvent(BuilderEvent.OnRetrySend) },
            )
            ResponseUiState.Empty -> ResponseEmpty()
        }
    }
}

@Composable
private fun ResponseToolbar(
    status: BadgeStatus,
    latencyMs: Long,
    showRaw: Boolean,
    onToggleRaw: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        StatusBadge(status = status)
        AssistChip(
            onClick = {},
            label = { Text("${latencyMs} ms", style = MaterialTheme.typography.labelMedium) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.semantics {
                contentDescription = "$latencyMs milliseconds"
            },
        )
        Spacer(Modifier.weight(1f))
        FilterChip(
            selected = showRaw,
            onClick = onToggleRaw,
            label = { Text(stringResource(R.string.raw), style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.semantics { contentDescription = "Toggle raw body" },
        )
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.cd_copy_body),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ResponseContent(
    headers: List<KeyValue>,
    body: BodyUi,
    showRaw: Boolean,
    modifier: Modifier = Modifier,
) {
    var headersExpanded by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.response_headers),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { headersExpanded = !headersExpanded }) {
                    Icon(
                        imageVector = if (headersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (headersExpanded)
                            stringResource(R.string.cd_collapse_headers)
                        else
                            stringResource(R.string.cd_expand_headers),
                    )
                }
            }
        }
        if (headersExpanded) {
            items(headers) { header ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs),
                ) {
                    Text(
                        text = header.key,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.sm))
                    SelectionContainer {
                        Text(
                            text = header.value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(2f),
                        )
                    }
                }
            }
        }
        if (body is BodyUi.Truncated) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md),
                ) {
                    Text(
                        text = stringResource(R.string.body_truncated, body.totalBytes / 1024),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(MaterialTheme.spacing.sm),
                    )
                }
            }
        }
        item {
            val bodyText = when (body) {
                is BodyUi.Pretty -> body.text
                is BodyUi.Raw -> body.text
                is BodyUi.Truncated -> body.shown
            }
            if (showRaw) {
                MonospaceCodeBlock(
                    text = bodyText,
                    contentDescription = "Response body",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md),
                )
            } else {
                JsonSyntaxBody(
                    text = bodyText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md),
                )
            }
        }
    }
}

@Composable
private fun JsonSyntaxBody(
    text: String,
    modifier: Modifier = Modifier,
) {
    val syntaxColors = MaterialTheme.jsonSyntax
    SelectionContainer {
        MonospaceCodeBlock(
            text = text,
            contentDescription = "Response body",
            modifier = modifier,
        )
    }
}

@Composable
private fun ResponseLoading(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.sending),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
private fun ResponseEmpty(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            modifier = Modifier.padding(MaterialTheme.spacing.xl),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.response_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ResponseErrorCard(
    reason: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                modifier = Modifier.padding(MaterialTheme.spacing.md),
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                OutlinedButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun ResponsePane_Empty() {
    AppTheme { ResponsePane(state = ResponseUiState.Empty, onEvent = {}) }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun ResponsePane_Loading() {
    AppTheme { ResponsePane(state = ResponseUiState.Loading, onEvent = {}) }
}

@Preview(showBackground = true, name = "Failure — timeout")
@Composable
private fun ResponsePane_Failure() {
    AppTheme {
        ResponsePane(
            state = ResponseUiState.Failure("Timeout after 30 s", FailureKind.TIMEOUT),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Content 200")
@Composable
private fun ResponsePane_Content200() {
    AppTheme {
        ResponsePane(
            state = ResponseUiState.Content(
                status = BadgeStatus.Http(200, "OK"),
                latencyMs = 342,
                headers = listOf(KeyValue("Content-Type", "application/json")),
                body = BodyUi.Pretty("""{"id":1,"name":"test"}"""),
                showRaw = false,
            ),
            onEvent = {},
        )
    }
}
