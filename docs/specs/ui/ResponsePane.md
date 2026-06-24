# UI Spec — ResponsePane

This composable is shared between the phone `Response` tab (hosted inside `RequestBuilderScreen`) and the tablet two-pane right panel. It is a stateless composable driven by `ResponseUiState` from `BuilderUiState`.

---

## Composable hierarchy

- `ResponsePane` (`Column`, fills its parent)
  - `ResponseToolbar` — `Row` with `StatusBadge`, `LatencyChip`, spacer, `RawToggle`, `CopyBodyButton`
  - `HorizontalDivider`
  - Conditional body based on `ResponseUiState`:
    - `ResponseLoading` (when `Loading`)
    - `ResponseEmpty` (when `Empty`)
    - `ResponseErrorCard` (when `Failure`)
    - `ResponseContent` (when `Content`)

### `ResponseContent`

- `LazyColumn`
  - `item`: `ResponseHeadersSection` — collapsible `Column` with expand/collapse `IconButton`
    - `items(headers)`: `HeaderDisplayRow` — read-only `Text` key + `Text` value
  - `item`: `TruncationBanner` — shown when `body is BodyUi.Truncated`; `colorScheme.errorContainer` strip
  - `item`: `ResponseBodySection`
    - If `showRaw == false` and JSON: `JsonSyntaxBody` — `SelectionContainer` wrapping syntax-colored `Text` nodes
    - If `showRaw == true` or non-JSON: `MonospaceCodeBlock`
    - If body is malformed JSON: `MalformedJsonNotice` above `MonospaceCodeBlock`

### `ResponseLoading`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center)
    - `CircularProgressIndicator`
    - `Text(stringResource(R.string.sending))` — `typography.bodyMedium`, `colorScheme.onSurfaceVariant`
    - `TextButton(stringResource(R.string.cancel))` — emits `OnCancelSend`

### `ResponseEmpty`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_lg` vertical gap)
    - `Icon(Icons.Default.Send, contentDescription = null)` — large, `colorScheme.outlineVariant`
    - `Text(stringResource(R.string.response_empty_message))` — `typography.bodyLarge`, `colorScheme.onSurfaceVariant`, `textAlign = Center`

### `ResponseErrorCard`

- `Box` (fill, `contentAlignment = Center`)
  - `Card` (`CardDefaults.cardElevation()`, `shapes.medium`, `colorScheme.errorContainer`)
    - `Column` (padding `spacing_md`)
      - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.onErrorContainer`
      - `Text(uiState.reason)` — `typography.bodyLarge`, `colorScheme.onErrorContainer`
      - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetrySend`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `ResponsePane` | — | — | `colorScheme.surface` | — | Fills allocated pane area |
