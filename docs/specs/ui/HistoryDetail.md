# UI Spec — HistoryDetail

## Composable hierarchy

- `HistoryDetailScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `navigationIcon`: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` — emits `OnBack`
    - `title`: `Text(stringResource(R.string.history_detail_title))` — `typography.titleLarge`
    - `actions`:
      - `IconButton(Icons.Default.Replay)` — emits `OnReplay`; `contentDescription = stringResource(R.string.cd_replay)`
      - `IconButton(Icons.Default.MoreVert)` — overflow `DropdownMenu`
        - `DropdownMenuItem` "Duplicate" → `OnDuplicate`
        - `DropdownMenuItem` "cURL" → `OnCurl`
        - `DropdownMenuItem` "Add to collection" → `OnAddToCollection`
        - `DropdownMenuItem` "Delete" → `OnDelete`
  - `content`:
    - Conditional based on `HistoryDetailUiState`:
      - `HistoryDetailLoading`
      - `HistoryDetailContent`
      - `HistoryDetailNotFound`
      - `HistoryDetailError`

### `HistoryDetailContent`

- `LazyColumn`
  - `item`: `RequestSummaryCard`
  - `item`: `ResponseCard` OR `FailureCard` (based on `detail.failure != null`)

### `RequestSummaryCard`

- `Card` (`CardDefaults.cardElevation()`, `shapes.medium`, `colorScheme.surface`)
  - `Column` (padding `spacing_md`)
    - `Row`: `MethodChip` + `Text(detail.entry.resolvedUrl)` — `typography.bodyLarge`, weight 1
    - `HorizontalDivider`
    - `SectionHeader("Headers")` — `typography.titleSmall`, `colorScheme.onSurfaceVariant`
    - `items(detail.request.headers)`: `HeaderDisplayRow`
    - `SectionHeader("Params")` — if params exist
    - `items(detail.request.params)`: `HeaderDisplayRow`
    - `SectionHeader("Body")` — if body is not `None`
    - `RequestBodySummary` — read-only body preview (`MonospaceCodeBlock` for JSON/raw, field list for Form)
    - `SectionHeader("Auth")` — if auth is not `None`
    - `AuthSummaryRow` — shows auth type + redacted credentials (username only for Basic, "Bearer [redacted]")
    - `EnvironmentChip` (small, read-only) — shown when `entry.environmentName != null`

### `ResponseCard`

- `Card` (`CardDefaults.cardElevation()`, `shapes.medium`, `colorScheme.surface`)
  - `Column` (padding `spacing_md`)
    - `Row`: `StatusBadge` + `LatencyChip` + spacer + `Text(relativeTimestamp)` — `typography.labelSmall`
    - `HorizontalDivider`
    - `ResponseHeadersSection` (collapsible, same as in `ResponsePane`)
    - `ResponseBodySection` (same `JsonSyntaxBody` / `MonospaceCodeBlock` logic as `ResponsePane`)

### `FailureCard`

- `Card` (`CardDefaults.cardElevation()`, `shapes.medium`, `colorScheme.errorContainer`)
  - `Column` (padding `spacing_md`)
    - `Icon(Icons.Default.WifiOff or Icons.Default.ErrorOutline)` — `colorScheme.onErrorContainer`
    - `Text(failure.message)` — `typography.bodyLarge`, `colorScheme.onErrorContainer`
    - `Text(stringResource(R.string.history_detail_failed_hint))` — `typography.bodySmall`, `colorScheme.onErrorContainer`

### `HistoryDetailLoading`

- `LazyColumn`
  - `item`: Skeleton card — shimmer `colorScheme.surfaceVariant`, `shapes.medium`, height ≈ 3 × `spacing_xl`
  - `item`: Skeleton card — smaller shimmer

### `HistoryDetailNotFound`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.SearchOff)` — `colorScheme.onSurfaceVariant`
    - `Text(stringResource(R.string.history_detail_not_found))` — `typography.bodyLarge`, `colorScheme.onSurfaceVariant`
    - `OutlinedButton(stringResource(R.string.go_back))` — emits `OnBack`

