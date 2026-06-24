# UI Spec — Settings

Settings is reached from the toolbar overflow menu on any primary tab screen. It is pushed onto the current tab's back stack.

## Composable hierarchy

- `SettingsScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `navigationIcon`: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` — emits `OnBack`
    - `title`: `Text(stringResource(R.string.settings_title))` — `typography.titleLarge`
  - `content`:
    - `SettingsContent` (always Content; no Loading or Empty state)

### `SettingsContent`

- `LazyColumn`
  - `item`: `SettingsSectionHeader(stringResource(R.string.settings_section_requests))`
  - `item`: `TimeoutSettingRow`
  - `item`: `FollowRedirectsRow`
  - `item`: `HorizontalDivider`
  - `item`: `SettingsSectionHeader(stringResource(R.string.settings_section_data))`
  - `item`: `ClearHistoryRow`
  - `item`: `ResetAppRow`

### `SettingsSectionHeader`

- `Text(title)` — `typography.titleSmall`, `colorScheme.primary`
- padding `spacing_md` horizontal, `spacing_sm` vertical (top `spacing_lg` for non-first sections)

### `TimeoutSettingRow`

- `Column` (padding `spacing_md` horizontal, `spacing_sm` vertical)
  - `Row` (vertically centered)
    - `Text(stringResource(R.string.timeout_label))` — `typography.bodyLarge`, weight 1
    - `Text("${uiState.settings.timeoutSeconds}s")` — `typography.bodyLarge`, `colorScheme.primary`
  - `Text(stringResource(R.string.timeout_hint))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
  - `Slider`
    - `value`: `uiState.settings.timeoutSeconds.toFloat()`
    - `valueRange`: `1f..600f`
    - `steps`: 0 (continuous; implementer may choose appropriate step granularity)
    - `onValueChangeFinished`: emits `OnTimeoutChanged(seconds.toInt())`
    - `colors`: `SliderDefaults.colors(thumbColor = colorScheme.primary, activeTrackColor = colorScheme.primary)`

### `FollowRedirectsRow`

- `ListItem`
  - `headlineContent`: `Text(stringResource(R.string.follow_redirects_label))` — `typography.bodyLarge`
  - `supportingContent`: `Text(stringResource(R.string.follow_redirects_hint))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
  - `trailingContent`: `Switch`
    - `checked`: `uiState.settings.followRedirects`
    - `onCheckedChange`: `OnFollowRedirectsToggled(it)`

### `ClearHistoryRow`

- `ListItem`
  - `headlineContent`: `Text(stringResource(R.string.clear_history_label))` — `typography.bodyLarge`, `colorScheme.error`
  - `supportingContent`: `Text(stringResource(R.string.clear_history_hint))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
  - `modifier`: `Modifier.clickable { showClearHistoryDialog = true }`
  - `trailingContent`: Busy spinner `CircularProgressIndicator` (small) when `busyAction == ClearingHistory`; else `Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)` — decorative

### `ResetAppRow`

- `ListItem`
  - `headlineContent`: `Text(stringResource(R.string.reset_app_label))` — `typography.bodyLarge`, `colorScheme.error`
  - `supportingContent`: `Text(stringResource(R.string.reset_app_hint))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
  - `modifier`: `Modifier.clickable { showResetDialog = true }`
  - `trailingContent`: Busy spinner when `busyAction == Resetting`

### `ActionErrorBanner`

- Shown below the relevant row when `uiState.actionError != null`
- `Surface` (`colorScheme.errorContainer`, `shapes.small`, padding `spacing_sm`)
  - `Row` (spacing `spacing_xs`)
    - `Icon(Icons.Default.ErrorOutline, contentDescription = null)` — `colorScheme.onErrorContainer`
    - `Text(uiState.actionError)` — `typography.bodySmall`, `colorScheme.onErrorContainer`
    - Spacer + `TextButton("Dismiss", colorScheme.onErrorContainer)` — emits `OnDismissError`
- `semantics { liveRegion = LiveRegionMode.Polite }`

### `ClearHistoryDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.clear_history_confirm_title))`
- `text`: `Text(stringResource(R.string.clear_history_confirm_body))` — "Clear all history? This can't be undone."
- `confirmButton`: `TextButton("Clear", color = colorScheme.error)` — emits `OnClearHistoryConfirmed`
- `dismissButton`: `TextButton("Cancel")`

### `ResetAppDialog` (AlertDialog)

