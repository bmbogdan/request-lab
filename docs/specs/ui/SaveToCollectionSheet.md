# UI Spec — SaveToCollectionSheet

This screen is a `ModalBottomSheet`. It is never pushed onto the back stack; system Back dismisses it and emits `OnDismiss`.

## Composable hierarchy

- `SaveToCollectionSheet` (`ModalBottomSheet`, `BottomSheetDefaults.Elevation`, `shapes.large` top corners)
  - `SheetDragHandle` (default M3 drag handle)
  - `Column` (padding `spacing_md` horizontal, `spacing_sm` vertical)
    - `Text(stringResource(R.string.save_to_collection_title))` — `typography.titleMedium`
    - `HorizontalDivider`
    - Conditional based on `SaveSheetUiState`:
      - `SaveSheetLoading`
      - `SaveSheetEmpty`
      - `SaveSheetContent`
    - `HorizontalDivider`
    - `OutlinedTextField` — request name field; label `stringResource(R.string.request_name_label)`; prefilled with method + path
    - `SaveSheetError` — shown when `uiState.error != null`
    - `Row` (end-aligned, `spacing_sm` gap)
      - `TextButton(stringResource(R.string.cancel))` — emits `OnDismiss`
      - `Button(stringResource(R.string.save))` — emits `OnSaveConfirmed`; disabled while `uiState.saving == true` or no collection selected and no new name entered

### `SaveSheetLoading`

- `Box` (height = 3 × `spacing_xl`, `contentAlignment = Center`)
  - `CircularProgressIndicator`

### `SaveSheetEmpty`

- `Column` (`spacing_md` vertical padding)
  - `Text(stringResource(R.string.no_collections_hint))` — `typography.bodyMedium`, `colorScheme.onSurfaceVariant`
  - `NewCollectionInputRow` (see below)

### `SaveSheetContent`

- `LazyColumn` (max height = `spacing_xl` × 6 to prevent sheet overflow; vertically scrollable within that)
  - `items(collections)`:
    - `CollectionRadioRow` — `RadioButton` + `Text(collection.name)` — `typography.bodyLarge`; tapping row or radio emits `OnSelectCollection(id)`; selected row uses `colorScheme.secondaryContainer` background
  - `item`: `NewCollectionInputRow`

### `NewCollectionInputRow`

- `Row` (vertically centered, `spacing_sm` gap)
  - `RadioButton(selected = uiState.selectedId == null && uiState.newName.isNotBlank())` — emits `OnSelectCollection(null)`
  - `OutlinedTextField`
    - `label`: `stringResource(R.string.new_collection_label)`
    - `value`: `uiState.newName`
    - `onValueChange`: `OnNewCollectionNameChanged`
    - Trailing clear `IconButton` when text is non-empty

### `SaveSheetError`

- `Text(uiState.error)` — `typography.bodySmall`, `colorScheme.error`, padding `spacing_sm` top

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `SaveToCollectionSheet` (`ModalBottomSheet`) | `spacing_md` horizontal / `spacing_sm` vertical | — | `colorScheme.surface` | `shapes.large` (top) | `BottomSheetDefaults.Elevation` |
| Sheet title `Text` | `spacing_sm` vertical | `typography.titleMedium` | `colorScheme.onSurface` | — | |
| `CollectionRadioRow` | `spacing_sm` vertical | `typography.bodyLarge` | `colorScheme.secondaryContainer` when selected | — | Full-row tap target; ≥ 48dp height |
| `RadioButton` | — | — | `colorScheme.primary` when selected | — | Leading in row |
| `NewCollectionInputRow` | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | |
| Request name `OutlinedTextField` | `spacing_sm` top | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | Prefilled; editable |
| `SaveSheetError` `Text` | `spacing_sm` top | `typography.bodySmall` | `colorScheme.error` | — | Inline; sheet stays open |
| Cancel `TextButton` | — | `typography.labelMedium` | `colorScheme.primary` | — | |
| Save `Button` | — | `typography.labelMedium` | `colorScheme.primary` / `colorScheme.onPrimary` | `shapes.small` | Disabled visually while saving |
| `CircularProgressIndicator` (loading) | — | — | `colorScheme.primary` | — | Centered in placeholder box |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface SaveSheetUiState {
    data object Loading
    data class Empty(val requestName: String)
    data class Content(
        val collections: List<Collection>,
        val selectedId: String?,
        val newName: String,
        val requestName: String,
        val saving: Boolean,
        val error: String?
    )
}
```

| State | What renders |
| --- | --- |
| `Loading` | `SaveSheetLoading` — centered spinner inside fixed-height box. Request name field and buttons visible but Save is disabled. |
| `Empty` | `SaveSheetEmpty` — hint text + `NewCollectionInputRow` only. Request name field prefilled. Save enabled once new name non-empty. |
| `Content` | `SaveSheetContent` — `LazyColumn` of `CollectionRadioRow`s + `NewCollectionInputRow`. Selected row highlighted. Request name field prefilled from `requestName`. Save enabled when a selection is made or `newName` non-empty. |
| `Content` with `saving = true` | Save `Button` shows `CircularProgressIndicator` replacing label text; all interactive items disabled. |
| `Content` with `error != null` | `SaveSheetError` appears above the action row. Sheet remains open. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnSelectCollection(id)` | Tapping a `CollectionRadioRow` or its `RadioButton` |
| `OnNewCollectionNameChanged(name)` | Typing in the `NewCollectionInputRow` `OutlinedTextField` |
| `OnRequestNameChanged(name)` | Typing in the request name `OutlinedTextField` |
| `OnSaveConfirmed` | Tapping Save `Button` |
| `OnDismiss` | Tapping Cancel `TextButton`; system Back; sheet drag-to-dismiss |

---

## @Preview targets

On stateless `SaveToCollectionSheetContent` composable:

1. `SaveSheetContent_LightContent` — light theme, 3 collections, one selected.
2. `SaveSheetContent_DarkContent` — dark theme.
3. `SaveSheetContent_Empty` — light theme, empty state with new-collection input.
4. `SaveSheetContent_Loading` — light theme, spinner.
5. `SaveSheetContent_Error` — light theme, error text below action row.
6. `SaveSheetContent_Saving` — light theme, Save button in loading state.

---

## Accessibility

- `ModalBottomSheet`: sheet drag handle announced as `contentDescription = stringResource(R.string.cd_sheet_drag_handle)`.
- `CollectionRadioRow`: the full row is a single semantic unit; `contentDescription = "Collection: ${name}, ${if selected "selected" else "not selected"}"`.
- `RadioButton` inside row: `contentDescription = null` (row description suffices); `selected` state propagated via semantics.
- Request name `OutlinedTextField`: `label` provides accessible name.
- Save `Button` when disabled: `contentDescription` updated to include reason (e.g. "Save disabled — choose a collection or enter a name").
- `SaveSheetError`: `semantics { liveRegion = LiveRegionMode.Polite }` to announce error to screen reader.
- All touch targets ≥ 48×48dp; `CollectionRadioRow` enforces `Modifier.heightIn(min = 48.dp)`.

---

## Tokens used (audit)

- Spacing: `spacing_sm`, `spacing_md`, `spacing_xl`
- Typography: `typography.titleMedium`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`
- Color: `colorScheme.surface`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.onPrimary`, `colorScheme.secondaryContainer`, `colorScheme.error`
- Shape: `shapes.small`, `shapes.large`
- Elevation: `BottomSheetDefaults.Elevation`
