# UI Spec — EnvironmentSwitcher

This is a lightweight popup that appears anchored to the `EnvironmentChip` in the `TopAppBar`. It is not a full screen and has no back-stack entry. System Back or an outside tap dismisses it.

## Composable hierarchy

The switcher is rendered as a `DropdownMenu` anchored to the `EnvironmentChip` trigger in the `TopAppBar` `actions` slot.

- `EnvironmentChip` (`FilterChip`) — trigger in `TopAppBar`
  - `label`: `Text(activeEnvName ?: stringResource(R.string.no_environment))` — `typography.labelMedium`
  - `leadingIcon`: `Icon(Icons.Default.Layers, contentDescription = null)` — decorative
  - `selected`: `activeId != null`
  - `onClick`: opens `EnvironmentSwitcherDropdown`

- `EnvironmentSwitcherDropdown` (`DropdownMenu`, `expanded = uiState.isOpen`)
  - `DropdownMenuItem` — "No environment"
    - `leadingIcon`: `Icon(Icons.Default.RadioButtonChecked or RadioButtonUnchecked)` based on `activeId == null`
    - `text`: `Text(stringResource(R.string.no_environment))` — `typography.bodyMedium`
    - `onClick`: `OnSelect(null)`
  - `HorizontalDivider`
  - For each `environment` in `uiState.environments`:
    - `DropdownMenuItem`
      - `leadingIcon`: `Icon(Icons.Default.CheckCircle if active, else Icons.Default.RadioButtonUnchecked)` — `colorScheme.primary` when active, `colorScheme.onSurfaceVariant` when not
      - `text`: `Text(environment.name)` — `typography.bodyMedium`; active env additionally displays `typography.labelSmall` `colorScheme.primary` "Active" chip or text below the name (optional; do not overlap touch target)
      - `onClick`: `OnSelect(environment.id)`
  - `HorizontalDivider`
  - `DropdownMenuItem` — "Manage environments"
    - `leadingIcon`: `Icon(Icons.Default.Settings, contentDescription = null)`
    - `text`: `Text(stringResource(R.string.manage_environments))` — `typography.bodyMedium`
    - `onClick`: `OnManageEnvironments`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `EnvironmentChip` (`FilterChip`) | `spacing_sm` horizontal | `typography.labelMedium` | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` when selected; `colorScheme.surface` unselected | `shapes.small` | Trigger; present on Builder, History, Collections top bars |
| `EnvironmentSwitcherDropdown` (`DropdownMenu`) | — | — | `colorScheme.surface` | `shapes.medium` | `CardDefaults.cardElevation()` applies via M3 `DropdownMenu` |
| "No environment" `DropdownMenuItem` | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.onSurface` | — | Min 48dp height |
| Environment `DropdownMenuItem` | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.onSurface`; active row `colorScheme.secondaryContainer` background | — | Min 48dp height |
| Active checkmark `Icon` (leading) | — | — | `colorScheme.primary` | — | `contentDescription = stringResource(R.string.cd_active_env)` |
| Inactive radio `Icon` (leading) | — | — | `colorScheme.onSurfaceVariant` | — | `contentDescription = null` |
| "Manage environments" `DropdownMenuItem` | `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.onSurface` | — | Settings icon leading |
| `HorizontalDivider` | — | — | `colorScheme.outlineVariant` | — | Between sections |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface SwitcherUiState {
    data class Content(val environments: List<Environment>, val activeId: String?)
}
```

This component has no `Loading` or `Error` state — it operates on the already-loaded environment list held in memory by `EnvironmentSwitcherViewModel`.

| State | What renders |
| --- | --- |
| `Content` with empty `environments` list | Dropdown shows only "No environment" item + divider + "Manage environments". |
| `Content` with environments | Dropdown shows "No environment" + divider + environment list rows + divider + "Manage environments". Active env row has checkmark leading icon and `colorScheme.secondaryContainer` background tint. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnSelect(id?)` | Tapping any environment `DropdownMenuItem` or "No environment" |
| `OnManageEnvironments` | Tapping "Manage environments" `DropdownMenuItem` |

---

## @Preview targets

On a standalone preview of `EnvironmentSwitcherDropdown`:

1. `EnvironmentSwitcher_LightNoEnv` — light theme, no active environment, 2 environments listed.
2. `EnvironmentSwitcher_DarkWithActive` — dark theme, one active environment highlighted.
3. `EnvironmentSwitcher_Empty` — light theme, no environments defined (only "No environment" + manage link).

---

## Accessibility

- `EnvironmentChip`: `contentDescription = stringResource(R.string.cd_env_chip, activeEnvName ?: "None")` (e.g. "Active environment: staging. Tap to switch.").
- Each `DropdownMenuItem`: accessible name from its `text` composable; leading icon `contentDescription` as noted in table above.
- Active environment row: `semantics { selected = true }` so TalkBack announces "selected".
- The entire dropdown must be keyboard/D-pad navigable; `DropdownMenu` handles this via M3 defaults.
- Touch targets: each `DropdownMenuItem` is ≥ 48dp in height by M3 default.

---

## Tokens used (audit)

- Spacing: `spacing_sm`
- Typography: `typography.labelMedium`, `typography.bodyMedium`, `typography.labelSmall`
- Color: `colorScheme.surface`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.primary`, `colorScheme.outlineVariant`
- Shape: `shapes.small`, `shapes.medium`
- Elevation: `CardDefaults.cardElevation()`