| `ResponseToolbar` | `spacing_sm` vertical, `spacing_md` horizontal | — | `colorScheme.surface` | — | `Row` with `spacing_sm` item gap |
| `StatusBadge` | `spacing_xs` horizontal, `spacing_xs` vertical | `typography.labelMedium` | Per-class mapping (see index) | `shapes.small` | Not shown in `Empty`/`Failure` states |
| `LatencyChip` (`AssistChip`) | `spacing_xs` | `typography.labelMedium` | `colorScheme.surfaceVariant` / `colorScheme.onSurfaceVariant` | `shapes.small` | Shows `{n} ms`; not shown in `Empty`/`Failure` |
| `RawToggle` (`FilterChip`) | `spacing_xs` | `typography.labelMedium` | `colorScheme.secondaryContainer` when selected | `shapes.small` | Emits `OnToggleRawBody` |
| `CopyBodyButton` (`IconButton`) | — | — | `colorScheme.onSurface` | — | `Icon(Icons.Default.ContentCopy)`; `contentDescription = stringResource(R.string.cd_copy_body)` |
| `ResponseHeadersSection` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.titleSmall` | `colorScheme.surface` | — | Expand/collapse toggle icon |
| `HeaderDisplayRow` | `spacing_md` horizontal, `spacing_xs` vertical | `typography.bodySmall` key bold / `typography.bodySmall` value | `colorScheme.onSurface` | — | Read-only; long-press on value for copy |
| `TruncationBanner` | `spacing_md` | `typography.bodySmall` | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` | `shapes.small` | "Body too large — showing first 500 KB" + `TextButton` "Copy full body" |
| `JsonSyntaxBody` | `spacing_md` | `typography.bodySmall` monospace | `syntaxHighlight.*` tokens per span type | — | Inside `SelectionContainer`; horizontally scrollable |
| `MonospaceCodeBlock` | `spacing_md` | `typography.bodySmall` monospace | `colorScheme.surface` | `shapes.small` | Horizontal scroll; see shared component spec |
| `MalformedJsonNotice` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodySmall` | `colorScheme.surfaceVariant` / `colorScheme.onSurfaceVariant` | `shapes.small` | "Not valid JSON — showing raw" |
| `ResponseLoading` | — | `typography.bodyMedium` | `colorScheme.onSurfaceVariant` | — | Centered; Cancel button below spinner |
| `ResponseEmpty` | `spacing_xl` | `typography.bodyLarge` | `colorScheme.onSurfaceVariant` | — | Centered illustration placeholder |
| `ResponseErrorCard` | `spacing_lg` (outer box) / `spacing_md` (card) | `typography.bodyLarge` | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` | `shapes.medium` | Retry button below message |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface ResponseUiState {
    data object Empty : ResponseUiState
    data object Loading : ResponseUiState
    data class Content(val status: StatusUi, val latencyMs: Long, val headers: List<KeyValue>,
                       val body: BodyUi, val showRaw: Boolean) : ResponseUiState
    data class Failure(val reason: String, val kind: FailureKind) : ResponseUiState
}
sealed interface BodyUi {
    data class Pretty(val text: String) : BodyUi
    data class Raw(val text: String) : BodyUi
    data class Truncated(val shown: String, val totalBytes: Long) : BodyUi
}
```

| State | What renders |
| --- | --- |
| `Empty` | `ResponseEmpty` centered placeholder; no toolbar row. On phone this state only shows when the Response tab is manually switched to before any send (tab is disabled — this state should be prevented by tab guard). |
| `Loading` | `ResponseLoading` with spinner, "Sending…" label, and "Cancel" button. Toolbar row is hidden. |
| `Content` | `ResponseToolbar` (status badge + latency chip + raw toggle + copy button) + `ResponseContent` (`LazyColumn` with headers section + body). |
| `Content` with `body = Truncated` | `TruncationBanner` item appears at top of body section. |
| `Content` with `showRaw = true` | `MonospaceCodeBlock` replaces `JsonSyntaxBody` regardless of `isJson`. |
| `Content` with malformed JSON | `MalformedJsonNotice` + `MonospaceCodeBlock` (raw fallback). |
| `Failure` | `ResponseErrorCard` centered with reason text and Retry button. Toolbar row hidden. No status or latency. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnCancelSend` | Tapping "Cancel" `TextButton` in `ResponseLoading` |
| `OnRetrySend` | Tapping "Retry" `OutlinedButton` in `ResponseErrorCard` |
| `OnToggleRawBody` | Tapping `RawToggle` chip in `ResponseToolbar` |
| `OnCopyBody` | Tapping `CopyBodyButton` in `ResponseToolbar` |

---

## @Preview targets

On the stateless `ResponsePane` composable:

1. `ResponsePane_LightContent200` — light theme, `Content` state, 200 status, JSON body with syntax highlight.
2. `ResponsePane_DarkContent200` — dark theme, same state.
3. `ResponsePane_Content404` — light theme, 404 status (`errorContainer`), JSON body.
4. `ResponsePane_ContentRaw` — light theme, `showRaw = true`, `MonospaceCodeBlock` visible.
5. `ResponsePane_ContentTruncated` — light theme, `body = Truncated`, banner visible.
6. `ResponsePane_Loading` — light theme, spinner + Cancel.
7. `ResponsePane_Failure` — light theme, transport error card (e.g. "Timeout after 30 s").
8. `ResponsePane_Empty` — light theme, placeholder.

---

## Accessibility

- `StatusBadge`: `contentDescription = stringResource(R.string.cd_status_badge, code, message)` (e.g. "HTTP status 404 Not Found").
- `LatencyChip`: `contentDescription = stringResource(R.string.cd_latency, ms)` (e.g. "Response latency 342 milliseconds").
- `RawToggle`: `contentDescription = stringResource(R.string.cd_raw_toggle)`.
- `CopyBodyButton`: `contentDescription = stringResource(R.string.cd_copy_body)`.
- `ResponseHeadersSection` expand/collapse `IconButton`: `contentDescription` toggles between `stringResource(R.string.cd_expand_headers)` and `stringResource(R.string.cd_collapse_headers)`.
- `JsonSyntaxBody` is wrapped in `SelectionContainer` so body text is selectable and readable by screen readers as a block via `semantics { contentDescription = "Response body" }`.
- `TruncationBanner` "Copy full body" `TextButton`: min touch target 48×48dp.
- Retry button in `ResponseErrorCard`: min touch target 48×48dp.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`, `spacing_xl`
- Typography: `typography.titleSmall`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`, `colorScheme.outlineVariant`
- Shape: `shapes.small`, `shapes.medium`
- Elevation: `CardDefaults.cardElevation()`
- Custom: `syntaxHighlight.jsonKey`, `syntaxHighlight.jsonString`, `syntaxHighlight.jsonNumber`, `syntaxHighlight.jsonBoolean`, `syntaxHighlight.jsonNull`