### `HistoryDetailError`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(uiState.message)` — `typography.bodyLarge`, `colorScheme.onSurface`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnBack` (exits; no re-fetch on this screen)

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `HistoryDetailScreen` (Scaffold) | — | — | `colorScheme.surface` | — | Root |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Back icon + Replay icon + overflow |
| `RequestSummaryCard` | `spacing_md` (card content) | — | `colorScheme.surface` | `shapes.medium` | Outer `spacing_md` horizontal padding in `LazyColumn` |
| `MethodChip` | `spacing_xs` | `typography.labelMedium` | Per-method | `shapes.small` | Read-only; no click |
| `SectionHeader` | `spacing_sm` vertical | `typography.titleSmall` | `colorScheme.onSurfaceVariant` | — | Bold label above sub-content |
| `HeaderDisplayRow` | `spacing_sm` vertical | `typography.bodySmall` (key) / `typography.bodySmall` (value) | `colorScheme.onSurface` | — | Long-press on value → copy |
| `AuthSummaryRow` | `spacing_sm` | `typography.bodySmall` | `colorScheme.onSurface` | — | Always redacts secrets |
| `EnvironmentChip` (read-only) | `spacing_xs` | `typography.labelSmall` | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` | `shapes.small` | Non-interactive in this context |
| `ResponseCard` | `spacing_md` (card) | — | `colorScheme.surface` | `shapes.medium` | Outer `spacing_md` |
| `StatusBadge` | `spacing_xs` | `typography.labelMedium` | Per-class (see index) | `shapes.small` | |
| `LatencyChip` | `spacing_xs` | `typography.labelMedium` | `colorScheme.surfaceVariant` / `colorScheme.onSurfaceVariant` | `shapes.small` | |
| `FailureCard` | `spacing_md` (card) | `typography.bodyLarge` | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` | `shapes.medium` | Icon + message + hint |
| `HistoryDetailLoading` skeletons | `spacing_md` | — | `colorScheme.surfaceVariant` | `shapes.medium` | Shimmer |
| `HistoryDetailNotFound` | `spacing_xl` | `typography.bodyLarge` | `colorScheme.onSurfaceVariant` | — | Centered |
| `HistoryDetailError` | `spacing_xl` | `typography.bodyLarge` | `colorScheme.onSurface` / `colorScheme.error` icon | — | Centered |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface HistoryDetailUiState {
    data object Loading
    data class Content(val detail: HistoryDetail)
    data object NotFound
    data class Error(val message: String)
}
```

| State | What renders |
| --- | --- |
| `Loading` | `HistoryDetailLoading` — two shimmer skeleton cards. Action bar icons visible but disabled (`alpha` reduced). |
| `Content` (HTTP response) | `RequestSummaryCard` + `ResponseCard`. All action bar items enabled. |
| `Content` (transport failure) | `RequestSummaryCard` + `FailureCard`. Replay, Duplicate, and cURL remain enabled. |
| `NotFound` | `HistoryDetailNotFound` — centered "no longer available" message + Go back button. Action bar hidden. |
| `Error` | `HistoryDetailError` — error message + back button. Action bar icons disabled. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnBack` | Back arrow in `TopAppBar`; system back gesture |
| `OnReplay` | `IconButton(Icons.Default.Replay)` in `TopAppBar` |
| `OnDuplicate` | "Duplicate" in overflow `DropdownMenu` |
| `OnCurl` | "cURL" in overflow `DropdownMenu` |
| `OnAddToCollection` | "Add to collection" in overflow `DropdownMenu` |
| `OnDelete` | "Delete" in overflow `DropdownMenu` |

---

## @Preview targets

On stateless `HistoryDetailContent` composable:

1. `HistoryDetailContent_LightHttpSuccess` — light theme, `Content` with HTTP 200 response card.
2. `HistoryDetailContent_DarkHttpSuccess` — dark theme.
3. `HistoryDetailContent_Http404` — light theme, `Content` with 404 `StatusBadge`.
4. `HistoryDetailContent_Failed` — light theme, `FailureCard` shown (transport error).
5. `HistoryDetailContent_Loading` — light theme, skeleton state.
6. `HistoryDetailContent_NotFound` — light theme, not-found state.

---

## Accessibility

- Back `IconButton`: `contentDescription = stringResource(R.string.cd_navigate_back)`.
- Replay `IconButton`: `contentDescription = stringResource(R.string.cd_replay_request)`.
- All overflow `DropdownMenuItem`s: explicit non-empty text labels.
- `HeaderDisplayRow` long-press to copy: add `CustomAccessibilityAction("Copy value")` semantics.
- `FailureCard` icon: `contentDescription = null` (decorative; message text describes the failure).
- `AuthSummaryRow`: never exposes raw credentials; screen-reader text must not include secret values.
- Skeleton loading cards: `semantics { contentDescription = stringResource(R.string.cd_loading) }`.
- All touch targets ≥ 48×48dp; `TopAppBar` icon buttons satisfy this by default.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`, `spacing_xl`
- Typography: `typography.titleLarge`, `typography.titleSmall`, `typography.bodyLarge`, `typography.bodySmall`, `typography.labelMedium`, `typography.labelSmall`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`, `colorScheme.error`
- Shape: `shapes.small`, `shapes.medium`
- Elevation: `CardDefaults.cardElevation()`
- Custom: `syntaxHighlight.*` (via `ResponseBodySection` reuse)
