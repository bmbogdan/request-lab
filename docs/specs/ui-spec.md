# UI Spec — Developer API Explorer

Per-screen specs live under `docs/specs/ui/`. Every screen in `docs/specs/ux-spec.md` has an entry here.

---

## Navigation scaffold

### Phone (Compact WindowSizeClass)
- `Scaffold` with a `NavigationBar` (`NavigationBarDefaults.Elevation`) hosting four items: **Builder**, **History**, **Collections**, **Docs**.
- Each item uses `NavigationBarItem` with an `Icon` and a `label`.
- **Settings** is reached from the top-app-bar overflow `DropdownMenu`, not a nav-bar tab.
- The **active environment chip** (`AssistChip` or `FilterChip` with `MaterialTheme.colorScheme.secondaryContainer` background) sits in the `TopAppBar` `actions` slot on Builder, History, and Collections screens. It is absent from Docs and Settings.
- On phone the Builder screen hosts two pager tabs (`Request` / `Response`) rendered via `TabRow` inside the screen content area; these are not navigation destinations.

### Tablet (Medium / Expanded WindowSizeClass)
- `AdaptiveScaffold` (from `material3-adaptive`) replaces `NavigationBar` with a `NavigationRail` (`NavigationRailDefaults.ContainerColor`).
- Builder is a two-pane layout: request editor pane (left) + response pane (right) with a `VerticalDivider`.
- History and Collections may be two-pane (list left, detail right) using `ListDetailPaneScaffold`.
- Window-size detection uses `calculateWindowSizeClass()` / `WindowSizeClass` from `material3-adaptive`.

---

## Shared / special components

| Component | Composable name | Key tokens |
| --- | --- | --- |
| HTTP method chip | `MethodChip` | Colored `SuggestionChip`; color per method — see table below |
| Status badge | `StatusBadge` | `FilterChip`-style pill; color per status class — see table below |
| Token-highlight URL field | `TokenHighlightField` | `OutlinedTextField`; `{{var}}` spans use `MaterialTheme.colorScheme.secondaryContainer` + `MaterialTheme.colorScheme.onSecondaryContainer` |
| Key-value row | `KeyValueRow` | Two `OutlinedTextField`s + `Switch` + `IconButton(Icons.Default.Delete)`; padding `spacing_sm` |
| Autocomplete dropdown | `AutocompleteDropdown` | `DropdownMenu` anchored below the active field; items use `typography.bodyMedium` |
| Monospace code block | `MonospaceCodeBlock` | `HorizontalScrollable` `SelectionContainer` wrapping `Text`; `typography.bodySmall` + monospace font family; `colorScheme.surface` background; `shapes.small` clip |
| Offline banner | `OfflineBanner` | Full-width `Surface` with `colorScheme.surfaceVariant`; `typography.labelMedium`; `colorScheme.onSurfaceVariant` text |
| Drag-handle row | `DragHandleRow` | Leading `Icon(Icons.Default.DragHandle)`; `colorScheme.onSurfaceVariant`; row uses `spacing_md` horizontal padding |

### MethodChip color mapping

| Method | Chip colors |
| --- | --- |
| GET | `colorScheme.tertiary` container / `colorScheme.onTertiary` label |
| POST | `colorScheme.primary` container / `colorScheme.onPrimary` label |
| PUT | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` |
| PATCH | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` |
| DELETE | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` |
| HEAD | `colorScheme.outline` border / `colorScheme.onSurface` label (outlined style) |
| OPTIONS | `colorScheme.outline` border / `colorScheme.onSurface` label (outlined style) |

### StatusBadge color mapping

| Status class | Badge colors |
| --- | --- |
| 2xx | `colorScheme.primary` / `colorScheme.onPrimary` |
| 3xx | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` |
| 4xx | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` |
| 5xx | `colorScheme.inverseSurface` / `colorScheme.inverseOnSurface` |
| `FAILED` | `colorScheme.surfaceVariant` / `colorScheme.onSurface` |

---

## New design tokens needed

The following tokens are **not** in the standard M3 palette and must be added to the design-system before implementers reference them.

### JSON syntax-highlight colors (custom, theme-aware)

| Token name | Light theme suggestion | Usage |
| --- | --- | --- |
| `syntaxHighlight.jsonKey` | amber/orange tonal | JSON object key text |
| `syntaxHighlight.jsonString` | green tonal | JSON string value text |
| `syntaxHighlight.jsonNumber` | blue tonal | JSON number / float value text |
| `syntaxHighlight.jsonBoolean` | purple tonal | `true` / `false` value text |
| `syntaxHighlight.jsonNull` | `colorScheme.outline` | `null` value text |

These must be defined as a `JsonSyntaxColors` data class in `core/designsystem` and provided via `CompositionLocal`. Raw hex is forbidden here; the design-system must map them to theme-aware color values.

### Additional spacing aliases

| Alias | Value |
| --- | --- |
| `spacing_xs` | 4dp |
| `spacing_sm` | 8dp |
| `spacing_md` | 16dp |
| `spacing_lg` | 24dp |
| `spacing_xl` | 32dp |

---

## Screen index

| Screen | File | Notes |
| --- | --- | --- |
| RequestBuilder | [ui/RequestBuilder.md](ui/RequestBuilder.md) | Two-tab on phone; two-pane on tablet; Send FAB |
| ResponsePane | [ui/ResponsePane.md](ui/ResponsePane.md) | Status badge, syntax highlight, raw toggle |
| History | [ui/History.md](ui/History.md) | Reverse-chronological list; swipe-to-delete; offline banner |
| HistoryDetail | [ui/HistoryDetail.md](ui/HistoryDetail.md) | Full request + response/error; action bar |
| Collections | [ui/Collections.md](ui/Collections.md) | Collection list; FAB; swipe actions |
| CollectionDetail | [ui/CollectionDetail.md](ui/CollectionDetail.md) | Reorderable saved-request list |
| SaveToCollectionSheet | [ui/SaveToCollectionSheet.md](ui/SaveToCollectionSheet.md) | `ModalBottomSheet`; collection picker + name field |
| Environments | [ui/Environments.md](ui/Environments.md) | Environment list; active checkmark |
| EnvironmentDetail | [ui/EnvironmentDetail.md](ui/EnvironmentDetail.md) | Key-value table; encrypted-values notice |
| EnvironmentSwitcher | [ui/EnvironmentSwitcher.md](ui/EnvironmentSwitcher.md) | Popup from toolbar chip |
| CurlExportSheet | [ui/CurlExportSheet.md](ui/CurlExportSheet.md) | `ModalBottomSheet`; monospace command; credentials toggle |
| Settings | [ui/Settings.md](ui/Settings.md) | Timeout slider; redirects switch; destructive actions |
| Docs | [ui/Docs.md](ui/Docs.md) | Four section cards; optional search |
| DocsArticle | [ui/DocsArticle.md](ui/DocsArticle.md) | Scrollable Markdown content |
