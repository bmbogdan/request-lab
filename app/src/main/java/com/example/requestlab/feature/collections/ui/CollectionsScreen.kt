@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.requestlab.feature.collections.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.requestlab.R
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.designsystem.components.MethodChip
import com.example.requestlab.core.designsystem.theme.AppTheme
import com.example.requestlab.core.designsystem.theme.spacing
import com.example.requestlab.feature.collections.domain.model.Collection
import com.example.requestlab.feature.collections.domain.model.SavedRequest

// ─── Collections list screen ────────────────────────────────────────────────────

@Composable
fun CollectionsScreen(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionsListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CollectionsListContent(
        uiState = uiState,
        onEvent = { event ->
            if (event is CollectionsListEvent.OnCollectionClicked) {
                onNavigateToDetail(event.id)
            } else {
                viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun CollectionsListContent(
    uiState: CollectionsListUiState,
    onEvent: (CollectionsListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.collections_title)) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(CollectionsListEvent.OnRequestNewCollection) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_collection)) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            CollectionsListUiState.Loading -> Unit
            CollectionsListUiState.Empty -> CollectionsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            is CollectionsListUiState.Content -> {
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
                    items(uiState.collections, key = { it.id }) { collection ->
                        CollectionRow(
                            collection = collection,
                            onClick = { onEvent(CollectionsListEvent.OnCollectionClicked(collection.id)) },
                            onRename = { onEvent(CollectionsListEvent.OnRequestRename(collection.id, collection.name)) },
                            onDelete = { onEvent(CollectionsListEvent.OnRequestDelete(collection.id, collection.name)) },
                        )
                    }
                }
            }
        }
    }

    val dialog = (uiState as? CollectionsListUiState.Content)?.dialog
    when (dialog) {
        is CollectionsDialog.NewCollection -> NameInputDialog(
            title = stringResource(R.string.new_collection_dialog_title),
            value = dialog.nameInput,
            onValueChange = { onEvent(CollectionsListEvent.OnNewCollectionNameChanged(it)) },
            onConfirm = { onEvent(CollectionsListEvent.OnConfirmNewCollection) },
            onDismiss = { onEvent(CollectionsListEvent.OnDismissDialog) },
        )
        is CollectionsDialog.Rename -> NameInputDialog(
            title = stringResource(R.string.rename_collection_title),
            value = dialog.nameInput,
            onValueChange = { onEvent(CollectionsListEvent.OnRenameNameChanged(it)) },
            onConfirm = { onEvent(CollectionsListEvent.OnConfirmRename) },
            onDismiss = { onEvent(CollectionsListEvent.OnDismissDialog) },
        )
        is CollectionsDialog.DeleteConfirm -> AlertDialog(
            onDismissRequest = { onEvent(CollectionsListEvent.OnDismissDialog) },
            title = { Text(stringResource(R.string.delete_collection_title)) },
            text = { Text(stringResource(R.string.delete_collection_body, dialog.name)) },
            confirmButton = {
                TextButton(onClick = { onEvent(CollectionsListEvent.OnConfirmDelete) }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CollectionsListEvent.OnDismissDialog) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
        null -> Unit
    }
}

@Composable
private fun CollectionsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            modifier = Modifier.padding(MaterialTheme.spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.collections_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CollectionRow(
    collection: Collection,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
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
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.request_count, collection.requestCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_overflow_menu))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { showMenu = false; onRename() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { showMenu = false; onDelete() },
                    )
                }
            }
        }
    }
}

// ─── Collection detail screen ────────────────────────────────────────────────────

