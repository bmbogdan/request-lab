# UX Spec — Developer API Explorer

## Summary & goals
Developer API Explorer is a phone-first (tablet-adaptive) Android HTTP client — a "Postman-lite" for solo developers who want to build, send, and inspect API requests on the go. It supports request building (all common methods, headers with autocomplete, params, multiple body types, Basic and Bearer auth), response inspection with JSON pretty-printing, reusable collections and environments with `{{variable}}` substitution, an offline-browsable history, one-tap cURL export, and an in-app HTTP reference. All data lives locally (no accounts, no cloud); sensitive values (Basic credentials, Bearer tokens, environment variable values) are stored encrypted at rest via the Android Keystore. Success looks like a user being able to compose a request, send it, read the response, save it to a collection, switch environments, and export it as cURL — all without signing in and with full history access offline.

## User flows

### Flow: Build and send a request
1. User launches app → lands on RequestBuilder screen (default new request, GET, empty URL). On phone, two top-level tabs are visible — **Request** (active) and **Response** (disabled/grayed out until the first send this session).
2. User taps the method selector → a method picker (GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS) opens → user taps `POST` → picker dismisses, method updates.
3. User taps the URL bar → keyboard opens → user types `https://api.example.com/{{baseId}}/users` → `{{baseId}}` renders as a highlighted token in the field.
4. User taps the `Headers` tab → taps `Add header` → starts typing the key `Cont` → an autocomplete dropdown of well-known header names appears → user taps `Content-Type` → the value field shows an autocomplete dropdown of common values → user taps `application/json`.
5. User taps the `Body` tab → taps the body-type selector → chooses `JSON` → enters a JSON payload in the editor.
6. User taps the `Send` button → button shows a sending/in-progress state; the URL bar is locked during the request.
7. On success: on phone, the **Response** tab activates (no longer grayed out) and the app switches to it, showing status badge (e.g. `200 OK`), latency badge (e.g. `342 ms`), and the body rendered as pretty-printed JSON. On tablet, the right-hand response pane populates in place.
8. The sent request is written to History automatically with method, resolved URL, status, latency, and timestamp.

### Flow: Use environment variables
1. From any screen with the toolbar, user taps the active-environment switcher (shows `No environment` by default).
2. A list of environments opens (e.g. `staging`, `prod`) plus a `Manage environments` entry → user taps `staging` → switcher dismisses and now displays `staging`.
3. User returns to RequestBuilder; `{{baseId}}` tokens in the URL/headers/body are now resolvable from the `staging` variable set and remain visually highlighted.
4. User taps `Send` → the app substitutes `{{baseId}}` with its `staging` value before sending → the resolved URL is what is stored in History.
5. To edit variables, user taps the switcher → `Manage environments` → Environments screen → taps `staging` → adds/edits/deletes key-value pairs → saves. Variable values are encrypted at rest.

### Flow: Save a request into a collection
1. On RequestBuilder with a composed request, user taps the overflow / `Save` action → a `Save to collection` sheet opens.
2. Sheet lists existing collections plus `New collection`. User taps `New collection` → enters a name (e.g. `User API`) → confirms.
3. User optionally names the request (defaults to method + path) → taps `Save` → sheet dismisses with a confirmation snackbar.
4. The request now appears inside the `User API` collection on the Collections screen.

### Flow: Browse and replay from history
1. User taps the `History` tab → sees a reverse-chronological list of sent requests, each row showing method, URL, status (or a `FAILED` chip for transport-level failures), and relative timestamp; a subtle `offline` indicator appears when there is no connectivity.
2. User taps a row → HistoryDetail opens showing the full request and the captured response (or, for a failed attempt, an error card in place of the response section).
3. User taps `Replay` → the request is loaded into the RequestBuilder (method, URL, headers, params, body, auth) ready to send or edit.
4. Alternatively user taps `Duplicate` → the request opens in RequestBuilder as a new unsaved draft (the original is untouched), or `Add to collection` → the Save-to-collection sheet opens.

### Flow: Export as cURL
1. On RequestBuilder (or HistoryDetail), user taps the `cURL` action.
2. A cURL preview sheet opens showing the generated command (with variables already resolved against the active environment). By default credentials are redacted: Basic auth emits `-u [username]:[redacted]`, Bearer auth emits `--header 'Authorization: Bearer [redacted]'`.
3. User optionally flips the `Include credentials` toggle to reveal the real values in the command.
4. User taps `Copy` → the command is copied to clipboard with a confirmation snackbar; OR user taps `Share` → the Android share sheet opens with the command as text.

