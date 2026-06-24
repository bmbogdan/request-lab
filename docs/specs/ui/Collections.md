# UI Spec — Collections

## Composable hierarchy

- `CollectionsScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `title`: `Text(stringResource(R.string.collections_title))` — `typography.titleLarge`
    - `actions`: `EnvironmentChip` + `IconButton(Icons.Default.MoreVert)` overflow → Settings
  - `content`:
    - Conditional based on `CollectionsUiState`:
      - `CollectionsLoadingContent`
      - `CollectionsEmptyContent`
      - `CollectionsErrorContent`
      - `CollectionsListContent`
  - `floatingActionButton`: `FloatingActionButton`
    - `Icon(Icons.Default.Add, stringResource(R.string.cd_new_collection))`
    - Visible in all non-error states; emits `OnNewCollection` which opens an inline `AlertDialog`

### `CollectionsListContent`

- `LazyColumn`
  - `items(rows, key = { it.id })`:
    - Each item: `SwipeToDismissBox` wrapping `CollectionRow`
      - Leading swipe background: `Box` with `colorScheme.secondaryContainer` + `Icon(Icons.Default.Edit)` → emits `OnRename(id, ...)`
      - Trailing swipe background: `Box` with `colorScheme.errorContainer` + `Icon(Icons.Default.Delete)` → emits `OnDelete(id)`
      - Foreground: `CollectionRow`

### `CollectionRow`

- `ListItem`
  - `leadingContent`: `Icon(Icons.Default.Folder, contentDescription = null)` — `colorScheme.primary`
  - `headlineContent`: `Text(collection.name)` — `typography.bodyLarge`
  - `trailingContent`: `CollectionCountBadge` — `Badge`-style composable showing `collection.requestCount`
  - `modifier`: `Modifier.clickable { OnCollectionClicked(id) }` + `combinedClickable` long-press → `DropdownMenu`

### Long-press `DropdownMenu` on `CollectionRow`

- `DropdownMenuItem` "Rename" → opens rename `AlertDialog`
- `DropdownMenuItem` "Delete" → emits `OnDelete(id)` with confirmation

### `NewCollectionDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.new_collection_title))`
- `text`: `OutlinedTextField(label = stringResource(R.string.collection_name_label))`
- `confirmButton`: `TextButton("Create")` — emits `OnNewCollection(name)` if name non-empty
- `dismissButton`: `TextButton("Cancel")`

### `RenameCollectionDialog` (AlertDialog)

- Same structure as `NewCollectionDialog` with prefilled name; confirm emits `OnRename(id, name)`

### `DeleteConfirmationDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.delete_collection_confirm_title))`
- `text`: `Text(stringResource(R.string.delete_collection_confirm_body, name))`
- `confirmButton`: `TextButton("Delete", color = colorScheme.error)`
- `dismissButton`: `TextButton("Cancel")`

### `CollectionsLoadingContent`

- `LazyColumn`
  - `items(4)`: `CollectionSkeletonRow` — shimmer `ListItem`-shaped placeholder (`colorScheme.surfaceVariant`, `shapes.small`)

### `CollectionsEmptyContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_lg` gap)
    - Illustration placeholder `Box` (`colorScheme.surfaceVariant`, `shapes.medium`)
    - `Text(stringResource(R.string.collections_empty_message))` — `typography.titleMedium`, `colorScheme.onSurfaceVariant`, `textAlign = Center`
    - `FilledTonalButton(stringResource(R.string.new_collection))` — emits `OnNewCollection`

### `CollectionsErrorContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(stringResource(R.string.collections_error_message))` — `typography.bodyLarge`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetry`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `CollectionsScreen` (Scaffold) | — | — | `colorScheme.surface` | — | |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | |
| `CollectionRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` | `colorScheme.surface` | — | Tap → detail; long-press → menu |
| Folder `Icon` (leading) | — | — | `colorScheme.primary` | — | `contentDescription = null` (decorative) |
| `CollectionCountBadge` | `spacing_xs` | `typography.labelSmall` | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` | `shapes.small` | Shows integer count |
| `SwipeToDismissBox` rename bg | — | — | `colorScheme.secondaryContainer` | — | Edit icon `colorScheme.onSecondaryContainer` |
| `SwipeToDismissBox` delete bg | — | — | `colorScheme.errorContainer` | — | Delete icon `colorScheme.onErrorContainer` |
| `FloatingActionButton` | — | — | `colorScheme.primary` / `colorScheme.onPrimary` | `shapes.large` | Add icon |
| `AlertDialog` | `spacing_md` | `typography.titleMedium` title / `typography.bodyMedium` body | `colorScheme.surface` | `shapes.extraLarge` | Standard M3 dialog |
| `OutlinedTextField` (dialog) | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | |
| Confirm `TextButton` (delete) | — | `typography.labelMedium` | `colorScheme.error` | — | Destructive action red tint |
| `CollectionSkeletonRow` | `spacing_md` | — | `colorScheme.surfaceVariant` | `shapes.small` | Shimmer |
| `CollectionsEmptyContent` illustration | — | — | `colorScheme.surfaceVariant` | `shapes.medium` | Placeholder |
| `FilledTonalButton` "New collection" | `spacing_md` top | `typography.labelMedium` | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` | `shapes.small` | Only in empty state |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface CollectionsUiState {
    data object Loading
    data object Empty
    data class Content(val rows: List<Collection>)
    data class Error(val message: String)
}
```

| State | What renders |
| --- | --- |
| `Loading` | `CollectionsLoadingContent` — 4 skeleton rows. FAB visible. |
| `Empty` | `CollectionsEmptyContent` — illustration + message + "New collection" button. FAB visible. |
| `Content` | `CollectionsListContent` — `LazyColumn` of swipeable `CollectionRow` items. FAB visible. |
| `Error` | `CollectionsErrorContent` — error icon + message + Retry button. FAB hidden. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnCollectionClicked(id)` | Tap on a `CollectionRow` |
| `OnNewCollection(name)` | Confirm in `NewCollectionDialog`; also CTA button in empty state |
| `OnRename(id, name)` | Confirm in `RenameCollectionDialog` (opened via long-press or leading swipe) |
| `OnDelete(id)` | Trailing swipe-to-dismiss OR "Delete" in long-press `DropdownMenu` (with confirmation) |
| `OnRetry` | Tapping "Retry" in `CollectionsErrorContent` |

