# UI Spec — Docs

## Composable hierarchy

- `DocsIndexScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `title`: `Text(stringResource(R.string.docs_title))` — `typography.titleLarge`
    - `actions`:
      - `IconButton(Icons.Default.Search)` — toggles `SearchField` inline; `contentDescription = stringResource(R.string.cd_search_docs)`
  - `content`:
    - `Column`
      - `SearchField` — `OutlinedTextField`; shown when search icon is toggled; placeholder `stringResource(R.string.docs_search_placeholder)`; `onValueChange`: `OnQueryChanged`; trailing `IconButton(Icons.Default.Close)` clears query and closes field
      - Conditional based on `DocsUiState`:
        - `DocsLoadingContent`
        - `DocsContent`
        - `DocsErrorContent`

### `DocsContent`

- `LazyColumn`
  - `item`: `DocsSectionCard` for each of the four sections in `uiState.sections`

### `DocsSectionCard`

- `Card` (`CardDefaults.cardElevation()`, `shapes.medium`, full-width with `spacing_md` horizontal + `spacing_sm` vertical margins)
  - `modifier`: `Modifier.clickable { OnSectionClicked(section.id) }`
  - `Row` (padding `spacing_md`, vertically centered, `spacing_md` gap)
    - `SectionIcon` — `Icon` per `DocsKind`:
      - `METHODS`: `Icons.Default.Http` — `colorScheme.primary`
      - `STATUS_CODES`: `Icons.Default.Numbers` — `colorScheme.tertiary`
      - `HEADERS`: `Icons.Default.ViewList` — `colorScheme.secondary`
      - `BODY_FORMATS`: `Icons.Default.DataObject` — `colorScheme.tertiary`
      - `contentDescription = null` (decorative; card text provides context)
    - `Column` (weight 1)
      - `Text(section.title)` — `typography.titleMedium`, `colorScheme.onSurface`
      - `Text(stringResource(sectionSubtitleRes(section.kind)))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`
    - `Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)` — `colorScheme.onSurfaceVariant`

### `DocsLoadingContent`

- `LazyColumn`
  - `items(4)`: `DocsSectionSkeletonCard` — shimmer `Card`-shaped placeholder (`colorScheme.surfaceVariant`, `shapes.medium`)

### `DocsErrorContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(stringResource(R.string.docs_error_message))` — `typography.bodyLarge`
    - `OutlinedButton(stringResource(R.string.retry))` — emits `OnRetry`

Note: per the UX spec, the Docs screen content is bundled in-app and has no Loading state from the product perspective. The `DocsLoadingContent` variant covers the brief initial suspending call to `GetDocsIndexUseCase` but should resolve near-instantly.

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `DocsIndexScreen` (Scaffold) | — | — | `colorScheme.surface` | — | Bottom nav tab; no EnvironmentChip (docs are offline-static) |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Search toggle in actions |
| `SearchField` (`OutlinedTextField`) | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | Shown/hidden via `AnimatedVisibility` |
| `DocsSectionCard` (`Card`) | `spacing_md` (card content) | — | `colorScheme.surface` | `shapes.medium` | `spacing_md` horizontal + `spacing_sm` vertical margin from LazyColumn |
| Section `Icon` (leading) | — | — | Per-kind color (see hierarchy) | — | `contentDescription = null` |
| Section title `Text` | — | `typography.titleMedium` | `colorScheme.onSurface` | — | |
| Section subtitle `Text` | `spacing_xs` top | `typography.bodySmall` | `colorScheme.onSurfaceVariant` | — | Short descriptive string |
| Trailing arrow `Icon` | — | — | `colorScheme.onSurfaceVariant` | — | `contentDescription = null` (decorative) |
| `DocsSectionSkeletonCard` | `spacing_md` | — | `colorScheme.surfaceVariant` | `shapes.medium` | Shimmer |
| `DocsErrorContent` icon | — | — | `colorScheme.error` | — | `contentDescription = null` |
| Retry `OutlinedButton` | `spacing_md` top | `typography.labelMedium` | `colorScheme.primary` | `shapes.small` | |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface DocsUiState {
    data object Loading
    data class Content(val sections: List<DocsSection>, val query: String)
    data object Error
}
```

| State | What renders |
| --- | --- |
| `Loading` | `DocsLoadingContent` — 4 skeleton cards. Search icon visible but disabled. |
| `Content` with empty `query` | `DocsContent` — all 4 section cards. |
| `Content` with non-empty `query` | `DocsContent` — only matching sections (filtered by `SearchDocsUseCase`). If no match, centered "No results for '…'" `Text` in `typography.bodyLarge`, `colorScheme.onSurfaceVariant`. |
| `Error` | `DocsErrorContent` — error icon + message + Retry. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnSectionClicked(id)` | Tap on a `DocsSectionCard` |
| `OnQueryChanged(String)` | Typing in `SearchField` `OutlinedTextField` |

---

## @Preview targets

On stateless `DocsContent` composable:

1. `DocsContent_Light` — light theme, all 4 sections, no search.
2. `DocsContent_Dark` — dark theme.
3. `DocsContent_SearchFiltered` — light theme, query filtering to 2 matching sections.
4. `DocsContent_SearchNoResults` — light theme, query with no matches.
5. `DocsContent_Loading` — light theme, skeleton cards.
6. `DocsContent_Error` — light theme, error state.

---

## Accessibility

- Each `DocsSectionCard` is a single tappable unit; `Modifier.semantics { role = Role.Button; contentDescription = "${section.title}. ${sectionSubtitle}" }` for a coherent screen-reader announcement.
- Section icons are all `contentDescription = null`; meaning is conveyed by title and subtitle text.
- Trailing arrow icons: `contentDescription = null` (decorative; navigation is implied by card role).
- `SearchField`: label/placeholder is the accessible name; close `IconButton` has `contentDescription = stringResource(R.string.cd_clear_search)`.
- Search toggle `IconButton`: `contentDescription` toggles between `stringResource(R.string.cd_open_search)` and `stringResource(R.string.cd_close_search)` based on visibility state.
- When search field appears, move accessibility focus to it via `FocusRequester`.
- Skeleton cards: `contentDescription = stringResource(R.string.cd_loading)` on the group.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`
- Typography: `typography.titleLarge`, `typography.titleMedium`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.secondary`, `colorScheme.tertiary`, `colorScheme.error`
- Shape: `shapes.small`, `shapes.medium`
- Elevation: `CardDefaults.cardElevation()`