### Flow: Consult HTTP reference
1. User taps the `Docs` tab → lands on the HTTP reference index with sections: Methods, Status codes, Common headers, Body formats.
2. User taps `Status codes` → a browsable/searchable list of codes with short explanations.
3. Alternatively, from the RequestBuilder a contextual `?` link (e.g. next to the Body type selector) deep-links straight to the relevant Docs section.

### Flow: Inspect an error response
1. User sends a request that returns a non-2xx status (e.g. `404`).
2. The Response view renders the status badge in an error-emphasis style with latency, response headers, and the body (pretty-printed if JSON, raw otherwise). On phone this is the activated Response tab; on tablet, the response pane.
3. User taps the `Raw` toggle to view the unformatted body.
4. The request is still recorded in History with its `404` status.

### Flow: Inspect a failed (transport-level) attempt
1. User sends a request that never receives an HTTP response (timeout, DNS, TLS).
2. The Response view shows an error card with the failure reason (e.g. `Timeout after 30 s`, `No internet`) and a `Retry` action; no status/latency badge is shown.
3. The attempt is recorded in History with a distinct `FAILED` chip and the failure reason as a subtitle.

### Flow: Adjust settings
1. User taps the toolbar overflow menu → `Settings` → Settings screen opens.
2. User adjusts request timeout (default 30 s), toggles follow-redirects (default on), or invokes a destructive action (`Clear history`, `Clear all data`), each of which prompts a confirmation dialog.

## Screen inventory
| Screen | Purpose | Reached from |
| --- | --- | --- |
| RequestBuilder | Compose a request (method, URL, headers, params, body, auth) and view its response; on phone hosts Request/Response top-level tabs | Launcher (default tab) / History replay / Collection item / Save flow |
| Response | Status, latency, response headers, and body for the last send | Phone: the `Response` top-level tab of RequestBuilder. Tablet: the second pane of RequestBuilder |
| History | Chronological, offline-browsable list of all sent requests and failed attempts | Bottom nav `History` tab |
| HistoryDetail | Full request + captured response (or error card) for one history entry; replay/duplicate/export | Tapping a History row |
| Collections | List of named collections | Bottom nav `Collections` tab |
| CollectionDetail | Saved requests within one collection; reorder/rename/delete/duplicate | Tapping a collection |
| SaveToCollectionSheet | Pick/create a collection and name the request | `Save` action in RequestBuilder or HistoryDetail |
| Environments | List of environments and active-environment management | Toolbar switcher → `Manage environments` |
| EnvironmentDetail | CRUD key-value variables for one environment (values encrypted at rest) | Tapping an environment row |
| EnvironmentSwitcher | Quick-pick the active environment | Toolbar switcher control (popup/sheet) |
| CurlExportSheet | Preview, copy, and share the generated cURL command; toggle credential redaction | `cURL` action in RequestBuilder or HistoryDetail |
| Settings | App-level settings (timeout, follow-redirects, clear history, reset app) | Toolbar overflow menu → `Settings` |
| Docs | HTTP reference index (Methods/Status codes/Headers/Body formats) | Bottom nav `Docs` tab / contextual `?` links |
| DocsArticle | A single reference section's content | Tapping a Docs index item or a `?` deep link |

## Per-screen states

### RequestBuilder
- **Loading:** When opened via Replay or from a collection, a brief skeleton on the URL bar and tabs while the saved request is hydrated from Room. On a fresh launch, no loading — empty defaults render immediately.
- **Empty:** Default new request — GET method, placeholder URL bar (`Enter request URL`), empty Headers/Params/Body tabs each with an `Add` CTA, Auth tab set to `No auth`. On phone, the `Response` top-level tab is grayed out/disabled with a hint (`Send a request to see the response`). `Send` is disabled until a non-empty URL is present.
- **Error:** Send failures surface inline above/within the Request view: invalid/empty URL (`Enter a valid URL`), unresolved variable (`{{baseId}} has no value in the active environment`), and no connectivity (`You're offline — sending requires a connection`). The request is not added to History when it never left the device due to a validation/offline error.
- **Content:** Method selector, URL bar with highlighted `{{variable}}` tokens, tab row (Headers, Params, Body, Auth) with per-tab editors, `Send`, and overflow actions (`Save`, `cURL`).
  - *Auth tab:* three mutually exclusive states — `No auth`; `Basic` (username + password fields); `Bearer` (single token field). Credentials and tokens are stored encrypted at rest via the Android Keystore; a small note indicates this on the Auth tab.
  - *Headers tab:* typing in a header **key** field shows an inline autocomplete dropdown of well-known header names (`Content-Type`, `Authorization`, `Accept`, `Cache-Control`, `X-Request-ID`, etc.). When a selected key has a constrained value set (e.g. `Content-Type`, `Accept`, `Cache-Control`), the **value** field shows an inline autocomplete dropdown of common values (e.g. `application/json`, `text/plain`, `no-cache`). Autocomplete is overlay/inline and never navigates away; it can be dismissed or ignored to type a free-form value.
  - *Phone top-level tabs:* `Request` (the above) and `Response`. The `Response` tab is disabled until the first send this session; after a send it activates and the app switches to it. *Tablet:* no Request/Response tabs — the Response pane sits side-by-side.