---

## @Preview targets

On stateless `CollectionsContent` composable:

1. `CollectionsContent_LightContent` — light theme, 3 collections with varying counts.
2. `CollectionsContent_DarkContent` — dark theme.
3. `CollectionsContent_Empty` — light theme, empty state with CTA.
4. `CollectionsContent_Loading` — light theme, skeleton rows.
5. `CollectionsContent_Error` — light theme, error state.

---

## Accessibility

- `CollectionRow` tap target: full row is clickable; `ListItem` ensures ≥ 48dp height.
- `CollectionCountBadge`: `contentDescription = stringResource(R.string.cd_collection_count, count)` (e.g. "3 requests").
- FAB `Icon`: `contentDescription = stringResource(R.string.cd_new_collection)`.
- `SwipeToDismissBox` rename/delete: add `CustomAccessibilityAction`s: "Rename" and "Delete" so gesture is not the only path.
- Long-press `DropdownMenu`: all items have explicit labels ≥ 48dp touch height.
- Dialog confirm button for destructive delete: announced with "destructive" semantic hint if API allows.
- Skeleton rows: group-level `contentDescription = stringResource(R.string.cd_loading)`.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.titleMedium`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.labelMedium`, `typography.labelSmall`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.onPrimary`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`, `colorScheme.error`
- Shape: `shapes.small`, `shapes.medium`, `shapes.large`, `shapes.extraLarge`
