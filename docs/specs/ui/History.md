# UI Spec — History

## Composable hierarchy

- `HistoryScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `title`: `Text(stringResource(R.string.history_title))` — `typography.titleLarge`
    - `actions`: `EnvironmentChip` + `IconButton(Icons.Default.MoreVert)` overflow
  - `content`:
    - `Column`
      - `OfflineBanner` — shown when `uiState.isOffline == true`; pinned below top bar
      - Conditional based on `HistoryListUiState`:
        - `HistoryLoadingContent`
        - `HistoryEmptyContent`
        - `HistoryErrorContent`
        - `HistoryListContent`

### `HistoryListContent`

- `LazyColumn`
  - `items(rows, key = { it.id })`:
    - Each item: `SwipeToDismissBox` wrapping `HistoryRow`
      - Swipe background: `Box` with `colorScheme.errorContainer` background + `Icon(Icons.Default.Delete)` on trailing side
      - Foreground: `HistoryRow`

### `HistoryRow`

- `ListItem`
  - `leadingContent`: `MethodChip`
  - `headlineContent`: `Text(entry.resolvedUrl)` — `typography.bodyLarge`, single line, ellipsis overflow
  - `supportingContent`: `Text(entry.failureReason)` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`; only shown for `FAILED` entries
  - `trailingContent`: `Column`
    - `StatusBadge` (or `FAILED` chip)
    - `Text(relativeTimestamp)` — `typography.labelSmall`, `colorScheme.onSurfaceVariant`
  - `modifier`: `Modifier.clickable { OnRowClicked(id) }` + `combinedClickable` long-press → context menu

### Long-press context menu (on `HistoryRow`)

- `DropdownMenu`
  - `DropdownMenuItem` "Replay" → emits `OnReplay(id)`
  - `DropdownMenuItem` "Duplicate" → emits `OnDuplicate(id)`
  - `DropdownMenuItem` "Add to collection" → emits `OnAddToCollection(id)`
  - `DropdownMenuItem` "Delete" → emits `OnDelete(id)`

### `HistoryLoadingContent`

- `LazyColumn`
  - `items(6)`: `HistorySkeletonRow` — `ListItem`-shaped shimmer using `colorScheme.surfaceVariant` rounded rectangles (`shapes.small`)

### `HistoryEmptyContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_lg` vertical gap)
    - Illustration placeholder `Box` (`colorScheme.surfaceVariant`, `shapes.medium`, fixed ratio)
    - `Text(stringResource(R.string.history_empty_message))` — `typography.titleMedium`, `colorScheme.onSurfaceVariant`, `textAlign = Center`

### `HistoryErrorContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(stringResource(R.string.history_error_message))` — `typography.bodyLarge`, `colorScheme.onSurface`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetryLoad`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `HistoryScreen` (Scaffold) | — | — | `colorScheme.surface` | — | Root scaffold |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Standard |
| `OfflineBanner` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.labelMedium` | `colorScheme.surfaceVariant` / `colorScheme.onSurfaceVariant` | — | Full-width strip |
| `HistoryRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` headline / `typography.bodySmall` supporting / `typography.labelSmall` trailing | `colorScheme.surface` | — | Tap → detail; long-press → `DropdownMenu` |
| `MethodChip` (leading) | `spacing_xs` | `typography.labelMedium` | Per-method (see index) | `shapes.small` | Non-interactive in this context; `contentDescription = method.name` |
| `StatusBadge` (trailing) | `spacing_xs` | `typography.labelMedium` | Per-class (see index) | `shapes.small` | Shows code + message or "FAILED" |
| Relative timestamp `Text` | `spacing_xs` top | `typography.labelSmall` | `colorScheme.onSurfaceVariant` | — | Accessible content description includes absolute time |
| `SwipeToDismissBox` background | — | — | `colorScheme.errorContainer` | — | Delete icon `colorScheme.onErrorContainer` |
| `HistorySkeletonRow` | `spacing_md` | — | `colorScheme.surfaceVariant` | `shapes.small` | Animated shimmer placeholder |
| `HistoryEmptyContent` illustration | — | — | `colorScheme.surfaceVariant` | `shapes.medium` | Placeholder box until final illustration asset |
| `HistoryErrorContent` icon | — | — | `colorScheme.error` | — | `contentDescription = null` (decorative) |
| `OutlinedButton` Retry | `spacing_md` top | `typography.labelMedium` | `colorScheme.primary` border | `shapes.small` | Min 48dp height |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface HistoryListUiState {
    data object Loading
    data object Empty
    data class Content(val rows: List<HistoryEntryUi>, val isOffline: Boolean)
    data class Error(val message: String)
}
```