### Response
- **Loading:** Spinner/progress indicator with `Sending…` while the request is in flight; cancellable via a `Cancel` affordance.
- **Empty:** Before any send in the current session — on phone the tab is disabled with `Send a request to see the response here.`; on tablet the pane shows the same placeholder.
- **Error:** For transport-level failures (timeout, DNS, TLS), an error card with the failure reason (e.g. `Timeout after 30 s`, `No internet`) and a `Retry` action; no status/latency badge is shown because no HTTP response was received.
- **Content:** Status code badge (color-coded by class), latency badge, response-headers section (collapsible), and body with JSON syntax highlighting plus a `Raw` toggle and a `Copy body` action. Bodies are rendered up to 500 KB; above that the view shows `Body too large — showing first 500 KB` with a `Copy full body` action. The full body is always persisted to History uncapped.

### History
- **Loading:** Skeleton list rows while reading from Room (typically instant).
- **Empty:** Illustration/text `No requests yet — send one from the builder to start your history.`
- **Error:** If the local store fails to read, an error row with `Couldn't load history` and a `Retry` action.
- **Content:** Reverse-chronological rows (method chip, URL, status badge, relative timestamp). A subtle persistent `offline` indicator shows when there's no connectivity (history remains fully browsable). History is unlimited — entries are kept until the user clears them via Settings → Clear history. Rows are tappable; long-press offers `Replay`, `Duplicate`, `Add to collection`, `Delete`.
- **Failed attempt:** A row for a request that failed at the transport level (timeout, DNS, TLS — no HTTP response received) shows a `FAILED` chip (distinct color from status-code chips) in place of the status badge, plus the failure reason as a subtitle (e.g. `Timeout after 30 s`, `No internet`).

### HistoryDetail
- **Loading:** Brief skeleton while the entry and its captured response are hydrated.
- **Empty:** N/A — a detail is only reachable from an existing entry.
- **Error:** If the entry can't be loaded (e.g. deleted concurrently), `This request is no longer available` with a back action.
- **Content:** Full request summary (method, resolved URL, headers, params, body, auth) and the captured response (status, latency, headers, body). Actions: `Replay`, `Duplicate` (opens the request in RequestBuilder as a new unsaved draft), `cURL`, `Add to collection`, `Delete`.
- **Failed attempt:** For an entry that failed at the transport level, the response section is replaced by an error card showing the failure reason; `Replay` and `Duplicate` remain available, `cURL` reflects the original request.

### Collections
- **Loading:** Skeleton list while reading collections from Room.
- **Empty:** `No collections yet — group related requests into a collection.` with a `New collection` CTA.
- **Error:** `Couldn't load collections` with `Retry`.
- **Content:** List of collection rows (name + request count). A `New collection` action (FAB or toolbar). Long-press a row for `Rename` / `Delete`.

### CollectionDetail
- **Loading:** Skeleton while the collection's requests load.
- **Empty:** `This collection is empty — add a request from the builder or history.`
- **Error:** `Couldn't load this collection` with `Retry`.
- **Content:** Ordered list of saved requests (method chip, name/URL). Tapping a request opens it in RequestBuilder. Drag handles allow reordering; per-item overflow offers `Duplicate` (opens the request in RequestBuilder as a new unsaved draft, original untouched) and `Remove`; collection-level overflow offers `Rename collection` and `Delete collection`.

### SaveToCollectionSheet
- **Loading:** Brief skeleton on the collection list while it loads.
- **Empty:** When no collections exist, the sheet shows only the `New collection` field/CTA plus the request-name field.
- **Error:** If saving fails, an inline `Couldn't save — try again` message; the sheet stays open.
- **Content:** Selectable list of existing collections, a `New collection` option, a request-name field (prefilled with method + path), and `Save` / `Cancel`.

### Environments
- **Loading:** Skeleton list while environments load.
- **Empty:** `No environments yet — create one to use {{variables}} in your requests.` with `New environment` CTA.
- **Error:** `Couldn't load environments` with `Retry`.
- **Content:** List of environment rows with the active one marked. A `New environment` action; long-press for `Rename` / `Delete`; tapping a row opens EnvironmentDetail. Environments are fully independent — there is no inheritance or shared global/base variable set.

