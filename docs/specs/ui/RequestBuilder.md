# UI Spec — RequestBuilder

## Composable hierarchy

### Phone layout (`Compact` WindowSizeClass)

- `RequestBuilderScreen` (`Scaffold`)
  - `topBar`: `TopAppBar`
    - `title`: `Text(stringResource(R.string.builder_title))` — `typography.titleLarge`
    - `actions`:
      - `EnvironmentChip` — `FilterChip` showing active env name or "No env"
      - `IconButton(Icons.Default.MoreVert)` — overflow menu → Settings / Save / cURL
    - `OfflineBanner` (shown when `uiState.isOffline == true`, pinned below top bar)
  - `content`:
    - `Column`
      - `MethodAndUrlRow` — horizontal `Row` with `MethodChip` + `TokenHighlightField`
      - `InlineErrorBanner` — shown when `uiState.inlineError != null`; `colorScheme.errorContainer` surface
      - `TabRow` (two tabs: Request / Response)
        - Tab 0 `Request` — always enabled
        - Tab 1 `Response` — enabled only when `uiState.responseAvailable == true`; label uses `colorScheme.outline` text when disabled
      - `HorizontalPager` controlled by `uiState.phonePane`
        - Page 0 `RequestEditorContent`
        - Page 1 `ResponseTabContent` — delegates to `ResponsePane` composable
  - `floatingActionButton`: `ExtendedFloatingActionButton` label `stringResource(R.string.send)` + `Icon(Icons.Default.Send)` — enabled when `uiState.sendEnabled`

### `RequestEditorContent`

- `Column`
  - `SecondaryTabRow` (four inner tabs: Headers / Params / Body / Auth)
  - `HorizontalPager`
    - Page 0 `HeadersTab`
    - Page 1 `ParamsTab`
    - Page 2 `BodyTab`
    - Page 3 `AuthTab`

### `HeadersTab`

- `LazyColumn`
  - `items(uiState.draft.headers)`: `KeyValueRow` with `AutocompleteDropdown` on key field
  - `item`: `TextButton(stringResource(R.string.add_header))` — emits `OnAddHeader`

### `ParamsTab`

- `LazyColumn`
  - `items(uiState.draft.params)`: `KeyValueRow`
  - `item`: `TextButton(stringResource(R.string.add_param))` — emits `OnAddParam`

### `BodyTab`

- `Column`
  - `BodyTypeSelectorRow` — `SingleChoiceSegmentedButtonRow` with options None / JSON / Raw / Form / Multipart / Binary
  - Conditional content based on selected body type:
    - `JsonBodyEditor` — `OutlinedTextField` multiline
    - `RawBodyEditor` — `OutlinedTextField` multiline + MIME type field
    - `FormBodyEditor` — `LazyColumn` of `KeyValueRow` + add button
    - `MultipartEditor` — `LazyColumn` of `MultipartPartRow` + add button
    - `BinaryBodySelector` — `OutlinedButton` "Choose file" + `FileRefRow` when file selected
  - `ContextualHelpLink` — `TextButton` with `?` icon → deep-links to DocsArticle on body formats

### `AuthTab`

- `Column`
  - `SingleChoiceSegmentedButtonRow` — No auth / Basic / Bearer
  - Conditional:
    - `BasicAuthFields` — two `OutlinedTextField`s (Username, Password with `visualTransformation = PasswordVisualTransformation()`)
    - `BearerAuthField` — one `OutlinedTextField` (Token, password visual transformation)
  - `Text(stringResource(R.string.auth_encrypted_notice))` — `typography.bodySmall`, `colorScheme.onSurfaceVariant`

### Tablet layout (`Medium`/`Expanded` WindowSizeClass)

- `RequestBuilderScreen` (`Scaffold`)
  - `topBar`: `TopAppBar` (same as phone, no Request/Response `TabRow`)
  - `content`:
    - `Row` with `VerticalDivider`
      - Left pane (weight 1): `Column`
        - `MethodAndUrlRow`
        - `InlineErrorBanner`
        - `SecondaryTabRow` (Headers / Params / Body / Auth)
        - `HorizontalPager`
      - Right pane (weight 1): `ResponsePane`
  - `floatingActionButton`: `ExtendedFloatingActionButton` (same as phone, positioned in the left-pane scaffold area)

---

## Component breakdown

