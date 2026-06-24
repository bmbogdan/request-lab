package com.example.requestlab.feature.environments.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.requestlab.R
import com.example.requestlab.core.designsystem.theme.AppTheme
import com.example.requestlab.core.designsystem.theme.spacing
import com.example.requestlab.feature.environments.domain.model.Environment

@Composable
fun EnvironmentsScreen(
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EnvironmentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    EnvironmentsContent(
        uiState = uiState,
        onBack = onBack,
        onEvent = { event ->
            if (event is EnvironmentsEvent.OnEnvironmentClicked) {
                onNavigateToDetail(event.id)
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentsContent(
    uiState: EnvironmentsUiState,
    onBack: () -> Unit,
    onEvent: (EnvironmentsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showNewDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.environments_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState !is EnvironmentsUiState.Error) {
                FloatingActionButton(onClick = { showNewDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_new_environment),
                    )
                }
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is EnvironmentsUiState.Loading -> EnvironmentsLoadingContent(Modifier.padding(innerPadding))
            is EnvironmentsUiState.Empty -> EnvironmentsEmptyContent(
                onNewEnvironment = { showNewDialog = true },
                modifier = Modifier.padding(innerPadding),
            )
            is EnvironmentsUiState.Content -> EnvironmentsListContent(
                rows = uiState.rows,
                onEvent = onEvent,
                modifier = Modifier.padding(innerPadding),
            )
            is EnvironmentsUiState.Error -> EnvironmentsErrorContent(
                onRetry = { onEvent(EnvironmentsEvent.OnRetry) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (showNewDialog) {
        NewEnvironmentDialog(
            onConfirm = { name ->
                onEvent(EnvironmentsEvent.OnNewEnvironment(name))
                showNewDialog = false
            },
            onDismiss = { showNewDialog = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EnvironmentsListContent(
    rows: List<Environment>,
    onEvent: (EnvironmentsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuState by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<Environment?>(null) }
    var deleteTarget by remember { mutableStateOf<Environment?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.md,
            vertical = MaterialTheme.spacing.sm,
        ),
    ) {
        items(rows, key = { it.id }) { env ->
            Box {
                ListItem(
                    headlineContent = {
                        Text(env.name, style = MaterialTheme.typography.bodyLarge)
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.variable_count, env.variableCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = {
                        if (env.isActive) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.cd_active_env),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { onEvent(EnvironmentsEvent.OnEnvironmentClicked(env.id)) },
                            onLongClick = { menuState = env.id },
                        )
                        .semantics {
                            customActions = listOf(
                                CustomAccessibilityAction("Rename") { renameTarget = env; true },
                                CustomAccessibilityAction("Delete") { deleteTarget = env; true },
                            )
                        },
                )
                DropdownMenu(
                    expanded = menuState == env.id,
                    onDismissRequest = { menuState = null },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        onClick = { menuState = null; renameTarget = env },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = { menuState = null; deleteTarget = env },
                    )
                }
            }
        }
    }

    renameTarget?.let { env ->
        RenameEnvironmentDialog(
            currentName = env.name,
            onConfirm = { name ->
                onEvent(EnvironmentsEvent.OnRename(env.id, name))
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }

    deleteTarget?.let { env ->
        DeleteEnvironmentDialog(
            name = env.name,
            onConfirm = {
                onEvent(EnvironmentsEvent.OnDelete(env.id))
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun EnvironmentsLoadingContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(3) {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.md,
                        vertical = MaterialTheme.spacing.sm,
                    )
                    .sizeIn(minWidth = 200.dp, minHeight = 56.dp),
            )
        }
    }
}

@Composable
private fun EnvironmentsEmptyContent(
    onNewEnvironment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Text(
                text = stringResource(R.string.environments_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.xl),
            )
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            FilledTonalButton(onClick = onNewEnvironment) {
                Text(stringResource(R.string.new_environment))
            }
        }
    }
}

@Composable
private fun EnvironmentsErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Text(
                text = stringResource(R.string.environments_error_message),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun NewEnvironmentDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_environment_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.environment_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun RenameEnvironmentDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.environment_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun DeleteEnvironmentDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_environment_confirm_title)) },
        text = { Text(stringResource(R.string.delete_environment_confirm_body, name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Preview(showBackground = true, name = "Light — content")
@Composable
private fun EnvironmentsContent_LightContent() {
    AppTheme {
        EnvironmentsContent(
            uiState = EnvironmentsUiState.Content(
                rows = listOf(
                    Environment("1", "Production", isActive = true, variableCount = 5),
                    Environment("2", "Staging", isActive = false, variableCount = 3),
                ),
                activeId = "1",
            ),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun EnvironmentsContent_Empty() {
    AppTheme { EnvironmentsContent(EnvironmentsUiState.Empty, onBack = {}, onEvent = {}) }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun EnvironmentsContent_Error() {
    AppTheme {
        EnvironmentsContent(
            EnvironmentsUiState.Error("Couldn't load environments."),
            onBack = {},
            onEvent = {},
        )
    }
}