### EnvironmentDetail
- **Loading:** Skeleton on the variable table while it loads.
- **Empty:** `No variables yet — add a key and value.` with an `Add variable` CTA.
- **Error:** If a save fails, inline `Couldn't save changes` with retry; duplicate-key entries show inline validation `Key already exists`.
- **Content:** Editable key-value table, `Add variable`, per-row delete, and `Save`. The environment name is editable at the top. Variable values are stored encrypted at rest via the Android Keystore; a small note indicates this.

### EnvironmentSwitcher
- **Loading:** N/A — operates on already-loaded environment list; shows instantly.
- **Empty:** Lists only `No environment` and `Manage environments` when none are defined.
- **Error:** N/A — read-only quick-pick over in-memory state; failures surface in Environments.
- **Content:** Selectable list with the active environment checked, a `No environment` option, and a `Manage environments` entry.

### CurlExportSheet
- **Loading:** Brief generation step while the command is assembled and variables resolved (usually instant).
- **Empty:** N/A — only opened for a concrete request that always yields a command.
- **Error:** If a variable can't be resolved, a warning banner (`Some variables are unresolved and left literal`) above the command; the command still displays.
- **Content:** Read-only, scrollable cURL command, an `Include credentials` toggle (off by default — Basic emits `-u [username]:[redacted]`, Bearer emits `--header 'Authorization: Bearer [redacted]'`; when on, real values are emitted), plus `Copy` (to clipboard) and `Share` (Android share sheet) actions and a `Done`/close affordance.

### Settings
- **Loading:** N/A — settings are read instantly from local preferences.
- **Empty:** N/A — settings always have default values.
- **Error:** A failed clear/reset action surfaces an inline `Couldn't complete — try again`; otherwise no error state.
- **Content:**
  - **Request timeout:** numeric input / slider, default 30 s, user-configurable. Applies globally to all sends.
  - **Follow redirects:** toggle, default on. Global; there is no per-request override.
  - **Clear history:** destructive action; opens a confirmation dialog (`Clear all history? This can't be undone.`) before removing all History entries.
  - **Clear all data / reset app:** destructive action; opens a confirmation dialog before wiping history, collections, environments, auth credentials/tokens, and settings back to defaults.

### Docs
- **Loading:** N/A — content is bundled in-app and renders immediately.
- **Empty:** N/A — the reference index is always populated.
- **Error:** N/A — static bundled content; no fetch.
- **Content:** Index with four sections (Methods, Status codes, Common headers, Body formats) and an optional search field. Tapping a section opens DocsArticle.

### DocsArticle
- **Loading:** N/A — bundled content renders immediately.
- **Empty:** N/A — every article has content.
- **Error:** N/A — static content.
- **Content:** Scrollable reference content for the chosen section with a back affordance; supports being deep-linked from contextual `?` icons in the builder.

## Navigation map
Bottom navigation has four primary destinations; each maintains its own back stack. Settings is reached from the toolbar overflow menu, not the bottom nav.

- Bottom nav
  - Builder (default / start destination)
    - On phone: hosts two top-level tabs — `Request` and `Response`. Switching between them is in-screen, not a navigation push. The `Response` tab is disabled until the first send this session; after a send the app auto-switches to it.
    - On tablet: Request editor + Response pane are side-by-side on one screen (no Request/Response tabs).
    - → SaveToCollectionSheet (modal, dismiss returns to Builder)
    - → CurlExportSheet (modal, dismiss returns to Builder)
    - → DocsArticle (via contextual `?`; back returns to Builder, not the Docs tab)
  - History
    - → HistoryDetail
      - → Builder (via Replay; replay switches to the Builder tab with the request loaded)
      - → Builder (via Duplicate; opens a new unsaved draft, original untouched)
      - → SaveToCollectionSheet (modal)
      - → CurlExportSheet (modal)
  - Collections
    - → CollectionDetail
      - → Builder (tapping a saved request loads it into the Builder tab)
      - → Builder (per-item Duplicate; opens a new unsaved draft)
  - Docs
    - → DocsArticle

- Toolbar (present on Builder/History/Collections)
  - EnvironmentSwitcher (popup/sheet)
    - → Environments → EnvironmentDetail
  - Overflow menu
    - → Settings

