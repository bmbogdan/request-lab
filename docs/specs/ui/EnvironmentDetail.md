# UI Spec — EnvironmentDetail

## Composable hierarchy

- `EnvironmentDetailScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `navigationIcon`: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` — emits `OnBack` (if dirty, prompt `UnsavedChangesDialog`)
    - `title`: `Text(stringResource(R.string.environment_detail_title))` — `typography.titleLarge`
    - `actions`:
      - `TextButton(stringResource(R.string.save))` — emits `OnSave`; enabled when `!uiState.saving`
  - `content`:
    - Conditional based on `EnvironmentDetailUiState`:
      - `EnvironmentDetailLoading`
      - `EnvironmentDetailContent`
      - `EnvironmentDetailError`

### `EnvironmentDetailContent`

- `LazyColumn`
  - `item`: `EnvironmentNameField`
    - `OutlinedTextField`
      - `label`: `stringResource(R.string.environment_name_label)`
      - `value`: `uiState.name`
      - `onValueChange`: `OnNameChanged`
      - padding `spacing_md` horizontal, `spacing_sm` vertical
  - `item`: `EncryptedValuesNotice`
    - `Row` (spacing `spacing_xs`, vertically centered)
      - `Icon(Icons.Default.Lock, contentDescription = null)` — `colorScheme.onSurfaceVariant`, small
      - `Text(stringResource(R.string.env_encrypted_notice))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
    - padding `spacing_md` horizontal, `spacing_xs` vertical
  - `item`: `SectionHeader("Variables")` — `typography.titleSmall`, `colorScheme.onSurfaceVariant`
  - `items(rows, key = { it.key })`:
    - `VariableRow` (keyed for stable recomposition)
  - `item`: `TextButton(stringResource(R.string.add_variable))` — emits `OnAddVariable`
    - padding `spacing_md`
  - `item`: `SectionSaveErrorRow` — shown when `uiState.validationErrors` or save error is non-empty

### `VariableRow`

- `Row` (vertically aligned, `spacing_sm` horizontal gap)
  - `OutlinedTextField`
    - `label`: `stringResource(R.string.variable_key_label)`
    - `value`: `variable.key`
    - `onValueChange`: `OnVariableEdited(index, variable.copy(key = ...))`
    - `isError`: `index in uiState.validationErrors`
    - weight 1
  - `OutlinedTextField`
    - `label`: `stringResource(R.string.variable_value_label)`
    - `value`: `variable.value`
    - `onValueChange`: `OnVariableEdited(index, variable.copy(value = ...))`
    - weight 1
  - `IconButton(Icons.Default.Delete)` — `colorScheme.error`; emits `OnDeleteVariable(index)`
- Inline validation below key field: `Text(stringResource(R.string.key_already_exists))` — `typography.bodySmall`, `colorScheme.error`; shown when key is a duplicate

### `SectionSaveErrorRow`

- `Text(errorMessage)` — `typography.bodySmall`, `colorScheme.error`
- padding `spacing_md` horizontal, `spacing_sm` vertical
- `semantics { liveRegion = LiveRegionMode.Polite }`

### `EnvironmentDetailLoading`

- `LazyColumn`
  - `item`: Skeleton for name field — `colorScheme.surfaceVariant`, `shapes.small`, full-width
  - `items(4)`: `VariableSkeletonRow` — shimmer key+value+delete placeholder

### `EnvironmentDetailError`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(uiState.message)` — `typography.bodyLarge`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetry`

### `UnsavedChangesDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.unsaved_changes_title))`
- `text`: `Text(stringResource(R.string.unsaved_changes_body))`
- `confirmButton`: `TextButton("Discard")` — allows navigation back
- `dismissButton`: `TextButton("Keep editing")`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `EnvironmentDetailScreen` (Scaffold) | — | — | `colorScheme.surface` | — | |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Back + Save |
| `EnvironmentNameField` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` | `colorScheme.surface` | `shapes.small` | Full-width |
| `EncryptedValuesNotice` | `spacing_md` horizontal, `spacing_xs` vertical | `typography.bodySmall` | `colorScheme.onSurfaceVariant` | — | Lock icon + text |
| `SectionHeader` "Variables" | `spacing_md` horizontal, `spacing_sm` vertical | `typography.titleSmall` | `colorScheme.onSurfaceVariant` | — | |
| `VariableRow` key `OutlinedTextField` | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.surface` (`isError = colorScheme.error`) | `shapes.small` | weight 1 |
| `VariableRow` value `OutlinedTextField` | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | weight 1 |
| Delete `IconButton` | — | — | `colorScheme.error` | — | Min 48×48dp |
| Duplicate-key error `Text` | `spacing_xs` top | `typography.bodySmall` | `colorScheme.error` | — | Below key field |
| "Add variable" `TextButton` | `spacing_md` | `typography.labelMedium` | `colorScheme.primary` | — | |
| `SectionSaveErrorRow` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodySmall` | `colorScheme.error` | — | Live region |
| Save `TextButton` (`TopAppBar` action) | — | `typography.labelMedium` | `colorScheme.primary` | — | Disabled while saving |
| `AlertDialog` | `spacing_md` | `typography.titleMedium` / `typography.bodyMedium` | `colorScheme.surface` | `shapes.extraLarge` | |
| `VariableSkeletonRow` | `spacing_sm` | — | `colorScheme.surfaceVariant` | `shapes.small` | Shimmer |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface EnvironmentDetailUiState {
    data object Loading
    data class Content(
        val name: String,
        val rows: List<Variable>,
        val validationErrors: Map<Int, String>,
        val saving: Boolean
    )
    data class Error(val message: String)
}
```

| State | What renders |
| --- | --- |
| `Loading` | `EnvironmentDetailLoading` — name field skeleton + 4 variable skeleton rows. Save action disabled. |
| `Content` | `EnvironmentDetailContent` — editable name field + `VariableRow` list + add button. Save enabled when not `saving`. |
| `Content` with `validationErrors` non-empty | Affected `VariableRow` key fields show `isError = true` and inline duplicate-key error text. Save remains disabled until errors resolved. |
| `Content` with `saving = true` | Save `TextButton` is disabled; small `CircularProgressIndicator` in toolbar area or the Save button becomes a spinner. |
| `Error` | `EnvironmentDetailError` — error icon + message + Retry. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnBack` | Back arrow; system Back; possibly guarded by `UnsavedChangesDialog` |
| `OnNameChanged(name)` | Typing in `EnvironmentNameField` |
| `OnAddVariable` | Tapping "Add variable" `TextButton` |
| `OnVariableEdited(index, Variable)` | Typing in any key or value `OutlinedTextField` in a `VariableRow` |
| `OnDeleteVariable(index)` | Tapping delete `IconButton` in a `VariableRow` |
| `OnSave` | Tapping Save `TextButton` in `TopAppBar` |
| `OnRetry` | Retry button in `EnvironmentDetailError` |

