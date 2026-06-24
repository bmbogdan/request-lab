# UI Spec — CollectionDetail

## Composable hierarchy

- `CollectionDetailScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `navigationIcon`: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` — emits `OnBack`
    - `title`: `Text(uiState.name)` — `typography.titleLarge`
    - `actions`:
      - `IconButton(Icons.Default.MoreVert)` — overflow `DropdownMenu`
        - `DropdownMenuItem` "Rename collection" → `OnRenameCollection`
        - `DropdownMenuItem` "Delete collection" → opens `DeleteCollectionDialog`
  - `content`:
    - Conditional based on `CollectionDetailUiState`:
      - `CollectionDetailLoading`
      - `CollectionDetailEmpty`
      - `CollectionDetailError`
      - `CollectionDetailContent`

### `CollectionDetailContent`

Uses `LazyColumn` with `ReorderableItem` from a reorder library (or custom drag-state composable) to support drag-and-drop reordering.

- `LazyColumn` (with `reorderState`)
  - `items(requests, key = { it.id })`:
    - `ReorderableItem` wrapping `SavedRequestRow`

### `SavedRequestRow`

- `ListItem`
  - `leadingContent`: `DragHandleRow` icon — `Icon(Icons.Default.DragHandle)`, `colorScheme.onSurfaceVariant`; only active when reorder mode is on (or always visible as a grip)
  - `headlineContent`: `Text(request.name)` — `typography.bodyLarge`
  - `supportingContent`: `Row`
    - `MethodChip` (small)
    - `Text(request.url)` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`, single line ellipsis
  - `trailingContent`: `IconButton(Icons.Default.MoreVert)` — opens per-item `DropdownMenu`
    - `DropdownMenuItem` "Duplicate" → `OnDuplicate(id)`
    - `DropdownMenuItem` "Remove" → `OnRemove(id)` with confirmation
  - `modifier`: `Modifier.clickable { OnRequestClicked(id) }`

### `RenameCollectionDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.rename_collection_title))`
- `text`: `OutlinedTextField` prefilled with current name
- `confirmButton`: `TextButton("Rename")` — emits `OnRenameCollection(name)`
- `dismissButton`: `TextButton("Cancel")`

### `DeleteCollectionDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.delete_collection_confirm_title))`
- `text`: `Text(stringResource(R.string.delete_collection_confirm_body))` — warns requests will be removed
- `confirmButton`: `TextButton("Delete", color = colorScheme.error)` — emits `OnDeleteCollection`
- `dismissButton`: `TextButton("Cancel")`

### `RemoveSavedRequestConfirmDialog` (AlertDialog)

- Inline confirm before emitting `OnRemove(id)`.

### `CollectionDetailLoading`

- `LazyColumn`
  - `items(4)`: `SavedRequestSkeletonRow` — shimmer `ListItem`-shaped placeholder

### `CollectionDetailEmpty`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_lg` gap)
    - Illustration placeholder `Box` (`colorScheme.surfaceVariant`, `shapes.medium`)
    - `Text(stringResource(R.string.collection_detail_empty_message))` — `typography.bodyLarge`, `colorScheme.onSurfaceVariant`, `textAlign = Center`

### `CollectionDetailError`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(uiState.message)` — `typography.bodyLarge`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetry`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `CollectionDetailScreen` (Scaffold) | — | — | `colorScheme.surface` | — | |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Back + overflow |
| `SavedRequestRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` headline / `typography.bodySmall` supporting | `colorScheme.surface` | — | Full row tappable |
| `DragHandleRow` icon | — | — | `colorScheme.onSurfaceVariant` | — | `contentDescription = stringResource(R.string.cd_drag_handle)` |
| `MethodChip` (supporting row) | `spacing_xs` | `typography.labelSmall` | Per-method | `shapes.small` | Non-interactive |
| Per-item `MoreVert` `IconButton` | — | — | `colorScheme.onSurface` | — | Opens per-item dropdown |
| `AlertDialog` (rename / delete) | `spacing_md` | `typography.titleMedium` title / `typography.bodyMedium` body | `colorScheme.surface` | `shapes.extraLarge` | Standard M3 |
| `OutlinedTextField` (rename) | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | |
| Destructive confirm `TextButton` | — | `typography.labelMedium` | `colorScheme.error` | — | |
| `SavedRequestSkeletonRow` | `spacing_md` | — | `colorScheme.surfaceVariant` | `shapes.small` | Shimmer |
| `CollectionDetailEmpty` illustration | — | — | `colorScheme.surfaceVariant` | `shapes.medium` | Placeholder |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface CollectionDetailUiState {
    data object Loading
    data object Empty
    data class Content(val name: String, val requests: List<SavedRequest>)
    data class Error(val message: String)
}
```

| State | What renders |
| --- | --- |
| `Loading` | `CollectionDetailLoading` — 4 skeleton rows; toolbar title shows skeleton placeholder. |
| `Empty` | `CollectionDetailEmpty` — illustration + "This collection is empty" message. Overflow menu still functional. |
| `Content` | `CollectionDetailContent` — reorderable `LazyColumn` of `SavedRequestRow` items. |
| `Error` | `CollectionDetailError` — error icon + message + Retry. Overflow menu hidden. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnBack` | Back arrow `IconButton`; system back gesture |
| `OnRequestClicked(id)` | Tap on a `SavedRequestRow` |
| `OnDuplicate(id)` | "Duplicate" in per-item `DropdownMenu` |
| `OnReorder(orderedIds)` | Drag-and-drop completion — reorder library callback |
| `OnRemove(id)` | "Remove" in per-item `DropdownMenu` + confirmation dialog confirm |
| `OnRenameCollection(name)` | Confirm in `RenameCollectionDialog` (triggered from overflow "Rename collection") |
| `OnDeleteCollection` | Confirm in `DeleteCollectionDialog` (triggered from overflow "Delete collection") |
| `OnRetry` | Retry button in `CollectionDetailError` |

---

## @Preview targets

On stateless `CollectionDetailContent` composable:

1. `CollectionDetailContent_LightContent` — light theme, 4 saved requests with varying methods.
2. `CollectionDetailContent_DarkContent` — dark theme.
3. `CollectionDetailContent_Empty` — light theme, empty state.
4. `CollectionDetailContent_Loading` — light theme, skeleton rows.
5. `CollectionDetailContent_Error` — light theme, error state.

---

## Accessibility

- `DragHandleRow` icon: `contentDescription = stringResource(R.string.cd_drag_handle)` + `semantics { role = Role.Button }` (drag activation via long-press or accessibility action).
- Add `CustomAccessibilityAction("Move up")` and `CustomAccessibilityAction("Move down")` on each `ReorderableItem` for keyboard/switch-access reordering.
- Per-item overflow `IconButton`: `contentDescription = stringResource(R.string.cd_item_options, request.name)`.
- "Remove" and "Delete collection" — both require confirmation dialog; screen reader should announce the confirmation dialog title.
- All `DropdownMenuItem` touch targets ≥ 48×48dp.
- Collection name in `TopAppBar` `title`: if dynamic, set `semantics { heading() }`.
- Empty-state illustration placeholder: `contentDescription = null` (decorative); description is in the text below.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.titleMedium`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`, `typography.labelSmall`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.error`, `colorScheme.secondaryContainer`
- Shape: `shapes.small`, `shapes.medium`, `shapes.extraLarge`
