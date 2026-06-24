# UI Spec — Environments

This screen is reached from the `EnvironmentSwitcher` via "Manage environments". It is not a bottom-nav tab; it is pushed onto the current tab's back stack.

## Composable hierarchy

- `EnvironmentsScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `navigationIcon`: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` — emits `OnBack`
    - `title`: `Text(stringResource(R.string.environments_title))` — `typography.titleLarge`
  - `content`:
    - Conditional based on `EnvironmentsUiState`:
      - `EnvironmentsLoadingContent`
      - `EnvironmentsEmptyContent`
      - `EnvironmentsErrorContent`
      - `EnvironmentsListContent`
  - `floatingActionButton`: `FloatingActionButton`
    - `Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_new_environment))`
    - Emits — opens `NewEnvironmentDialog`
    - Visible in all states except `Error`

### `EnvironmentsListContent`

- `LazyColumn`
  - `items(rows, key = { it.id })`:
    - Each item: `EnvironmentRow` with `combinedClickable` long-press → `DropdownMenu`

### `EnvironmentRow`

- `ListItem`
  - `leadingContent`: Conditional
    - If `environment.isActive`: `Icon(Icons.Default.CheckCircle)` — `colorScheme.primary`; `contentDescription = stringResource(R.string.cd_active_env)`
    - Else: `Icon(Icons.Default.RadioButtonUnchecked)` — `colorScheme.onSurfaceVariant`; `contentDescription = null`
  - `headlineContent`: `Text(environment.name)` — `typography.bodyLarge`
  - `supportingContent`: `Text(stringResource(R.string.variable_count, environment.variableCount))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
  - `modifier`: `Modifier.clickable { OnEnvironmentClicked(id) }`

### Long-press `DropdownMenu` on `EnvironmentRow`

- `DropdownMenuItem` "Rename" → opens `RenameEnvironmentDialog`
- `DropdownMenuItem` "Delete" → opens `DeleteEnvironmentDialog`

### `NewEnvironmentDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.new_environment_title))`
- `text`: `OutlinedTextField(label = stringResource(R.string.environment_name_label))`
- `confirmButton`: `TextButton("Create")` — emits `OnNewEnvironment(name)` if non-empty
- `dismissButton`: `TextButton("Cancel")`

### `RenameEnvironmentDialog` (AlertDialog)

- Same structure; confirm emits `OnRename(id, name)`

### `DeleteEnvironmentDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.delete_environment_confirm_title))`
- `text`: `Text(stringResource(R.string.delete_environment_confirm_body, name))`
- `confirmButton`: `TextButton("Delete", color = colorScheme.error)` — emits `OnDelete(id)`
- `dismissButton`: `TextButton("Cancel")`

### `EnvironmentsLoadingContent`

- `LazyColumn`
  - `items(3)`: `EnvironmentSkeletonRow` — shimmer `ListItem`-shaped placeholder

### `EnvironmentsEmptyContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_lg` gap)
    - Illustration placeholder `Box` (`colorScheme.surfaceVariant`, `shapes.medium`)
    - `Text(stringResource(R.string.environments_empty_message))` — `typography.bodyLarge`, `colorScheme.onSurfaceVariant`, `textAlign = Center`
    - `FilledTonalButton(stringResource(R.string.new_environment))` — opens `NewEnvironmentDialog`

### `EnvironmentsErrorContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(stringResource(R.string.environments_error_message))` — `typography.bodyLarge`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetry`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `EnvironmentsScreen` (Scaffold) | — | — | `colorScheme.surface` | — | |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Back nav |
| `EnvironmentRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` headline / `typography.bodySmall` supporting | `colorScheme.surface` | — | Tap → `EnvironmentDetail` |
| Active checkmark `Icon` | — | — | `colorScheme.primary` | — | Visible only for active env |
| Inactive circle `Icon` | — | — | `colorScheme.onSurfaceVariant` | — | `contentDescription = null` |
| Variable count `Text` | — | `typography.bodySmall` | `colorScheme.onSurfaceVariant` | — | Supporting text in `ListItem` |
| `FloatingActionButton` | — | — | `colorScheme.primary` / `colorScheme.onPrimary` | `shapes.large` | |
| `AlertDialog` | `spacing_md` | `typography.titleMedium` / `typography.bodyMedium` | `colorScheme.surface` | `shapes.extraLarge` | |
| Destructive `TextButton` | — | `typography.labelMedium` | `colorScheme.error` | — | Delete confirm |
| `EnvironmentSkeletonRow` | `spacing_md` | — | `colorScheme.surfaceVariant` | `shapes.small` | Shimmer |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface EnvironmentsUiState {
    data object Loading
    data object Empty
    data class Content(val rows: List<Environment>, val activeId: String?)
    data class Error(val message: String)
}
```

| State | What renders |
| --- | --- |
| `Loading` | `EnvironmentsLoadingContent` — 3 skeleton rows. FAB visible. |
| `Empty` | `EnvironmentsEmptyContent` — illustration + message + "New environment" button. FAB visible. |
| `Content` | `EnvironmentsListContent` — `LazyColumn` with active env row showing checkmark. FAB visible. |
| `Error` | `EnvironmentsErrorContent` — error icon + message + Retry. FAB hidden. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnBack` | Back arrow; system Back |
| `OnEnvironmentClicked(id)` | Tap on `EnvironmentRow` |
| `OnNewEnvironment(name)` | Confirm in `NewEnvironmentDialog`; CTA button in empty state |
| `OnRename(id, name)` | Confirm in `RenameEnvironmentDialog` |
| `OnDelete(id)` | Confirm in `DeleteEnvironmentDialog` |
| `OnRetry` | Retry button in `EnvironmentsErrorContent` |

---

## @Preview targets

On stateless `EnvironmentsContent` composable:

1. `EnvironmentsContent_LightContent` — light theme, 2 environments, one active (checkmark).
2. `EnvironmentsContent_DarkContent` — dark theme.
3. `EnvironmentsContent_Empty` — light theme, empty state.
4. `EnvironmentsContent_Loading` — light theme, skeleton rows.
5. `EnvironmentsContent_Error` — light theme, error state.

---

## Accessibility

- `EnvironmentRow` active icon: `contentDescription = stringResource(R.string.cd_active_env)` (e.g. "Active environment").
- `EnvironmentRow` tap: announces "Opens environment detail for ${name}".
- Long-press for rename/delete: add `CustomAccessibilityAction("Rename")` and `CustomAccessibilityAction("Delete")` semantics on each row.
- FAB: `contentDescription = stringResource(R.string.cd_new_environment)`.
- Dialog destructive confirm: announce "This will delete the environment and all its variables" in `text`.
- Skeleton rows: group `contentDescription = stringResource(R.string.cd_loading)`.
- All touch targets ≥ 48×48dp.

---

## Tokens used (audit)

- Spacing: `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`, `typography.titleMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.onPrimary`, `colorScheme.error`
- Shape: `shapes.small`, `shapes.medium`, `shapes.large`, `shapes.extraLarge`
