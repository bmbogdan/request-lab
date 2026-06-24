package eu.mihaibadea.requestlab.feature.environments.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.mihaibadea.requestlab.R
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentSwitcherSheet(
    onDismiss: () -> Unit,
    onManageEnvironments: () -> Unit,
    viewModel: EnvironmentSwitcherViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = MaterialTheme.spacing.lg)) {
            Text(
                text = stringResource(R.string.switcher_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.md,
                    vertical = MaterialTheme.spacing.sm,
                ),
            )
            HorizontalDivider()
            LazyColumn {
                item {
                    SwitcherRow(
                        name = stringResource(R.string.no_environment),
                        selected = uiState.activeId == null,
                        onClick = {
                            viewModel.onEvent(SwitcherEvent.OnSelect(null))
                            onDismiss()
                        },
                    )
                }
                items(uiState.environments) { env ->
                    SwitcherRow(
                        name = env.name,
                        selected = uiState.activeId == env.id,
                        onClick = {
                            viewModel.onEvent(SwitcherEvent.OnSelect(env.id))
                            onDismiss()
                        },
                    )
                }
            }
            HorizontalDivider()
            TextButton(
                onClick = {
                    viewModel.onEvent(SwitcherEvent.OnManageEnvironments)
                    onManageEnvironments()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.md),
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(MaterialTheme.spacing.sm))
                Text(stringResource(R.string.manage_environments))
            }
        }
    }
}

@Composable
private fun SwitcherRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}