| State | What renders |
| --- | --- |
| `Loading` | `HistoryLoadingContent` — 6 skeleton rows with shimmer animation. `OfflineBanner` is hidden. |
| `Empty` | `HistoryEmptyContent` — centered illustration placeholder + "No requests yet…" + no FAB. |
| `Error` | `HistoryErrorContent` — error icon + message + Retry button. |
| `Content` | `HistoryListContent` — `LazyColumn` of `SwipeToDismissBox`-wrapped `HistoryRow` items. |
| `Content` with `isOffline = true` | Same as `Content` plus `OfflineBanner` pinned at top. History rows remain fully interactive (read-only local data). |
| `Content` with a FAILED-status row | `HistoryRow` shows `FAILED` `StatusBadge` + failure reason as `supportingContent`. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnRowClicked(id)` | Tap on a `HistoryRow` |
| `OnReplay(id)` | "Replay" item in long-press `DropdownMenu` |
| `OnDuplicate(id)` | "Duplicate" item in long-press `DropdownMenu` |
| `OnAddToCollection(id)` | "Add to collection" item in long-press `DropdownMenu` |
| `OnDelete(id)` | Swipe-to-dismiss completion OR "Delete" item in long-press `DropdownMenu` |
| `OnRetryLoad` | Tapping "Retry" in `HistoryErrorContent` |

---

## @Preview targets

On stateless `HistoryContent` composable:

1. `HistoryContent_LightContent` — light theme, populated list, `isOffline = false`.
2. `HistoryContent_DarkContent` — dark theme, populated list.
3. `HistoryContent_ContentOffline` — light theme, `isOffline = true`, `OfflineBanner` visible.
4. `HistoryContent_Empty` — light theme, empty state.
5. `HistoryContent_Loading` — light theme, skeleton rows.
6. `HistoryContent_Error` — light theme, error state with Retry.
7. `HistoryContent_WithFailed` — light theme, list containing at least one `FAILED` row.

---

## Accessibility

- `HistoryRow` (`ListItem`): `contentDescription` should describe "HTTP method URL — status — time" for screen readers; use `Modifier.semantics { contentDescription = "..." }` on the row.
- `StatusBadge` when showing "FAILED": `contentDescription = stringResource(R.string.cd_failed_request, reason)`.
- Relative timestamp `Text`: add `semantics { contentDescription = absoluteTimeString }` alongside the relative display string.
- `SwipeToDismissBox`: add `Modifier.semantics { customActions = listOf(CustomAccessibilityAction("Delete") { onDelete(); true }) }` so delete is reachable without gesture.
- `OfflineBanner`: `semantics { liveRegion = LiveRegionMode.Polite }` so screen readers announce connectivity changes.
- All `DropdownMenuItem`s must have non-empty text; touch targets ≥ 48×48dp.
- Decorative delete `Icon` in swipe background: `contentDescription = null`.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.bodyLarge`, `typography.bodySmall`, `typography.labelMedium`, `typography.labelSmall`, `typography.titleMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.error`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`, `colorScheme.primary`
- Shape: `shapes.small`, `shapes.medium`
