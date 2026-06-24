package eu.mihaibadea.requestlab.feature.environments.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.mihaibadea.requestlab.R
import eu.mihaibadea.requestlab.core.designsystem.theme.AppTheme
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing
import eu.mihaibadea.requestlab.feature.environments.domain.model.Variable

@Composable
fun EnvironmentDetailScreen(
    environmentId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EnvironmentDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(environmentId) { viewModel.init(environmentId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showUnsavedDialog by remember { mutableStateOf(false) }
    val isDirty = uiState is EnvironmentDetailUiState.Content

    EnvironmentDetailContent(
        uiState = uiState,
        onBack = {
            if (isDirty) showUnsavedDialog = true else onBack()
        },
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_body)) },
            confirmButton = {
                TextButton(onClick = { showUnsavedDialog = false; onBack() }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text(stringResource(R.string.keep_editing))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentDetailContent(
    uiState: EnvironmentDetailUiState,
    onBack: () -> Unit,
    onEvent: (EnvironmentDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? EnvironmentDetailUiState.Content
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.environment_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (content != null) {
                        if (content.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = MaterialTheme.spacing.md),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            TextButton(onClick = { onEvent(EnvironmentDetailEvent.OnSave) }) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is EnvironmentDetailUiState.Loading ->
                EnvironmentDetailLoading(Modifier.padding(innerPadding))
            is EnvironmentDetailUiState.Content ->
                EnvironmentDetailForm(
                    state = uiState,
                    onEvent = onEvent,
                    modifier = Modifier.padding(innerPadding),
                )
            is EnvironmentDetailUiState.Error ->
                EnvironmentDetailError(
                    message = uiState.message,
                    onRetry = { onEvent(EnvironmentDetailEvent.OnRetry) },
                    modifier = Modifier.padding(innerPadding),
                )
        }
    }
}

@Composable
private fun EnvironmentDetailForm(
    state: EnvironmentDetailUiState.Content,
    onEvent: (EnvironmentDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(EnvironmentDetailEvent.OnNameChanged(it)) },
                label = { Text(stringResource(R.string.environment_name_label)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.md,
                        vertical = MaterialTheme.spacing.sm,
                    ),
            )
        }
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(MaterialTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.env_encrypted_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Text(
                text = "Variables",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.md,
                    vertical = MaterialTheme.spacing.sm,
                ),
            )
        }
        itemsIndexed(state.rows, key = { i, _ -> i }) { index, variable ->
            VariableRow(
                variable = variable,
                isError = index in state.validationErrors,
                errorMessage = state.validationErrors[index],
                onEdit = { onEvent(EnvironmentDetailEvent.OnVariableEdited(index, it)) },
                onDelete = { onEvent(EnvironmentDetailEvent.OnDeleteVariable(index)) },
            )
        }
        item {
            TextButton(
                onClick = { onEvent(EnvironmentDetailEvent.OnAddVariable) },
                modifier = Modifier.padding(MaterialTheme.spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.add_variable),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (state.saveError != null) {
            item {
                Text(
                    text = state.saveError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(
                            horizontal = MaterialTheme.spacing.md,
                            vertical = MaterialTheme.spacing.sm,
                        )
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}

@Composable
private fun VariableRow(
    variable: Variable,
    isError: Boolean,
    errorMessage: String?,
    onEdit: (Variable) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = variable.key,
                onValueChange = { onEdit(variable.copy(key = it)) },
                label = { Text(stringResource(R.string.variable_key_label)) },
                singleLine = true,
                isError = isError,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            OutlinedTextField(
                value = variable.value,
                onValueChange = { onEdit(variable.copy(value = it)) },
                label = { Text(stringResource(R.string.variable_value_label)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete_variable, variable.key),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
            )
        }
    }
}

@Composable
private fun EnvironmentDetailLoading(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.md,
                        vertical = MaterialTheme.spacing.sm,
                    )
                    .fillMaxWidth()
                    .height(56.dp),
            )
        }
        items(4) {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.md,
                        vertical = MaterialTheme.spacing.xs,
                    )
                    .fillMaxWidth()
                    .height(48.dp),
            )
        }
    }
}

@Composable
private fun EnvironmentDetailError(
    message: String,
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
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Preview(showBackground = true, name = "Content — 3 variables")
@Composable
private fun EnvironmentDetailContent_LightContent() {
    AppTheme {
        EnvironmentDetailContent(
            uiState = EnvironmentDetailUiState.Content(
                name = "Production",
                rows = listOf(
                    Variable("baseUrl", "https://api.example.com"),
                    Variable("apiKey", "secret123"),
                    Variable("timeout", "30"),
                ),
                validationErrors = emptyMap(),
                saving = false,
                saveError = null,
            ),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Validation error")
@Composable
private fun EnvironmentDetailContent_ValidationError() {
    AppTheme {
        EnvironmentDetailContent(
            uiState = EnvironmentDetailUiState.Content(
                name = "Staging",
                rows = listOf(
                    Variable("baseUrl", "https://staging.example.com"),
                    Variable("baseUrl", "duplicate"),
                ),
                validationErrors = mapOf(1 to "Key already exists"),
                saving = false,
                saveError = null,
            ),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Error state")
@Composable
private fun EnvironmentDetailContent_Error() {
    AppTheme {
        EnvironmentDetailContent(
            uiState = EnvironmentDetailUiState.Error("Couldn't load environment."),
            onBack = {},
            onEvent = {},
        )
    }
}