| Composable | Padding | Typography | Color | Shape | Notes |
| --- | --- | --- | --- | --- | --- |
| `RequestBuilderScreen` (Scaffold background) | — | — | `colorScheme.surface` | — | Root scaffold |
| `TopAppBar` | — | `typography.titleLarge` | `colorScheme.surface` | — | Standard M3 `TopAppBar` |
| `EnvironmentChip` (`FilterChip`) | `spacing_sm` horizontal | `typography.labelMedium` | `colorScheme.secondaryContainer` / `colorScheme.onSecondaryContainer` | `shapes.small` | Tapping opens `EnvironmentSwitcher` |
| `OfflineBanner` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.labelMedium` | `colorScheme.surfaceVariant` / `colorScheme.onSurfaceVariant` | — | Pinned below top bar; full-width |
| `MethodAndUrlRow` | `spacing_md` horizontal, `spacing_sm` vertical | — | — | — | `Row` with `spacing_sm` gap |
| `MethodChip` | `spacing_sm` | `typography.labelMedium` | Per-method mapping (see index) | `shapes.small` | Tapping opens method picker `DropdownMenu` |
| `TokenHighlightField` | `spacing_sm` | `typography.bodyLarge` | `colorScheme.surface` outline `colorScheme.outline` | `shapes.small` | Weight 1 in row; `{{var}}` spans highlighted with `colorScheme.secondaryContainer` |
| `InlineErrorBanner` | `spacing_md` horizontal, `spacing_sm` vertical | `typography.bodySmall` | `colorScheme.errorContainer` / `colorScheme.onErrorContainer` | `shapes.small` | Dismiss `X` icon emits `OnDismissInlineError` |
| `TabRow` (Request/Response) | — | `typography.titleSmall` | `colorScheme.surface` / `colorScheme.primary` indicator | — | Disabled tab uses `colorScheme.outline` text |
| `SecondaryTabRow` (Headers/Params/Body/Auth) | — | `typography.titleSmall` | `colorScheme.surface` | — | Standard M3 `SecondaryTabRow` |
| `KeyValueRow` | `spacing_sm` vertical, `spacing_md` horizontal | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | Key field weight 1, value field weight 2; delete icon `colorScheme.error`; enabled `Switch` |
| `AutocompleteDropdown` | `spacing_xs` | `typography.bodyMedium` | `colorScheme.surface` | `shapes.medium` | `DropdownMenu` elevation `CardDefaults.cardElevation()` |
| `BodyTypeSelectorRow` | `spacing_md` | `typography.labelMedium` | — | `shapes.small` | `SingleChoiceSegmentedButtonRow` |
| `JsonBodyEditor` (`OutlinedTextField`) | `spacing_md` | `typography.bodyMedium` monospace | `colorScheme.surface` | `shapes.small` | Multiline; fills remaining height |
| `BinaryBodySelector` | `spacing_md` | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | File name + size shown in `FileRefRow` after pick |
| `BasicAuthFields` / `BearerAuthField` | `spacing_md` | `typography.bodyMedium` | `colorScheme.surface` | `shapes.small` | Password fields use `PasswordVisualTransformation` |
| `AuthEncryptedNotice` | `spacing_md` horizontal, `spacing_sm` top | `typography.bodySmall` | `colorScheme.onSurfaceVariant` | — | Informational only |
| `ExtendedFloatingActionButton` | — | `typography.labelMedium` | `colorScheme.primary` / `colorScheme.onPrimary` | `shapes.large` | Disabled visual state when `sendEnabled == false` |

---

## State → UI mapping

From `arch-spec.md`, the screen's `UiState` is `BuilderUiState` with a nested `draftStatus`:

```kotlin
data class BuilderUiState(
    val draftStatus: DraftStatus,        // Loading | Ready | HydrateError
    val draft: RequestDraftUi,
    val activeTab: BuilderTab,
    val phonePane: BuilderPane,          // REQUEST | RESPONSE
    val responseAvailable: Boolean,
    val sendState: SendState,
    val response: ResponseUiState,
    val inlineError: String?,
    val isOffline: Boolean,
    val sendEnabled: Boolean
)
```

| State | What renders |
| --- | --- |
| `draftStatus = Loading` | The `MethodAndUrlRow` area renders skeleton shimmer shapes (`colorScheme.surfaceVariant`, `shapes.small`) in place of the method chip and URL field. Inner tab pagers show a single `CircularProgressIndicator` centered in a `Box`. FAB is disabled. |
| `draftStatus = HydrateError` | Full-screen `Column` centered: `Icon(Icons.Default.Error, colorScheme.error)` + `typography.bodyLarge` message + `OutlinedButton("Retry")`. FAB is hidden. |
| `draftStatus = Ready` + empty URL (Empty equivalent) | Normal composable hierarchy renders. `sendEnabled = false` disables the FAB. Response tab is grayed out with `colorScheme.outline` text. Inner tabs show their respective "add" `TextButton` CTAs (no rows). |
| `draftStatus = Ready` + content | Full composable hierarchy with populated fields, rows, and active FAB. |
| `inlineError != null` | `InlineErrorBanner` appears below `MethodAndUrlRow`. Does not hide content. |
| `isOffline == true` | `OfflineBanner` pinned below `TopAppBar`; FAB remains visible but tapping while offline emits inline error. |
| `sendState = Sending` | FAB shows `CircularProgressIndicator` replacing `Icon`; URL field is read-only; a `Cancel` `TextButton` appears near the FAB or as an overlay. |
| `sendState = Done` (phone) | Auto-switch to `phonePane = RESPONSE`; `responseAvailable = true` activates Response tab. |

---

## Events

| Event | Trigger |
| --- | --- |
| `OnMethodSelected(HttpMethod)` | Tapping a method item in the `DropdownMenu` opened by `MethodChip` |
| `OnUrlChanged(String)` | Text change in `TokenHighlightField` |
| `OnTabSelected(BuilderTab)` | Tapping any inner tab (Headers/Params/Body/Auth) |
| `OnHeaderEdited(index, KeyValue)` | Editing key or value in a `KeyValueRow` within `HeadersTab` |
| `OnAddHeader` | Tapping "Add header" `TextButton` |
| `OnHeaderKeyTyping(index, prefix)` | Typing in a header key field (triggers autocomplete) |
| `OnParamEdited(index, KeyValue)` | Editing a `KeyValueRow` in `ParamsTab` |
| `OnAddParam` | Tapping "Add param" `TextButton` |
| `OnBodyTypeChanged(bodyType)` | Selecting a segment in `BodyTypeSelectorRow` |
| `OnBodyTextChanged(String)` | Typing in `JsonBodyEditor` or `RawBodyEditor` |
| `OnPickFile` | Tapping "Choose file" `OutlinedButton` in `BinaryBodySelector` |
| `OnFilePicked(FileRef)` | SAF result callback |
| `OnAuthTypeChanged(AuthType)` | Selecting No auth / Basic / Bearer segment |
| `OnAuthFieldChanged(field, value)` | Typing in username / password / token field |
| `OnSendClicked` | Tapping `ExtendedFloatingActionButton` |
| `OnCancelSend` | Tapping Cancel during `Sending` state |
| `OnPhonePaneChanged(BuilderPane)` | Tapping Request or Response tab (phone only) |
| `OnRequestSave` | Tapping "Save" in overflow menu |
| `OnRequestCurl` | Tapping "cURL" in overflow menu |
| `OnDismissInlineError` | Tapping `X` on `InlineErrorBanner` |

---

## @Preview targets

On the stateless `RequestBuilderContent` composable:

1. `RequestBuilderContent_LightReady` — light theme, `draftStatus = Ready`, populated draft, `sendEnabled = true`.
2. `RequestBuilderContent_DarkReady` — dark theme, same populated draft.
3. `RequestBuilderContent_Loading` — light theme, `draftStatus = Loading` skeleton.
4. `RequestBuilderContent_Offline` — light theme, `isOffline = true`, `OfflineBanner` visible.
5. `RequestBuilderContent_InlineError` — light theme, `inlineError = "{{baseId}} has no value..."`.
6. `RequestBuilderContent_Sending` — light theme, `sendState = Sending`, FAB spinner visible.

Use `@PreviewParameter(BuilderStateProvider::class)` to cover the full state matrix.

---

## Accessibility

- `MethodChip`: `contentDescription = stringResource(R.string.cd_method_chip, method.name)` (e.g. "HTTP method: POST. Tap to change.").
- `TokenHighlightField`: `label` set to `stringResource(R.string.request_url_label)`; unresolved `{{var}}` spans have a `semantics { contentDescription = "Unresolved variable: varName" }` annotation.
- `ExtendedFloatingActionButton`: `contentDescription = stringResource(R.string.cd_send_button)` when disabled, `contentDescription = stringResource(R.string.cd_sending)` when in `Sending` state.
- `KeyValueRow` delete `IconButton`: `contentDescription = stringResource(R.string.cd_delete_row, key)`.
- `AutocompleteDropdown` items: must be reachable via `Tab`/D-pad traversal; each item has a `Modifier.semantics { role = Role.DropdownListItem }`.
- All touch targets are ≥ 48×48dp. The `MethodChip` and delete icon buttons must use `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)` — flag to implementer.
- Decorative `Icon(Icons.Default.Send)` in FAB: `contentDescription = null` (label provides meaning).
- Response tab when disabled: `semantics { disabled(); contentDescription = stringResource(R.string.cd_response_tab_disabled) }`.

---

## Tokens used (audit)

- Spacing: `spacing_xs`, `spacing_sm`, `spacing_md`, `spacing_lg`
- Typography: `typography.titleLarge`, `typography.titleSmall`, `typography.bodyLarge`, `typography.bodyMedium`, `typography.bodySmall`, `typography.labelMedium`
- Color: `colorScheme.surface`, `colorScheme.surfaceVariant`, `colorScheme.onSurfaceVariant`, `colorScheme.primary`, `colorScheme.onPrimary`, `colorScheme.secondaryContainer`, `colorScheme.onSecondaryContainer`, `colorScheme.tertiary`, `colorScheme.onTertiary`, `colorScheme.errorContainer`, `colorScheme.onErrorContainer`, `colorScheme.error`, `colorScheme.outline`, `colorScheme.inverseSurface`, `colorScheme.inverseOnSurface`
- Shape: `shapes.small`, `shapes.medium`, `shapes.large`
- Elevation: `CardDefaults.cardElevation()`
