package com.example.requestlab.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.requestlab.R
import com.example.requestlab.core.designsystem.theme.AppTheme
import com.example.requestlab.core.designsystem.theme.spacing
import com.example.requestlab.feature.settings.domain.model.AppSettings

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val busy = uiState.busyAction != BusyAction.None

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacing.xl),
        ) {
            item {
                SettingsSectionHeader(stringResource(R.string.settings_section_requests))
            }
            item {
                TimeoutSettingRow(
                    seconds = uiState.settings.timeoutSeconds,
                    onFinished = { onEvent(SettingsEvent.OnTimeoutChanged(it)) },
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.follow_redirects_label),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.follow_redirects_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = uiState.settings.followRedirects,
                            onCheckedChange = { onEvent(SettingsEvent.OnFollowRedirectsToggled(it)) },
                        )
                    },
                )
            }
            item { HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm)) }
            item {
                SettingsSectionHeader(
                    title = stringResource(R.string.settings_section_data),
                    topPadding = true,
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.clear_history_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.clear_history_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        if (uiState.busyAction == BusyAction.ClearingHistory) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .clickable(enabled = !busy) { showClearDialog = true }
                        .semantics { role = Role.Button },
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.reset_app_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.reset_app_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        if (uiState.busyAction == BusyAction.Resetting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .clickable(enabled = !busy) { showResetDialog = true }
                        .semantics { role = Role.Button },
                )
            }
            if (uiState.actionError != null) {
                item {
                    ActionErrorBanner(
                        message = uiState.actionError,
                        onDismiss = { onEvent(SettingsEvent.OnDismissError) },
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.md,
                            vertical = MaterialTheme.spacing.sm,
                        ),
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.clear_history_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onEvent(SettingsEvent.OnClearHistoryConfirmed)
                }) {
                    Text(
                        text = stringResource(R.string.clear),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_app_confirm_title)) },
            text = { Text(stringResource(R.string.reset_app_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onEvent(SettingsEvent.OnResetConfirmed)
                }) {
                    Text(
                        text = stringResource(R.string.reset),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    topPadding: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = MaterialTheme.spacing.md,
            end = MaterialTheme.spacing.md,
            top = if (topPadding) MaterialTheme.spacing.lg else MaterialTheme.spacing.sm,
            bottom = MaterialTheme.spacing.sm,
        ),
    )
}

@Composable
private fun TimeoutSettingRow(
    seconds: Int,
    onFinished: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderValue by remember(seconds) { mutableStateOf(seconds.toFloat()) }
    val cdText = stringResource(R.string.cd_timeout_slider, sliderValue.toInt())
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.timeout_label),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${sliderValue.toInt()}s",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = stringResource(R.string.timeout_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onFinished(sliderValue.toInt()) },
            valueRange = 1f..600f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.semantics { contentDescription = cdText },
        )
    }
}

@Composable
private fun ActionErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(MaterialTheme.spacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.dismiss),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light — default settings")
@Composable
private fun SettingsContent_Light() {
    AppTheme {
        SettingsContent(uiState = SettingsUiState(), onBack = {}, onEvent = {})
    }
}

@Preview(showBackground = true, name = "Busy — clearing history")
@Composable
private fun SettingsContent_BusyClearing() {
    AppTheme {
        SettingsContent(
            uiState = SettingsUiState(busyAction = BusyAction.ClearingHistory),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Action error")
@Composable
private fun SettingsContent_ActionError() {
    AppTheme {
        SettingsContent(
            uiState = SettingsUiState(
                settings = AppSettings(timeoutSeconds = 60, followRedirects = false),
                actionError = "Couldn't clear history.",
            ),
            onBack = {},
            onEvent = {},
        )
    }
}