Back-stack notes:
- Replay, Duplicate, and "open saved request" all target the Builder tab. Replay/open replaces the Builder's prior draft; Duplicate also replaces it with a fresh unsaved copy. If the existing draft was unsaved and dirty, prompt `Discard current request?` before replacing.
- On phone, the `Request`/`Response` toggle is intra-screen state — system Back from the Response tab does not pop to a previous screen; it behaves like the Builder destination's normal Back. (After a send the user can freely toggle back to Request to edit and re-send.)
- Modal sheets (Save, cURL, Switcher) never appear in the back stack; system Back dismisses the sheet only.
- Settings is pushed onto the current tab's back stack; system Back returns to the originating screen.
- Tablet two-pane: Builder + Response share one screen; History/Collections may show list + detail side-by-side, where selecting a list item updates the detail pane in place rather than pushing a new screen.

## Edge cases
1. App backgrounded mid-send: the in-flight request continues or is cancelled cleanly; on return the Response view reflects the final state and History stays consistent (no duplicate entries).
2. Connectivity lost between composing and tapping Send: surface the offline error in the Builder; do not record a History entry for an unsent request.
3. Connectivity lost while browsing: History, HistoryDetail, Collections, Environments, Settings, and Docs all remain usable; the subtle `offline` indicator appears where network-backed actions are unavailable.
4. Unresolved `{{variable}}` at send time (no active environment or missing key): block send with a clear inline error naming the variable; in cURL export, leave it literal and warn.
5. Active environment deleted while referenced by the current draft: switcher falls back to `No environment`; variables become unresolved and follow rule 4.
6. Header autocomplete dismissal: tapping outside the dropdown, pressing back, or continuing to type a non-matching value dismisses the autocomplete and accepts the free-form text; autocomplete never blocks free-form key/value entry.
7. Binary file upload where the chosen file is moved/deleted before send: show `Selected file is no longer available — choose another`.
8. Very large response body: render up to 500 KB; above that show `Body too large — showing first 500 KB` with a `Copy full body` action. The full body is always saved to History uncapped. Pretty-print is skipped above the render threshold with a note.
9. Malformed JSON response while body-type is JSON: fall back to raw view with a subtle `Not valid JSON — showing raw` note.
10. Request timeout (exceeds the configured Settings value, default 30 s) / DNS / TLS failure: Response error state with `Retry`; recorded in History as a failed attempt with a `FAILED` chip and failure reason (no HTTP status).
11. Duplicate variable keys within an environment: inline validation prevents save until resolved.
12. Deleting a collection that contains requests: confirm destructive action; saved requests in it are removed (history copies are unaffected).
13. Storage/permission failure when picking a file for upload (Storage Access Framework denied): show `Permission needed to attach a file` with a retry path.
14. Replaying or duplicating a request whose body referenced a binary file that no longer exists: persisted metadata (filename, size, MIME type, URI) is loaded, and the missing attachment is flagged with `Attached file is no longer available — choose another`.
15. Process death while a modal sheet is open: on relaunch, return to the underlying tab without a stale sheet; unsaved sheet input is discarded.
16. Rotation / window-size change between phone and tablet layouts: the active request, response, send-state, and the selected Request/Response tab (phone) are preserved across the layout switch; on switching to tablet the Response pane shows the same response that the Response tab held.
17. Clear history / reset app: each requires confirmation; after Clear history the History screen returns to its empty state; after reset, the app returns to a fresh first-launch state.

## Offline behavior
- View/browse History (including failed attempts): works offline (read from Room).
- Inspect a HistoryDetail (request + captured response, or error card): works offline.
- Replay or Duplicate a request: composing/loading works offline; the actual Send requires connectivity (blocked with the offline error otherwise).
- Create/rename/delete/reorder Collections and add saved requests: works offline (local writes).
- Create/edit Environments and variables, switch active environment: works offline.
- Adjust Settings (timeout, follow-redirects, clear history, reset): works offline (local state).
- Generate, copy, and share cURL: works offline (no network needed; uses the current request + active environment).
- Header autocomplete: works offline (header names/values are bundled in-app).
- Browse Docs (Methods, Status codes, Headers, Body formats): works offline (bundled content).
- Send any request (GET/POST/etc.): requires connectivity — blocked offline with `You're offline — sending requires a connection.`
- The word `offline` appears as a subtle, persistent indicator in History and on any action gated by connectivity (e.g. the Send button area), never as an intrusive blocking dialog except at the moment of an attempted send.

## Open questions
None — all prior open questions have been resolved and incorporated into the spec body (timeout and follow-redirects in Settings, body truncation in Response, unlimited history with manual clear, encrypted-at-rest credentials/tokens/variables, redacted cURL with `Include credentials` toggle, global follow-redirects, Duplicate action, independent environments, binary metadata persistence, and the Settings screen).
