# UI Spec — CurlExportSheet

This screen is a `ModalBottomSheet`. It is never pushed onto the back stack; system Back or the drag handle dismisses it and emits `OnDismiss`.

## Composable hierarchy

- `CurlExportSheet` (`ModalBottomSheet`, `BottomSheetDefaults.Elevation`, `shapes.large` top corners)
  - `SheetDragHandle` (default M3 drag handle)
  - `Column` (padding `spacing_md` horizontal, `spacing_sm` vertical)
    - `CurlSheetHeader` — `Row`
      - `Text(stringResource(R.string.curl_export_title))` — `typography.titleMedium`, weight 1
      - `IconButton(Icons.Default.Close)` — emits `OnDismiss`; `contentDescription = stringResource(R.string.cd_close_sheet)`
    - `HorizontalDivider`
    - Conditional based on `CurlUiState`:
      - `CurlGenerating`
      - `CurlContent`

### `CurlGenerating`

- `Box` (height = 4 × `spacing_xl`, `contentAlignment = Center`)
  - `CircularProgressIndicator`

### `CurlContent`

- `Column`
  - `UnresolvedWarningBanner` — shown when `uiState.unresolvedWarning != null`
  - `CurlCommandBlock` — `MonospaceCodeBlock` wrapping `uiState.command.text`
    - Horizontally scrollable
    - `colorScheme.surface` background, `shapes.small` clip
    - `typography.bodySmall` monospace font
    - `SelectionContainer` so text is selectable
    - Vertically constrained to a max of 40% of sheet height (scrollable vertically within that)
  - `HorizontalDivider`
  - `CredentialsToggleRow` — `Row` (vertically centered, `spacing_md` horizontal padding, `spacing_sm` vertical)
    - `Column` (weight 1)
      - `Text(stringResource(R.string.include_credentials_label))` — `typography.bodyMedium`
      - `Text(stringResource(R.string.include_credentials_hint))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
    - `Switch(checked = uiState.includeCredentials, onCheckedChange = { OnToggleIncludeCredentials })`
  - `HorizontalDivider`
  - `CurlActionRow` — `Row` (horizontally even, `spacing_md` padding, `spacing_sm` gap)
    - `OutlinedButton(stringResource(R.string.copy))` — weight 1; emits `OnCopy`; leading `Icon(Icons.Default.ContentCopy)`
    - `FilledTonalButton(stringResource(R.string.share))` — weight 1; emits `OnShare`; leading `Icon(Icons.Default.Share)`

### `UnresolvedWarningBanner`

- `Surface` (`colorScheme.errorContainer`, `shapes.small`, padding `spacing_sm`)
  - `Row` (vertically centered, `spacing_sm` gap)
    - `Icon(Icons.Default.Warning, contentDescription = null)` — `colorScheme.onErrorContainer`
    - `Text(uiState.unresolvedWarning)` — `typography.bodySmall`, `colorScheme.onErrorContainer`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `CurlExportSheet` (`ModalBottomSheet`) | `spacing_md` horizontal / `spacing_sm` vertical | — | `colorScheme.surface` | `shapes.large` (top) | `BottomSheetDefaults.Elevation` |
| Sheet title `Text` | — | `typography.titleMedium` | `colorScheme.onSurface` | — | |
| Close `IconButton` | — | — | `colorScheme.onSurface` | — | `contentDescription` required |
| `UnresolvedWarningBanner` | `spacing_sm` | `typography.bodySmall` | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` | `shapes.small` | Warning icon + message |
| `CurlCommandBlock` (`MonospaceCodeBlock`) | `spacing_md` | `typography.bodySmall` | `colorScheme.surface` | `shapes.small` | Horizontally + vertically scrollable; `SelectionContainer` |
| `CredentialsToggleRow` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyMedium` label / `typography.bodySmall` hint | `colorScheme.onSurface` / `colorScheme.onSurfaceVariant` | — | Full-width row |
| `Switch` | — | — | `colorScheme.primary` (thumb when on) | — | M3 default Switch |
| Copy `OutlinedButton` | `spacing_md` | `typography.labelMedium` | `colorScheme.primary` border | `shapes.small` | Leading copy icon |
| Share `FilledTonalButton` | `spacing_md` | `typography.labelMedium` | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` | `shapes.small` | Leading share icon |
| `CircularProgressIndicator` (generating) | — | — | `colorScheme.primary` | — | Centered in placeholder box |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface CurlUiState {
    data object Generating
    data class Content(
        val command: CurlCommand,
        val includeCredentials: Boolean,
        val unresolvedWarning: String?
    )
}
```

| State | What renders |
| --- | --- |
| `Generating` | `CurlGenerating` — spinner inside fixed-height placeholder. Header + close button visible. Action buttons hidden. |
| `Content` | Full `CurlContent` layout. Copy and Share buttons enabled. |
| `Content` with `unresolvedWarning != null` | `UnresolvedWarningBanner` appears above `CurlCommandBlock`. Command still rendered as-is with literal `{{var}}` tokens. |
| `Content` with `includeCredentials = false` | Command text shows `-u [username]:[redacted]` or `Bearer [redacted]` — text is read-only; implementer must not sanitize client-side, the ViewModel generates the correct text. |
| `Content` with `includeCredentials = true` | Command text shows real credential values. Toggle reflects on state. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnToggleIncludeCredentials` | Toggling the `Switch` in `CredentialsToggleRow` |
| `OnCopy` | Tapping "Copy" `OutlinedButton` |
| `OnShare` | Tapping "Share" `FilledTonalButton` |
| `OnDismiss` | Tapping close `IconButton`; system Back; sheet drag-to-dismiss |

---

## @Preview targets

On stateless `CurlExportSheetContent` composable:

1. `CurlExportSheet_LightContent` — light theme, `Content` with a multi-line cURL command, `includeCredentials = false`.
2. `CurlExportSheet_DarkContent` — dark theme, same.
3. `CurlExportSheet_WithWarning` — light theme, `unresolvedWarning` banner visible.
4. `CurlExportSheet_CredentialsRevealed` — light theme, `includeCredentials = true`.
5. `CurlExportSheet_Generating` — light theme, spinner state.

---

## Accessibility

- Sheet drag handle: `contentDescription = stringResource(R.string.cd_sheet_drag_handle)`.
- Close `IconButton`: `contentDescription = stringResource(R.string.cd_close_sheet)`.
- `CurlCommandBlock` inside `SelectionContainer`: add `semantics { contentDescription = stringResource(R.string.cd_curl_command) }` on the block so TalkBack announces it as "cURL command" before reading content.
- `UnresolvedWarningBanner`: `semantics { liveRegion = LiveRegionMode.Polite }` — announced when it appears after toggling credentials.
- `Switch`: TalkBack uses M3 `Switch` built-in label; pair it with `semantics { stateDescription = if (includeCredentials) "On" else "Off" }`.
- Copy button: after `OnCopy` is processed, the ViewModel should expose a one-shot snackbar signal; the Scaffold's `SnackbarHost` announces it via `LiveRegionMode.Polite`.
- All buttons ≥ 48×48dp (M3 defaults satisfy this).

---

## Tokens used (audit)

- Spacing: `spacing_sm`, `spacing_md`, `spacing_xl`
- Typography: `typography.titleMedium`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`
- Color: `colorScheme.surface`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`
- Shape: `shapes.small`, `shapes.large`
- Elevation: `BottomSheetDefaults.Elevation`
