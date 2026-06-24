package eu.mihaibadea.requestlab.feature.builder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import eu.mihaibadea.requestlab.feature.collections.domain.model.Collection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveToCollectionSheet(
    onDismiss: () -> Unit,
    viewModel: SaveToCollectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dismissSignal.collect { onDismiss() }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        SaveToCollectionSheetContent(
            uiState = uiState,
            onEvent = { event ->
                when (event) {
                    SaveSheetEvent.OnDismiss -> onDismiss()
                    else -> viewModel.onEvent(event)
                }
            },
        )
    }
}

@Composable
fun SaveToCollectionSheetContent(
    uiState: SaveSheetUiState,
    onEvent: (SaveSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.save_to_collection_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm),
        )
        HorizontalDivider()
        Spacer(Modifier.height(MaterialTheme.spacing.sm))

        when (uiState) {
            SaveSheetUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MaterialTheme.spacing.xl * 3),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is SaveSheetUiState.Empty -> {
                Text(
                    text = stringResource(R.string.no_collections_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(MaterialTheme.spacing.sm))
                NewCollectionInputRow(
                    newName = "",
                    selectedNew = false,
                    onNameChange = { onEvent(SaveSheetEvent.OnNewCollectionNameChanged(it)) },
                    onSelect = { onEvent(SaveSheetEvent.OnSelectCollection(null)) },
                )
            }
            is SaveSheetUiState.Content -> {
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(uiState.collections) { collection ->
                        CollectionRadioRow(
                            collection = collection,
                            selected = uiState.selectedId == collection.id,
                            onClick = { onEvent(SaveSheetEvent.OnSelectCollection(collection.id)) },
                        )
                    }
                    item {
                        NewCollectionInputRow(
                            newName = uiState.newName,
                            selectedNew = uiState.selectedId == null && uiState.newName.isNotBlank(),
                            onNameChange = { onEvent(SaveSheetEvent.OnNewCollectionNameChanged(it)) },
                            onSelect = { onEvent(SaveSheetEvent.OnSelectCollection(null)) },
                        )
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm))

        val requestName = when (uiState) {
            is SaveSheetUiState.Content -> uiState.requestName
            is SaveSheetUiState.Empty -> uiState.requestName
            else -> ""
        }
        OutlinedTextField(
            value = requestName,
            onValueChange = { onEvent(SaveSheetEvent.OnRequestNameChanged(it)) },
            label = { Text(stringResource(R.string.request_name_label)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
        )

        val errorMsg = (uiState as? SaveSheetUiState.Content)?.error
        if (errorMsg != null) {
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = MaterialTheme.spacing.sm)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }

        Spacer(Modifier.height(MaterialTheme.spacing.sm))

        val isSaving = (uiState as? SaveSheetUiState.Content)?.saving == true
        val saveEnabled = !isSaving && when (uiState) {
            is SaveSheetUiState.Content -> uiState.selectedId != null || uiState.newName.isNotBlank()
            is SaveSheetUiState.Empty -> false
            else -> false
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { onEvent(SaveSheetEvent.OnDismiss) }) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Button(
                onClick = { onEvent(SaveSheetEvent.OnSaveConfirmed) },
                enabled = saveEnabled,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(18.dp).width(18.dp))
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        }
        Spacer(Modifier.height(MaterialTheme.spacing.md))
    }
}

@Composable
private fun CollectionRadioRow(
    collection: Collection,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(MaterialTheme.spacing.sm))
            Text(text = collection.name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun NewCollectionInputRow(
    newName: String,
    selectedNew: Boolean,
    onNameChange: (String) -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selectedNew, onClick = onSelect)
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        OutlinedTextField(
            value = newName,
            onValueChange = {
                onNameChange(it)
                if (it.isNotBlank()) onSelect()
            },
            label = { Text(stringResource(R.string.new_collection_label)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, name = "Content — 3 collections")
@Composable
private fun SaveSheetContent_LightContent() {
    AppTheme {
        SaveToCollectionSheetContent(
            uiState = SaveSheetUiState.Content(
                collections = listOf(
                    Collection("1", "Auth Tests", 3, 0),
                    Collection("2", "Users API", 5, 1),
                    Collection("3", "Payments", 2, 2),
                ),
                selectedId = "1",
                newName = "",
                requestName = "GET /users",
                saving = false,
                error = null,
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun SaveSheetContent_Empty() {
    AppTheme {
        SaveToCollectionSheetContent(
            uiState = SaveSheetUiState.Empty(requestName = "GET /users"),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun SaveSheetContent_Loading() {
    AppTheme {
        SaveToCollectionSheetContent(uiState = SaveSheetUiState.Loading, onEvent = {})
    }
}
