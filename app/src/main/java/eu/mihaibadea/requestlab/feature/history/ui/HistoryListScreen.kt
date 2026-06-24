package eu.mihaibadea.requestlab.feature.history.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.mihaibadea.requestlab.R
import eu.mihaibadea.requestlab.core.common.model.FailureKind
import eu.mihaibadea.requestlab.core.designsystem.components.BadgeStatus
import eu.mihaibadea.requestlab.core.designsystem.components.StatusBadge
import eu.mihaibadea.requestlab.core.designsystem.components.MethodChip
import eu.mihaibadea.requestlab.core.designsystem.theme.AppTheme
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryEntry
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ─── Stateful screen ────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryListContent(
        uiState = uiState,
        onEvent = { event ->
            if (event is HistoryListEvent.OnEntryClicked) {
                onNavigateToDetail(event.id)
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

// ─── Stateless content ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListContent(
    uiState: HistoryListUiState,
    onEvent: (HistoryListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOverflow by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                actions = {
                    if (uiState is HistoryListUiState.Content) {
                        Box {
                            IconButton(onClick = { showOverflow = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_overflow_menu))
                            }
                            DropdownMenu(
                                expanded = showOverflow,
                                onDismissRequest = { showOverflow = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.clear_history_label)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        showOverflow = false
                                        onEvent(HistoryListEvent.OnRequestClearAll)
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            HistoryListUiState.Loading -> Unit
            HistoryListUiState.Empty -> HistoryEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            is HistoryListUiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = MaterialTheme.spacing.md,
                        vertical = MaterialTheme.spacing.sm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    items(
                        items = uiState.entries,
                        key = { it.id },
                    ) { entry ->
                        HistoryEntryRow(
                            entry = entry,
                            onClick = { onEvent(HistoryListEvent.OnEntryClicked(entry.id)) },
                            onDelete = { onEvent(HistoryListEvent.OnDeleteEntry(entry.id)) },
                        )
                    }
                }

                if (uiState.showClearConfirm) {
                    AlertDialog(
                        onDismissRequest = { onEvent(HistoryListEvent.OnDismissClearConfirm) },
                        title = { Text(stringResource(R.string.clear_history_confirm_title)) },
                        text = { Text(stringResource(R.string.clear_history_confirm_body)) },
                        confirmButton = {
                            TextButton(onClick = { onEvent(HistoryListEvent.OnConfirmClearAll) }) {
                                Text(stringResource(R.string.clear))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { onEvent(HistoryListEvent.OnDismissClearConfirm) }) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.padding(MaterialTheme.spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.history_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryEntryRow(
    entry: HistoryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            eu.mihaibadea.requestlab.core.common.model.HttpMethod.entries
                .firstOrNull { it.name == entry.method }
                ?.let { method ->
                    MethodChip(method = method)
                }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.resolvedUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(status = entry.status.toBadgeStatus())
                    if (entry.latencyMs != null) {
                        Text(
                            text = "${entry.latencyMs} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = entry.sentAt.formatRelative(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (entry.environmentName != null) {
                    Text(
                        text = entry.environmentName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_entry),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun HistoryStatus.toBadgeStatus(): BadgeStatus = when (this) {
    is HistoryStatus.Http -> BadgeStatus.Http(code, "")
    is HistoryStatus.Failed -> BadgeStatus.Failed(reason = reason)
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

private fun Instant.formatRelative(): String {
    val zone = ZoneId.systemDefault()
    val local = atZone(zone)
    val now = java.time.ZonedDateTime.now(zone)
    return if (local.toLocalDate() == now.toLocalDate()) {
        local.format(timeFormatter)
    } else {
        local.format(dateFormatter)
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "History — list")
@Composable
private fun HistoryListContent_WithEntries() {
    AppTheme {
        HistoryListContent(
            uiState = HistoryListUiState.Content(
                entries = listOf(
                    HistoryEntry(
                        id = "1",
                        method = "GET",
                        resolvedUrl = "https://api.example.com/users",
                        status = HistoryStatus.Http(200),
                        latencyMs = 342,
                        sentAt = Instant.now(),
                        environmentName = "Staging",
                    ),
                    HistoryEntry(
                        id = "2",
                        method = "POST",
                        resolvedUrl = "https://api.example.com/users",
                        status = HistoryStatus.Http(201),
                        latencyMs = 512,
                        sentAt = Instant.now().minusSeconds(3600),
                        environmentName = null,
                    ),
                    HistoryEntry(
                        id = "3",
                        method = "DELETE",
                        resolvedUrl = "https://api.example.com/users/1",
                        status = HistoryStatus.Failed(FailureKind.TIMEOUT, "Timeout after 30s"),
                        latencyMs = null,
                        sentAt = Instant.now().minusSeconds(7200),
                        environmentName = null,
                    ),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "History — empty")
@Composable
private fun HistoryListContent_Empty() {
    AppTheme {
        HistoryListContent(
            uiState = HistoryListUiState.Empty,
            onEvent = {},
        )
    }
}