---

## @Preview targets

On stateless `EnvironmentDetailContent` composable:

1. `EnvironmentDetailContent_LightContent` — light theme, 3 variables, no errors.
2. `EnvironmentDetailContent_DarkContent` — dark theme.
3. `EnvironmentDetailContent_ValidationError` — light theme, one duplicate-key error highlighted.
4. `EnvironmentDetailContent_Loading` — light theme, skeleton rows.
5. `EnvironmentDetailContent_Error` — light theme, error state.
6. `EnvironmentDetailContent_Saving` — light theme, Save button disabled/spinner.

---

## Accessibility

- `EnvironmentNameField`: `label` = environment name; unique `testTag` for UI tests.
- `VariableRow` key `OutlinedTextField`: `label = stringResource(R.string.variable_key_label)` (non-empty for screen reader).
- `VariableRow` value `OutlinedTextField`: `label = stringResource(R.string.variable_value_label)`.
- When `isError = true` on key field: `errorMessage` via `supportingText` slot is announced by TalkBack automatically through M3 `OutlinedTextField`.
- Delete `IconButton`: `contentDescription = stringResource(R.string.cd_delete_variable, variable.key)`.
- `EncryptedValuesNotice` lock `Icon`: `contentDescription = null` (decorative; text explains the notice).
- `SectionSaveErrorRow`: `liveRegion = LiveRegionMode.Polite`.
- All touch targets ≥ 48×48dp; delete `IconButton` uses `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.titleSmall`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`, `typography.titleMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.error`
- Shape: `shapes.small`, `shapes.extraLarge`
