# UI Spec — DocsArticle

## Composable hierarchy

- `DocsArticleScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `navigationIcon`: `IconButton(Icons.AutoMirrored.Default.ArrowBack)` — emits `OnBack`
    - `title`: Conditional
      - `Content` state: `Text(uiState.article.title)` — `typography.titleLarge`
      - Other states: `Text(stringResource(R.string.article_loading_title))` — `typography.titleLarge`, `colorScheme.onSurfaceVariant`
  - `content`:
    - Conditional based on `ArticleUiState`:
      - `ArticleLoadingContent`
      - `ArticleContent`
      - `ArticleErrorContent`

### `ArticleContent`

- `LazyColumn` (padding `spacing_md` horizontal)
  - `item`: `MarkdownContent(uiState.article.markdown)`

### `MarkdownContent`

A composable that renders the bundled Markdown string. Since there is no in-scope third-party Markdown library specified, this composable must be defined as a leaf that the implementer fills using the project's chosen Markdown renderer (e.g. a `WebView` for simplicity, or a custom Compose renderer). The spec defines its interface and visual contract:

- Container: `SelectionContainer` wrapping the rendered output
- Background: `colorScheme.surface`
- Horizontal padding: `spacing_md`
- Paragraph body text: `typography.bodyLarge`, `colorScheme.onSurface`
- Headings (H1): `typography.headlineSmall`, `colorScheme.onSurface`
- Headings (H2): `typography.titleMedium`, `colorScheme.onSurface`
- Headings (H3): `typography.titleSmall`, `colorScheme.onSurface`
- Inline code: `typography.bodySmall` monospace, `colorScheme.secondaryContainer` background, `shapes.small` clip, `spacing_xs` horizontal padding
- Code blocks: `MonospaceCodeBlock` (see shared components in `ui-spec.md`)
- Links: `colorScheme.primary` text color; underline decoration
- Horizontal rules: `HorizontalDivider` with `colorScheme.outlineVariant`
- Lists (bullet/numbered): indented with `spacing_md` left padding; `typography.bodyLarge`

### `ArticleLoadingContent`

- `LazyColumn` (padding `spacing_md` horizontal)
  - `item`: Title skeleton — full-width shimmer `Box` (`colorScheme.surfaceVariant`, `shapes.small`, height `spacing_xl`)
  - `items(6)`: Paragraph skeleton rows — shimmer `Box`s of varying widths and `spacing_sm` height

### `ArticleErrorContent`

- `Box` (fill, `contentAlignment = Center`)
  - `Column` (horizontal center, `spacing_md` gap)
    - `Icon(Icons.Default.ErrorOutline)` — `colorScheme.error`
    - `Text(stringResource(R.string.article_error_message))` — `typography.bodyLarge`, `colorScheme.onSurface`
    - `OutlinedButton(stringResource(R.string.go_back))` — emits `OnBack`

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `DocsArticleScreen` (Scaffold) | — | — | `colorScheme.surface` | — | |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Back nav |
| `ArticleContent` (`LazyColumn`) | `spacing_md` horizontal | — | `colorScheme.surface` | — | Full-width content |
| `MarkdownContent` paragraphs | — | `typography.bodyLarge` | `colorScheme.onSurface` | — | Line height per M3 body defaults |
| `MarkdownContent` H1 | `spacing_sm` bottom | `typography.headlineSmall` | `colorScheme.onSurface` | — | |
| `MarkdownContent` H2 | `spacing_sm` bottom | `typography.titleMedium` | `colorScheme.onSurface` | — | |
| `MarkdownContent` H3 | `spacing_xs` bottom | `typography.titleSmall` | `colorScheme.onSurface` | — | |
| Inline code `Text` | `spacing_xs` horizontal | `typography.bodySmall` monospace | `colorScheme.secondaryContainer` bg / `colorScheme.onSecondaryContainer` text | `shapes.small` | Inline |
| `MonospaceCodeBlock` | `spacing_md` | `typography.bodySmall` monospace | `colorScheme.surface` | `shapes.small` | Horizontal scroll |
| Link `Text` | — | `typography.bodyLarge` | `colorScheme.primary` | — | Underline; opens in external browser or deep-link |
| `HorizontalDivider` (hr) | `spacing_sm` vertical | — | `colorScheme.outlineVariant` | — | |
| Title skeleton | `spacing_sm` vertical | — | `colorScheme.surfaceVariant` | `shapes.small` | Shimmer |
| Paragraph skeleton rows | `spacing_xs` vertical | — | `colorScheme.surfaceVariant` | `shapes.small` | Varying widths for realism |
| `ArticleErrorContent` icon | — | — | `colorScheme.error` | — | `contentDescription = null` |
| Go back `OutlinedButton` | `spacing_md` top | `typography.labelMedium` | `colorScheme.primary` | `shapes.small` | |

---

## State → UI mapping

From `arch-spec.md`:

```kotlin
sealed interface ArticleUiState {
    data object Loading
    data class Content(val article: DocsArticle)
    data object Error
}
```

| State | What renders |
| --- | --- |
| `Loading` | `ArticleLoadingContent` — title skeleton + 6 paragraph skeleton rows. Back button functional. |
| `Content` | `ArticleContent` — `LazyColumn` with `MarkdownContent` rendering the article. |
| `Error` | `ArticleErrorContent` — error icon + message + "Go back" button. Considered a packaging bug per arch spec; user cannot retry in-screen beyond going back. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnBack` | Back arrow `IconButton`; system Back; "Go back" button in `ArticleErrorContent` |

---

## @Preview targets

On stateless `ArticleContent` composable:

1. `ArticleContent_LightContent` — light theme, `Content` with sample Markdown including headings, code blocks, and links.
2. `ArticleContent_DarkContent` — dark theme, same.
3. `ArticleContent_Loading` — light theme, skeleton shimmer rows.
4. `ArticleContent_Error` — light theme, error state.

---

## Accessibility

- Back `IconButton`: `contentDescription = stringResource(R.string.cd_navigate_back)`.
- `MarkdownContent` links: `Modifier.semantics { role = Role.Link; contentDescription = linkText }` on tappable spans; announce target (opens external browser if URL is external).
- `MonospaceCodeBlock`: `semantics { contentDescription = stringResource(R.string.cd_code_block) }` as a group annotation so TalkBack announces "code block" before reading content.
- `SelectionContainer` around article body allows text selection and copy, which satisfies WCAG success criterion 1.4.12 (text resizing compatibility) through system-level text selection.
- Skeleton loading rows: `semantics { contentDescription = stringResource(R.string.cd_loading) }` on the parent `Column`.
- Article `TopAppBar` title: `semantics { heading() }` so screen readers announce it as a page heading.
- Inline code spans: no interactive action; `contentDescription = null` unless the span contains a unique concept (then wrap with meaningful description).

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`, `spacing_xl`
- Typography: `typography.headlineSmall`, `typography.titleMedium`, `typography.titleSmall`, `typography.titleLarge`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurface`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.primary`, `colorScheme.error`, `colorScheme.outlineVariant`
- Shape: `shapes.small`
- Elevation: `CardDefaults.cardElevation()` (if code blocks use `Card` wrapper)
