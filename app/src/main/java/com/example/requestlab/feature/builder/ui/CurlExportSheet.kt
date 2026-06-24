package com.example.requestlab.feature.builder.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.requestlab.R
import com.example.requestlab.core.designsystem.components.MonospaceCodeBlock
import com.example.requestlab.core.designsystem.theme.AppTheme
import com.example.requestlab.core.designsystem.theme.spacing
import com.example.requestlab.feature.builder.domain.model.CurlCommand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurlExportSheet(
    onDismiss: () -> Unit,
    viewModel: CurlExportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.copySignal.collect { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("cURL command", text))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.shareSignal.collect { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = BottomSheetDefaults.Elevation,
    ) {
        CurlExportSheetContent(
            uiState = uiState,
            onEvent = { event ->
                when (event) {
                    CurlEvent.OnDismiss -> onDismiss()
                    else -> viewModel.onEvent(event)
                }
            },
        )
    }
}

@Composable
fun CurlExportSheetContent(
    uiState: CurlUiState,
    onEvent: (CurlEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.curl_export_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onEvent(CurlEvent.OnDismiss) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close_sheet),
                )
            }
        }
        HorizontalDivider()
        when (uiState) {
            CurlUiState.Generating -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MaterialTheme.spacing.xl * 4),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is CurlUiState.Content -> {
                Spacer(Modifier.height(MaterialTheme.spacing.sm))

                if (uiState.unresolvedWarning != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { liveRegion = LiveRegionMode.Polite },
                    ) {
                        Row(
                            modifier = Modifier.padding(MaterialTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = uiState.unresolvedWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.sm))
                }

                MonospaceCodeBlock(
                    text = uiState.command.text,
                    contentDescription = stringResource(R.string.cd_curl_command),
                    modifier = Modifier.fillMaxWidth(),
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.include_credentials_label),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(R.string.include_credentials_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.includeCredentials,
                        onCheckedChange = { onEvent(CurlEvent.OnToggleIncludeCredentials) },
                    )
                }

                HorizontalDivider()
                Spacer(Modifier.height(MaterialTheme.spacing.sm))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.md),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    OutlinedButton(
                        onClick = { onEvent(CurlEvent.OnCopy) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(MaterialTheme.spacing.xs))
                        Text(stringResource(R.string.copy))
                    }
                    FilledTonalButton(
                        onClick = { onEvent(CurlEvent.OnShare) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(Modifier.width(MaterialTheme.spacing.xs))
                        Text(stringResource(R.string.share))
                    }
                }
                Spacer(Modifier.height(MaterialTheme.spacing.md))
            }
        }
    }
}

@Preview(showBackground = true, name = "Content — no credentials")
@Composable
private fun CurlExportSheet_LightContent() {
    AppTheme {
        CurlExportSheetContent(
            uiState = CurlUiState.Content(
                command = CurlCommand(
                    text = "curl 'https://api.example.com/users' \\\n  -H 'Accept: application/json'",
                    unresolvedVariables = emptyList(),
                ),
                includeCredentials = false,
                unresolvedWarning = null,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "With unresolved warning")
@Composable
private fun CurlExportSheet_WithWarning() {
    AppTheme {
        CurlExportSheetContent(
            uiState = CurlUiState.Content(
                command = CurlCommand(
                    text = "curl '{{baseUrl}}/users'",
                    unresolvedVariables = listOf("baseUrl"),
                ),
                includeCredentials = false,
                unresolvedWarning = "Unresolved variables: baseUrl",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Generating")
@Composable
private fun CurlExportSheet_Generating() {
    AppTheme {
        CurlExportSheetContent(uiState = CurlUiState.Generating, onEvent = {})
    }
}