- `title`: `Text(stringResource(R.string.reset_app_confirm_title))`
- `text`: `Text(stringResource(R.string.reset_app_confirm_body))` — "Reset all data? History, collections, environments, and credentials will be permanently deleted."
- `confirmButton`: `TextButton("Reset", color = colorScheme.error)` — emits `OnResetConfirmed`
- `dismissButton`: `TextButton("Cancel")`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `SettingsScreen` (Scaffold) | — | — | `colorScheme.surface` | — | |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Back nav only |
| `SettingsSectionHeader` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.titleSmall` | `colorScheme.primary` | — | Section label |
| `TimeoutSettingRow` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` / `typography.bodySmall` | `colorScheme.primary` (value), `colorScheme.onSurfaceVariant` (hint) | — | Full-width |
| `Slider` | — | — | `colorScheme.primary` | — | Range 1–600 |
| `FollowRedirectsRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` / `typography.bodySmall` | `colorScheme.onSurface` / `colorScheme.onSurfaceVariant` | — | Trailing `Switch` |
| `ClearHistoryRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` | `colorScheme.error` (headline) / `colorScheme.onSurfaceVariant` (supporting) | — | Tappable; shows busy spinner |
| `ResetAppRow` (`ListItem`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyLarge` | `colorScheme.error` (headline) / `colorScheme.onSurfaceVariant` (supporting) | — | Tappable; shows busy spinner |
| `ActionErrorBanner` | `spacing_sm` | `typography.bodySmall` | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` | `shapes.small` | Live region |
| `AlertDialog` (both) | `spacing_md` | `typography.titleMedium` title / `typography.bodyMedium` body | `colorScheme.surface` | `shapes.extraLarge` | |
| Destructive confirm `TextButton` | — | `typography.labelMedium` | `colorScheme.error` | — | In both dialogs |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
data class SettingsUiState(
    val settings: AppSettings,
    val busyAction: BusyAction,   // None | ClearingHistory | Resetting
    val actionError: String?
)
```

`SettingsScreen` has no `Loading`, `Empty`, or `Error` top-level state — settings are read synchronously from DataStore and always have defaults.

| State | What renders |
| --- | --- |
| `busyAction = None` | Full `SettingsContent` with all interactive elements enabled. No spinners. |
| `busyAction = ClearingHistory` | `ClearHistoryRow` trailing shows small `CircularProgressIndicator`; row is non-clickable; `ResetAppRow` also disabled to prevent concurrent destructive actions. |
| `busyAction = Resetting` | `ResetAppRow` trailing shows spinner; both destructive rows non-clickable. |
| `actionError != null` | `ActionErrorBanner` appears below the relevant row (or at bottom of list). Live region announces it. Dismiss emits `OnDismissError`. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnBack` | Back arrow; system Back |
| `OnTimeoutChanged(Int)` | `Slider.onValueChangeFinished` |
| `OnFollowRedirectsToggled(Boolean)` | `Switch.onCheckedChange` |
| `OnClearHistoryConfirmed` | Confirm in `ClearHistoryDialog` |
| `OnResetConfirmed` | Confirm in `ResetAppDialog` |
| `OnDismissError` | "Dismiss" in `ActionErrorBanner` |

---

## @Preview targets

On stateless `SettingsContent` composable:

1. `SettingsContent_Light` — light theme, default settings (timeout=30, follow redirects on), no error.
2. `SettingsContent_Dark` — dark theme, same defaults.
3. `SettingsContent_BusyClearing` — light theme, `busyAction = ClearingHistory`, spinner in clear row.
4. `SettingsContent_ActionError` — light theme, `actionError` message visible with live-region banner.

---

## Accessibility

- `TimeoutSettingRow` `Slider`: add `semantics { contentDescription = stringResource(R.string.cd_timeout_slider, value) }` (e.g. "Request timeout slider, currently 30 seconds"); also `setProgress` accessibility action for keyboard/switch-access.
- `FollowRedirectsRow` `Switch`: label is provided by the `ListItem` `headlineContent`; `Switch` itself needs `contentDescription = null` (row provides context).
- `ClearHistoryRow` and `ResetAppRow`: headline color `colorScheme.error` communicates danger visually; add `semantics { role = Role.Button }` and explicit `contentDescription` that mentions "destructive action" for screen reader users.
- Confirmation dialogs: focus moves to dialog on open; "Cancel" is the default focused button per M3 dialog guidance.
- `ActionErrorBanner` "Dismiss" `TextButton`: min 48×48dp touch target.
- Back `IconButton`: `contentDescription = stringResource(R.string.cd_navigate_back)`.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.titleSmall`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`, `typography.titleMedium`
- Color: `colorScheme.surface`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.error`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`
- Shape: `shapes.small`, `shapes.extraLarge`