@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onOpenRequest: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(collectionId) { viewModel.init(collectionId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.navigateToBuilder.collect { onOpenRequest() } }
    LaunchedEffect(Unit) { viewModel.navigateBack.collect { onBack() } }

    CollectionDetailContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                CollectionDetailEvent.OnBack -> onBack()
                else -> viewModel.onEvent(event)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun CollectionDetailContent(
    uiState: CollectionDetailUiState,
    onEvent: (CollectionDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOverflow by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            val title = (uiState as? CollectionDetailUiState.Content)?.collection?.name
                ?: stringResource(R.string.collection_detail_title)
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CollectionDetailEvent.OnBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back),
                        )
                    }
                },
                actions = {
                    if (uiState is CollectionDetailUiState.Content) {
                        Box {
                            IconButton(onClick = { showOverflow = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_overflow_menu))
                            }
                            DropdownMenu(
                                expanded = showOverflow,
                                onDismissRequest = { showOverflow = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.rename)) },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        showOverflow = false
                                        onEvent(CollectionDetailEvent.OnRequestRename)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        showOverflow = false
                                        onEvent(CollectionDetailEvent.OnRequestDeleteCollection)
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
            CollectionDetailUiState.Loading -> Unit
            is CollectionDetailUiState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }
            is CollectionDetailUiState.Content -> {
                if (uiState.requests.isEmpty()) {
                    CollectionEmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                } else {
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
                        items(uiState.requests, key = { it.id }) { request ->
                            SavedRequestRow(
                                request = request,
                                onOpen = { onEvent(CollectionDetailEvent.OnOpenRequest(request.id)) },
                                onDelete = { onEvent(CollectionDetailEvent.OnDeleteRequest(request.id)) },
                            )
                        }
                    }
                }
            }
        }
    }

    val dialog = (uiState as? CollectionDetailUiState.Content)?.dialog
    when (dialog) {
        is CollectionDetailDialog.Rename -> NameInputDialog(
            title = stringResource(R.string.rename_collection_title),
            value = dialog.nameInput,
            onValueChange = { onEvent(CollectionDetailEvent.OnRenameNameChanged(it)) },
            onConfirm = { onEvent(CollectionDetailEvent.OnConfirmRename) },
            onDismiss = { onEvent(CollectionDetailEvent.OnDismissDialog) },
        )
        CollectionDetailDialog.DeleteCollectionConfirm -> AlertDialog(
            onDismissRequest = { onEvent(CollectionDetailEvent.OnDismissDialog) },
            title = { Text(stringResource(R.string.delete_collection_title)) },
            text = { Text(stringResource(R.string.delete_collection_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { onEvent(CollectionDetailEvent.OnConfirmDeleteCollection) }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CollectionDetailEvent.OnDismissDialog) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
        null -> Unit
    }
}

@Composable
private fun CollectionEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.collection_requests_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SavedRequestRow(
    request: SavedRequest,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            MethodChip(method = request.method)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = request.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.open_in_builder),
                    tint = MaterialTheme.colorScheme.primary,
                )
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

// ─── Shared dialog ────────────────────────────────────────────────────────────────

@Composable
private fun NameInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(stringResource(R.string.collection_name_label)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = value.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Collections — list")
@Composable
private fun CollectionsListContent_WithData() {
    AppTheme {
        CollectionsListContent(
            uiState = CollectionsListUiState.Content(
                collections = listOf(
                    Collection("1", "Auth Tests", 3, 0),
                    Collection("2", "Users API", 7, 1),
                ),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Collections — empty")
@Composable
private fun CollectionsListContent_Empty() {
    AppTheme {
        CollectionsListContent(uiState = CollectionsListUiState.Empty, onEvent = {})
    }
}

@Preview(showBackground = true, name = "Collection detail — with requests")
@Composable
private fun CollectionDetailContent_WithRequests() {
    AppTheme {
        CollectionDetailContent(
            uiState = CollectionDetailUiState.Content(
                collection = Collection("1", "Users API", 2, 0),
                requests = listOf(
                    SavedRequest("r1", "1", "Get all users", HttpMethod.GET, "https://api.example.com/users", 0),
                    SavedRequest("r2", "1", "Create user", HttpMethod.POST, "https://api.example.com/users", 1),
                ),
            ),
            onEvent = {},
        )
    }
}
