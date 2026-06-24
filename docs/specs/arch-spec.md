# Architecture Spec — Developer API Explorer

A single-module, package-by-feature Android app. All data is local (Room + Keystore-encrypted sensitive fields + DataStore for settings). There is exactly one network boundary — the HTTP engine that *sends user-composed requests*. Everything else (history, collections, environments, docs, settings, cURL export) is offline-first by construction because it never touches the network. "Offline-first" here means: reads always come from the local store; the only operation gated on connectivity is `SendRequestUseCase`.

## Module / package layout

Single module (`:app`) is justified: solo-use app, no shared library reuse target, no dynamic feature delivery, modest screen count. Package-by-feature with a `core` package for cross-feature primitives.

```
com.requestlab
├── RequestLabApplication.kt              (@HiltAndroidApp)
├── MainActivity.kt                        (single activity, Compose host, adaptive nav)
│
├── core
│   ├── common
│   │   ├── AppResult.kt                    (sealed result type)
│   │   ├── AppError.kt                     (sealed error type)
│   │   ├── DispatcherModule.kt             (@IoDispatcher / @DefaultDispatcher qualifiers + providers)
│   │   └── ConnectivityObserver.kt         (interface) + ConnectivityObserverImpl
│   ├── crypto
│   │   ├── SecretCipher.kt                 (interface: encrypt/decrypt String <-> ciphertext blob)
│   │   ├── KeystoreSecretCipher.kt         (AES/GCM key in AndroidKeyStore)
│   │   └── CryptoModule.kt
│   ├── database
│   │   ├── RequestLabDatabase.kt           (@Database, all entities + DAOs)
│   │   ├── DatabaseModule.kt               (provides DB + each DAO)
│   │   ├── Converters.kt                   (TypeConverters: header/param lists, enums, instants)
│   │   └── migrations/                     (Migration_1_2, ... if/when schema changes)
│   ├── datastore
│   │   ├── SettingsDataStore.kt            (Proto/Preferences DataStore wrapper)
│   │   └── DataStoreModule.kt
│   ├── network
│   │   ├── HttpEngine.kt                   (interface: execute(PreparedRequest) -> RawHttpResult)
│   │   ├── OkHttpHttpEngine.kt             (OkHttp impl; timeout + redirects from settings)
│   │   ├── HttpClientProvider.kt           (builds OkHttpClient per-call from RequestConfig)
│   │   └── NetworkModule.kt
│   ├── designsystem                        (theme, color, typography, reusable composables)
│   └── navigation
│       ├── RequestLabNavHost.kt
│       ├── Destinations.kt
│       └── AdaptiveScaffold.kt             (phone tabs vs tablet two-pane via WindowSizeClass)
│
└── feature
    ├── builder                             (RequestBuilder + Response — share a draft, one feature)
    │   ├── domain  { model/, usecase/, RequestDraftRepository.kt, SendRepository.kt }
    │   ├── data    { entity/, dao/, mapper/, RequestDraftRepositoryImpl.kt, SendRepositoryImpl.kt, BuilderDataModule.kt }
    │   └── ui      { builder/, response/, curlexport/, savesheet/ }
    ├── history
    │   ├── domain  { model/, usecase/, HistoryRepository.kt }
    │   ├── data    { entity/, dao/, mapper/, HistoryRepositoryImpl.kt, HistoryDataModule.kt }
    │   └── ui      { list/, detail/ }
    ├── collections
    │   ├── domain  { model/, usecase/, CollectionsRepository.kt }
    │   ├── data    { entity/, dao/, mapper/, CollectionsRepositoryImpl.kt, CollectionsDataModule.kt }
    │   └── ui      { list/, detail/ }
    ├── environments
    │   ├── domain  { model/, usecase/, EnvironmentsRepository.kt, VariableResolver.kt }
    │   ├── data    { entity/, dao/, mapper/, EnvironmentsRepositoryImpl.kt, EnvironmentsDataModule.kt }
    │   └── ui      { list/, detail/, switcher/ }
    ├── settings
    │   ├── domain  { model/, usecase/, SettingsRepository.kt }
    │   ├── data    { SettingsRepositoryImpl.kt, AppResetCoordinator.kt, SettingsDataModule.kt }
    │   └── ui      { SettingsScreen.kt, SettingsViewModel.kt }
    └── docs                                (static bundled content; no data layer)
        ├── domain  { model/, usecase/, DocsRepository.kt }
        ├── data    { DocsRepositoryImpl.kt (reads bundled assets/JSON), DocsDataModule.kt }
        └── ui      { index/, article/ }
```

Cross-feature note: `RequestDraft` (the in-flight composed request) is the shared currency between builder, history (replay/duplicate), collections (open saved), environments (variable resolution), and cURL export. It lives in `feature/builder/domain/model`. Replay/Duplicate/Open hand a `RequestDraft` to the builder via a saved-state handle / navigation argument carrying a draft id resolved through `RequestDraftRepository`.

---

## Features

### Feature: builder (RequestBuilder + Response + cURL export + Save sheet)

#### Domain

**Models** (immutable):
- `data class RequestDraft(val id: String, val method: HttpMethod, val url: String, val headers: List<KeyValue>, val params: List<KeyValue>, val body: RequestBody, val auth: AuthConfig, val sourceSavedRequestId: String?, val isDirty: Boolean)` — the composed, editable request held by the builder; `id` is an ephemeral draft id.
- `enum class HttpMethod { GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS }`
- `data class KeyValue(val key: String, val value: String, val enabled: Boolean = true)` — a header or query param row.
- `sealed interface RequestBody` with `data object None`, `data class Json(val text: String)`, `data class RawText(val text: String, val contentType: String)`, `data class FormUrlEncoded(val fields: List<KeyValue>)`, `data class Multipart(val parts: List<MultipartPart>)`, `data class Binary(val file: FileRef)` — body variants. Bytes are never stored; binary holds a `FileRef` URI reference only.
- `sealed interface MultipartPart` with `data class Text(val name: String, val value: String)` and `data class FilePart(val name: String, val file: FileRef)`.
- `data class FileRef(val uri: String, val displayName: String, val sizeBytes: Long?, val mimeType: String?)` — SAF URI + persisted metadata; resolvability checked at send time.
- `sealed interface AuthConfig` with `data object None`, `data class Basic(val username: String, val password: String)`, `data class Bearer(val token: String)` — secret fields (`password`, `token`) are encrypted at rest in the data layer.
- `data class PreparedRequest(val method: HttpMethod, val resolvedUrl: String, val headers: List<KeyValue>, val params: List<KeyValue>, val body: RequestBody, val auth: AuthConfig, val config: RequestConfig)` — a draft after `{{variable}}` substitution + auth materialization, ready for the engine.
- `data class RequestConfig(val timeoutSeconds: Int, val followRedirects: Boolean)` — pulled from settings at send time.
- `data class HttpResponse(val statusCode: Int, val statusMessage: String, val headers: List<KeyValue>, val body: String, val bodySizeBytes: Long, val latencyMs: Long, val isJson: Boolean)` — successful HTTP exchange (any status code, including 4xx/5xx).
- `data class SendOutcome(val request: PreparedRequest, val response: HttpResponse?, val failure: TransportFailure?, val sentAt: Instant)` — unified result; exactly one of `response`/`failure` is non-null. Persisted to history.
- `data class TransportFailure(val kind: FailureKind, val message: String)` ; `enum class FailureKind { TIMEOUT, NO_INTERNET, DNS, TLS, CANCELLED, UNKNOWN }` — no HTTP response received.
- `data class CurlCommand(val text: String, val unresolvedVariables: List<String>)` — generated cURL output.

**Use cases:**
- `LoadDraftUseCase` — `suspend (draftSource: DraftSource) -> AppResult<RequestDraft>` — hydrates a fresh, replayed, duplicated, or saved-request draft. `DraftSource` is `New | FromHistory(id) | FromSavedRequest(id) | Duplicate(historyOrSavedId)`.
- `UpdateDraftUseCase` — `suspend (draft: RequestDraft) -> AppResult<Unit>` — persists the working draft (marks dirty) so it survives process death.
- `ResolveDraftUseCase` — `suspend (draft: RequestDraft, activeEnvId: String?) -> AppResult<PreparedRequest>` — substitutes `{{vars}}`, materializes auth, validates URL; returns `Failure(AppError.Validation)` on unresolved variable / empty-or-invalid URL.
- `SendRequestUseCase` — `suspend (prepared: PreparedRequest) -> AppResult<SendOutcome>` — the only network use case. Checks connectivity, executes via engine, captures latency, writes the outcome to history, returns it. Cancellable.
- `GenerateCurlUseCase` — `suspend (draft: RequestDraft, activeEnvId: String?, includeCredentials: Boolean) -> AppResult<CurlCommand>` — builds cURL, redacting credentials unless `includeCredentials`, leaving unresolved vars literal and listing them. Offline-safe.
- `SaveRequestToCollectionUseCase` — `suspend (draft: RequestDraft, collectionId: String?, newCollectionName: String?, requestName: String) -> AppResult<String>` — persists the draft as a SavedRequest (creating a collection if `newCollectionName` given); returns saved request id. Delegates to CollectionsRepository.
- `GetHeaderSuggestionsUseCase` — `(keyPrefix: String) -> List<String>` — well-known header names matching prefix (bundled, sync, offline).
- `GetHeaderValueSuggestionsUseCase` — `(headerKey: String) -> List<String>` — common values for a constrained header (bundled, sync, offline).
- `ObserveConnectivityUseCase` — `() -> Flow<Boolean>` — drives the offline indicator and Send enablement.

**Repository interfaces:**
```kotlin
interface RequestDraftRepository {
    suspend fun loadDraft(source: DraftSource): AppResult<RequestDraft>
    suspend fun saveWorkingDraft(draft: RequestDraft): AppResult<Unit>
    fun observeWorkingDraft(): Flow<RequestDraft?>
}

interface SendRepository {
    // resolves config from settings, runs the engine, records history, returns outcome
    suspend fun send(prepared: PreparedRequest): AppResult<SendOutcome>
}
```

#### Data

**Entities (Room):**
```kotlin
@Entity(tableName = "working_draft")  // single-row table; survives process death
data class WorkingDraftEntity(
    @PrimaryKey val id: String,
    val method: String,
    val url: String,
    val headersJson: String,
    val paramsJson: String,
    val bodyJson: String,           // serialized RequestBody (URIs only for files, never bytes)
    val authType: String,           // NONE | BASIC | BEARER
    val authUsername: String?,      // plaintext (username is not secret)
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val authSecretCipher: ByteArray?, // encrypted password/token
    val sourceSavedRequestId: String?,
    val isDirty: Boolean
)
```
The builder reuses `CollectionsRepository`/`HistoryRepository` for persistence of saved requests and outcomes; it owns only the single-row `working_draft` (its own DAO). No network entities — `HttpResponse`/`SendOutcome` are produced live and persisted via the history feature.

**DAOs:**
```kotlin
@Dao
interface WorkingDraftDao {
    @Query("SELECT * FROM working_draft WHERE id = 'current'") fun observe(): Flow<WorkingDraftEntity?>
    @Query("SELECT * FROM working_draft WHERE id = 'current'") suspend fun get(): WorkingDraftEntity?
    @Upsert suspend fun upsert(draft: WorkingDraftEntity)
    @Query("DELETE FROM working_draft") suspend fun clear()
}
```

**Conflict strategy:** `@Upsert` (REPLACE) — single-row, last write wins.
**Refresh policy:** N/A for network (send is the only network op and is user-triggered, recorded once). The working draft is written on every edit (debounced in the ViewModel) and on send.
**Mappers:** `WorkingDraftEntity <-> RequestDraft` in `data/mapper/DraftMapper.kt`, using `SecretCipher` to encrypt/decrypt `authSecretCipher`. `RequestDraft -> PreparedRequest` is a domain operation in `ResolveDraftUseCase`, not a mapper.

#### UI

**Screens & ViewModels:**
- `RequestBuilderScreen` + `ResponsePane`/`ResponseTab` → `BuilderViewModel(loadDraft, updateDraft, resolveDraft, sendRequest, generateCurl, saveToCollection, getHeaderSuggestions, getHeaderValueSuggestions, observeConnectivity, getActiveEnvironment)`
  - `BuilderUiState`:
    ```kotlin
    data class BuilderUiState(
        val draftStatus: DraftStatus,              // Loading | Ready | HydrateError
        val draft: RequestDraftUi,
        val activeTab: BuilderTab,                  // HEADERS | PARAMS | BODY | AUTH
        val phonePane: BuilderPane,                 // REQUEST | RESPONSE  (phone only)
        val responseAvailable: Boolean,             // gates the phone Response tab this session
        val sendState: SendState,
        val response: ResponseUiState,
        val inlineError: String?,                   // validation/offline error shown in builder
        val isOffline: Boolean,
        val sendEnabled: Boolean
    )
    sealed interface SendState { data object Idle; data object Sending; data object Done : SendState }
    sealed interface ResponseUiState {
        data object Empty : ResponseUiState
        data object Loading : ResponseUiState
        data class Content(val status: StatusUi, val latencyMs: Long, val headers: List<KeyValue>,
                           val body: BodyUi, val showRaw: Boolean) : ResponseUiState
        data class Failure(val reason: String, val kind: FailureKind) : ResponseUiState
    }
    sealed interface BodyUi { data class Pretty(...); data class Raw(...); data class Truncated(val shown: String, val totalBytes: Long) : BodyUi }
    ```
  - **Events:** `OnMethodSelected(HttpMethod)`, `OnUrlChanged(String)`, `OnTabSelected(BuilderTab)`, `OnHeaderEdited(index, KeyValue)`, `OnAddHeader`, `OnHeaderKeyTyping(index, prefix)`, `OnParamEdited(...)`, `OnBodyTypeChanged(...)`, `OnBodyTextChanged(...)`, `OnPickFile`, `OnFilePicked(FileRef)`, `OnAuthTypeChanged(...)`, `OnAuthFieldChanged(...)`, `OnSendClicked`, `OnCancelSend`, `OnRetrySend`, `OnPhonePaneChanged(BuilderPane)`, `OnToggleRawBody`, `OnCopyBody`, `OnRequestSave`, `OnRequestCurl`, `OnDismissInlineError`.
- `SaveToCollectionSheet` → `SaveToCollectionViewModel(getCollections, saveToCollection)`
  - `SaveSheetUiState`: `Loading | Empty(requestName) | Content(collections, selectedId, newName, requestName, saving, error)`.
  - **Events:** `OnSelectCollection(id)`, `OnNewCollectionNameChanged`, `OnRequestNameChanged`, `OnSaveConfirmed`, `OnDismiss`.
- `CurlExportSheet` → `CurlExportViewModel(generateCurl, getActiveEnvironment)`
  - `CurlUiState`: `Generating | Content(command, includeCredentials, unresolvedWarning)`.
  - **Events:** `OnToggleIncludeCredentials`, `OnCopy`, `OnShare`, `OnDismiss`.

---

### Feature: history

#### Domain

**Models:**
- `data class HistoryEntry(val id: String, val method: HttpMethod, val resolvedUrl: String, val status: HistoryStatus, val latencyMs: Long?, val sentAt: Instant, val environmentName: String?)` — list-row summary.
- `sealed interface HistoryStatus` with `data class Http(val code: Int)` and `data class Failed(val kind: FailureKind, val reason: String)`.
- `data class HistoryDetail(val entry: HistoryEntry, val request: PreparedRequest, val response: HttpResponse?, val failure: TransportFailure?)` — full record for the detail screen.

**Use cases:**
- `ObserveHistoryUseCase` — `() -> Flow<AppResult<List<HistoryEntry>>>` — reverse-chronological, offline (Room).
- `GetHistoryDetailUseCase` — `suspend (id: String) -> AppResult<HistoryDetail>` — full request+response; `Failure(NotFound)` if deleted concurrently.
- `RecordSendOutcomeUseCase` — `suspend (outcome: SendOutcome, environmentName: String?) -> AppResult<String>` — writes a send outcome (success or transport failure) to history; called by `SendRepositoryImpl`.
- `DeleteHistoryEntryUseCase` — `suspend (id: String) -> AppResult<Unit>`.
- `ClearHistoryUseCase` — `suspend () -> AppResult<Unit>` — used directly and by Settings → Clear history.

**Repository interface:**
```kotlin
interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryEntry>>
    suspend fun getDetail(id: String): AppResult<HistoryDetail>
    suspend fun record(outcome: SendOutcome, environmentName: String?): AppResult<String>
    suspend fun delete(id: String): AppResult<Unit>
    suspend fun clearAll(): AppResult<Unit>
}
```

#### Data

**Entities (Room):**
```kotlin
@Entity(tableName = "history", indices = [Index("sent_at"), Index("status_code")])
data class HistoryEntity(
    @PrimaryKey val id: String,
    val method: String,
    val resolvedUrl: String,
    @ColumnInfo(name = "status_code") val statusCode: Int?,       // null => transport failure
    val statusMessage: String?,
    @ColumnInfo(name = "failure_kind") val failureKind: String?,  // non-null => FAILED row
    val failureReason: String?,
    val latencyMs: Long?,
    val requestHeadersJson: String,
    val requestParamsJson: String,
    val requestBodyJson: String,
    val authType: String,
    val authUsername: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val authSecretCipher: ByteArray?, // encrypted
    val responseHeadersJson: String?,
    @ColumnInfo(name = "response_body") val responseBody: String?,  // full, uncapped
    val responseBodySizeBytes: Long?,
    val isJsonResponse: Boolean,
    @ColumnInfo(name = "sent_at") val sentAt: Long,                 // epoch millis
    val environmentName: String?
)
```

**DAOs:**
```kotlin
@Dao
interface HistoryDao {
    @Query("SELECT id, method, resolvedUrl, status_code, statusMessage, failure_kind, failureReason, latencyMs, sent_at, environmentName FROM history ORDER BY sent_at DESC")
    fun observeSummaries(): Flow<List<HistorySummaryView>>
    @Query("SELECT * FROM history WHERE id = :id") suspend fun getById(id: String): HistoryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entry: HistoryEntity)
    @Query("DELETE FROM history WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM history") suspend fun clearAll()
}
```

**Conflict strategy:** `REPLACE` (each send produces a unique id; replace is defensive).
**Refresh policy:** N/A — history is append-only local writes from sends; reads are pure Room observation.
**Mappers:** `HistoryEntity <-> HistoryEntry`/`HistoryDetail` and `SendOutcome -> HistoryEntity` in `data/mapper/HistoryMapper.kt`; decrypts `authSecretCipher` via `SecretCipher` only when building a `HistoryDetail`.

#### UI

**Screens & ViewModels:**
- `HistoryListScreen` → `HistoryListViewModel(observeHistory, deleteHistoryEntry, observeConnectivity)`
  - `HistoryListUiState`: `Loading | Empty | Content(rows, isOffline) | Error(message)`.
  - **Events:** `OnRowClicked(id)`, `OnReplay(id)`, `OnDuplicate(id)`, `OnAddToCollection(id)`, `OnDelete(id)`, `OnRetryLoad`.
- `HistoryDetailScreen` → `HistoryDetailViewModel(getHistoryDetail)`
  - `HistoryDetailUiState`: `Loading | Content(detail) | NotFound | Error(message)`.
  - **Events:** `OnReplay`, `OnDuplicate`, `OnCurl`, `OnAddToCollection`, `OnDelete`, `OnBack`.

---

### Feature: collections

#### Domain

**Models:**
- `data class Collection(val id: String, val name: String, val requestCount: Int, val position: Int)`.
- `data class SavedRequest(val id: String, val collectionId: String, val name: String, val method: HttpMethod, val url: String, val position: Int)` — list-row summary.
- `data class SavedRequestDetail(val savedRequest: SavedRequest, val headers: List<KeyValue>, val params: List<KeyValue>, val body: RequestBody, val auth: AuthConfig)` — full payload to rehydrate a draft.

**Use cases:**
- `ObserveCollectionsUseCase` — `() -> Flow<AppResult<List<Collection>>>`.
- `ObserveCollectionRequestsUseCase` — `(collectionId: String) -> Flow<AppResult<List<SavedRequest>>>` — ordered by position.
- `CreateCollectionUseCase` — `suspend (name: String) -> AppResult<String>`.
- `RenameCollectionUseCase` — `suspend (id: String, name: String) -> AppResult<Unit>`.
- `DeleteCollectionUseCase` — `suspend (id: String) -> AppResult<Unit>` — cascades to its saved requests.
- `SaveRequestUseCase` — `suspend (collectionId: String, name: String, draft: RequestDraft) -> AppResult<String>` — persists a draft as a SavedRequest (encrypting secrets).
- `GetSavedRequestDetailUseCase` — `suspend (id: String) -> AppResult<SavedRequestDetail>` — to open in the builder.
- `ReorderRequestsUseCase` — `suspend (collectionId: String, orderedIds: List<String>) -> AppResult<Unit>`.
- `RemoveSavedRequestUseCase` — `suspend (id: String) -> AppResult<Unit>`.

**Repository interface:**
```kotlin
interface CollectionsRepository {
    fun observeCollections(): Flow<List<Collection>>
    fun observeRequests(collectionId: String): Flow<List<SavedRequest>>
    suspend fun createCollection(name: String): AppResult<String>
    suspend fun renameCollection(id: String, name: String): AppResult<Unit>
    suspend fun deleteCollection(id: String): AppResult<Unit>
    suspend fun saveRequest(collectionId: String, name: String, draft: RequestDraft): AppResult<String>
    suspend fun getRequestDetail(id: String): AppResult<SavedRequestDetail>
    suspend fun reorder(collectionId: String, orderedIds: List<String>): AppResult<Unit>
    suspend fun removeRequest(id: String): AppResult<Unit>
}
```

#### Data

**Entities (Room):**
```kotlin
@Entity(tableName = "collection", indices = [Index("position")])
data class CollectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val position: Int,
    val createdAt: Long
)

@Entity(
    tableName = "saved_request",
    foreignKeys = [ForeignKey(entity = CollectionEntity::class, parentColumns = ["id"],
        childColumns = ["collectionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("collectionId"), Index(value = ["collectionId", "position"])]
)
data class SavedRequestEntity(
    @PrimaryKey val id: String,
    val collectionId: String,
    val name: String,
    val method: String,
    val url: String,
    val headersJson: String,
    val paramsJson: String,
    val bodyJson: String,
    val authType: String,
    val authUsername: String?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val authSecretCipher: ByteArray?, // encrypted
    val position: Int
)
```

**DAOs:**
```kotlin
@Dao
interface CollectionDao {
    @Query("""SELECT c.id, c.name, c.position,
              (SELECT COUNT(*) FROM saved_request s WHERE s.collectionId = c.id) AS requestCount
              FROM collection c ORDER BY c.position""")
    fun observeWithCounts(): Flow<List<CollectionWithCount>>
    @Upsert suspend fun upsert(collection: CollectionEntity)
    @Query("UPDATE collection SET name = :name WHERE id = :id") suspend fun rename(id: String, name: String)
    @Query("DELETE FROM collection WHERE id = :id") suspend fun delete(id: String)
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM collection") suspend fun nextPosition(): Int
}

@Dao
interface SavedRequestDao {
    @Query("SELECT * FROM saved_request WHERE collectionId = :cid ORDER BY position")
    fun observeByCollection(cid: String): Flow<List<SavedRequestEntity>>
    @Query("SELECT * FROM saved_request WHERE id = :id") suspend fun getById(id: String): SavedRequestEntity?
    @Upsert suspend fun upsert(request: SavedRequestEntity)
    @Query("UPDATE saved_request SET position = :pos WHERE id = :id") suspend fun updatePosition(id: String, pos: Int)
    @Transaction suspend fun reorder(orderedIds: List<String>) { orderedIds.forEachIndexed { i, id -> updatePosition(id, i) } }
    @Query("DELETE FROM saved_request WHERE id = :id") suspend fun delete(id: String)
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM saved_request WHERE collectionId = :cid") suspend fun nextPosition(cid: String): Int
}
```

**Conflict strategy:** `@Upsert` (REPLACE); FK cascade deletes saved requests when a collection is deleted.
**Refresh policy:** N/A — local-only.
**Mappers:** `data/mapper/CollectionMapper.kt` (`CollectionWithCount -> Collection`) and `data/mapper/SavedRequestMapper.kt` (`SavedRequestEntity <-> SavedRequest`/`SavedRequestDetail`, and `RequestDraft -> SavedRequestEntity`) using `SecretCipher`.

#### UI

**Screens & ViewModels:**
- `CollectionsListScreen` → `CollectionsViewModel(observeCollections, createCollection, renameCollection, deleteCollection)`
  - `CollectionsUiState`: `Loading | Empty | Content(rows) | Error(message)`.
  - **Events:** `OnCollectionClicked(id)`, `OnNewCollection(name)`, `OnRename(id,name)`, `OnDelete(id)`, `OnRetry`.
- `CollectionDetailScreen` → `CollectionDetailViewModel(observeCollectionRequests, getSavedRequestDetail, reorderRequests, removeSavedRequest, renameCollection, deleteCollection)`
  - `CollectionDetailUiState`: `Loading | Empty | Content(name, requests) | Error(message)`.
  - **Events:** `OnRequestClicked(id)`, `OnDuplicate(id)`, `OnReorder(orderedIds)`, `OnRemove(id)`, `OnRenameCollection(name)`, `OnDeleteCollection`, `OnRetry`.

---

### Feature: environments

#### Domain

**Models:**
- `data class Environment(val id: String, val name: String, val isActive: Boolean, val variableCount: Int)`.
- `data class EnvironmentDetail(val id: String, val name: String, val variables: List<Variable>)`.
- `data class Variable(val key: String, val value: String)` — `value` is plaintext in memory, encrypted at rest.
- `data class ResolutionResult(val text: String, val unresolved: List<String>)` — output of variable substitution.

**Use cases:**
- `ObserveEnvironmentsUseCase` — `() -> Flow<AppResult<List<Environment>>>`.
- `ObserveActiveEnvironmentUseCase` — `() -> Flow<String?>` — active env id (from DataStore); drives the switcher and resolution.
- `SetActiveEnvironmentUseCase` — `suspend (id: String?) -> AppResult<Unit>` — `null` => `No environment`.
- `GetEnvironmentDetailUseCase` — `suspend (id: String) -> AppResult<EnvironmentDetail>`.
- `CreateEnvironmentUseCase` — `suspend (name: String) -> AppResult<String>`.
- `RenameEnvironmentUseCase` — `suspend (id: String, name: String) -> AppResult<Unit>`.
- `DeleteEnvironmentUseCase` — `suspend (id: String) -> AppResult<Unit>` — if it was active, falls back to `No environment`.
- `SaveVariablesUseCase` — `suspend (id: String, variables: List<Variable>) -> AppResult<Unit>` — rejects duplicate keys with `Failure(AppError.Validation)`; encrypts values.
- `ResolveVariablesUseCase` — `suspend (text: String, activeEnvId: String?) -> ResolutionResult` — substitutes `{{key}}`; used by `ResolveDraftUseCase` and `GenerateCurlUseCase` (via `VariableResolver`).

**Repository interface:**
```kotlin
interface EnvironmentsRepository {
    fun observeEnvironments(): Flow<List<Environment>>
    fun observeActiveEnvironmentId(): Flow<String?>
    suspend fun setActive(id: String?): AppResult<Unit>
    suspend fun getDetail(id: String): AppResult<EnvironmentDetail>
    suspend fun create(name: String): AppResult<String>
    suspend fun rename(id: String, name: String): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
    suspend fun saveVariables(id: String, variables: List<Variable>): AppResult<Unit>
    suspend fun resolvedVariables(id: String): AppResult<Map<String, String>> // decrypted, for substitution
}
```

#### Data

**Entities (Room):**
```kotlin
@Entity(tableName = "environment")
data class EnvironmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long
)

@Entity(
    tableName = "environment_variable",
    primaryKeys = ["environmentId", "key"],
    foreignKeys = [ForeignKey(entity = EnvironmentEntity::class, parentColumns = ["id"],
        childColumns = ["environmentId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("environmentId")]
)
data class EnvironmentVariableEntity(
    val environmentId: String,
    val key: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val valueCipher: ByteArray  // encrypted
)
```
Active environment id is stored in **DataStore** (`SettingsDataStore`), not Room — it is a single user preference, and decouples "active" from environment rows. Composite PK on (`environmentId`, `key`) enforces unique keys at the DB level (defense-in-depth behind the use-case validation).

**DAOs:**
```kotlin
@Dao
interface EnvironmentDao {
    @Query("""SELECT e.id, e.name,
              (SELECT COUNT(*) FROM environment_variable v WHERE v.environmentId = e.id) AS variableCount
              FROM environment e ORDER BY e.name""")
    fun observeWithCounts(): Flow<List<EnvironmentWithCount>>
    @Query("SELECT * FROM environment WHERE id = :id") suspend fun getById(id: String): EnvironmentEntity?
    @Upsert suspend fun upsert(env: EnvironmentEntity)
    @Query("UPDATE environment SET name = :name WHERE id = :id") suspend fun rename(id: String, name: String)
    @Query("DELETE FROM environment WHERE id = :id") suspend fun delete(id: String)
}

@Dao
interface EnvironmentVariableDao {
    @Query("SELECT * FROM environment_variable WHERE environmentId = :id") suspend fun getForEnvironment(id: String): List<EnvironmentVariableEntity>
    @Query("DELETE FROM environment_variable WHERE environmentId = :id") suspend fun deleteForEnvironment(id: String)
    @Upsert suspend fun upsertAll(vars: List<EnvironmentVariableEntity>)
    @Transaction suspend fun replaceAll(id: String, vars: List<EnvironmentVariableEntity>) { deleteForEnvironment(id); upsertAll(vars) }
}
```

**Conflict strategy:** `@Upsert` (REPLACE); composite PK guarantees per-environment key uniqueness; FK cascade removes variables with their environment.
**Refresh policy:** N/A — local-only.
**Mappers:** `data/mapper/EnvironmentMapper.kt` (`EnvironmentWithCount -> Environment`, `EnvironmentVariableEntity <-> Variable`) decrypting/encrypting `valueCipher` via `SecretCipher`.

#### UI

**Screens & ViewModels:**
- `EnvironmentsListScreen` → `EnvironmentsViewModel(observeEnvironments, observeActiveEnvironment, createEnvironment, renameEnvironment, deleteEnvironment)`
  - `EnvironmentsUiState`: `Loading | Empty | Content(rows, activeId) | Error(message)`.
  - **Events:** `OnEnvironmentClicked(id)`, `OnNewEnvironment(name)`, `OnRename(id,name)`, `OnDelete(id)`, `OnRetry`.
- `EnvironmentDetailScreen` → `EnvironmentDetailViewModel(getEnvironmentDetail, saveVariables, renameEnvironment)`
  - `EnvironmentDetailUiState`: `Loading | Content(name, rows, validationErrors, saving) | Error(message)`.
  - **Events:** `OnNameChanged`, `OnAddVariable`, `OnVariableEdited(index, Variable)`, `OnDeleteVariable(index)`, `OnSave`, `OnRetry`.
- `EnvironmentSwitcher` → `EnvironmentSwitcherViewModel(observeEnvironments, observeActiveEnvironment, setActiveEnvironment)`
  - `SwitcherUiState`: `Content(environments, activeId)` (no loading/error — reads in-memory list).
  - **Events:** `OnSelect(id?)`, `OnManageEnvironments`.

---

### Feature: settings

#### Domain

**Models:**
- `data class AppSettings(val timeoutSeconds: Int = 30, val followRedirects: Boolean = true)`.

**Use cases:**
- `ObserveSettingsUseCase` — `() -> Flow<AppSettings>` — from DataStore.
- `UpdateTimeoutUseCase` — `suspend (seconds: Int) -> AppResult<Unit>` — validates range (e.g. 1..600).
- `UpdateFollowRedirectsUseCase` — `suspend (enabled: Boolean) -> AppResult<Unit>`.
- `ClearHistoryUseCase` — (reused from history) `suspend () -> AppResult<Unit>`.
- `ResetAppUseCase` — `suspend () -> AppResult<Unit>` — wipes history, collections, environments, working draft, encrypted secrets, and resets settings to defaults.

**Repository interface:**
```kotlin
interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setTimeout(seconds: Int): AppResult<Unit>
    suspend fun setFollowRedirects(enabled: Boolean): AppResult<Unit>
    suspend fun resetAll(): AppResult<Unit>   // delegates to AppResetCoordinator
}
```

#### Data
No Room entities. `SettingsRepositoryImpl` reads/writes `SettingsDataStore`. `AppResetCoordinator` is injected with all DAOs + DataStore + `SecretCipher` and performs the wipe in a single coordinated operation (`@Transaction` per DB, plus DataStore clear, plus keystore key rotation so old ciphertext is unreadable).

**Refresh policy:** N/A — local preference store.
**Mappers:** trivial DataStore <-> `AppSettings`.

#### UI

- `SettingsScreen` → `SettingsViewModel(observeSettings, updateTimeout, updateFollowRedirects, clearHistory, resetApp)`
  - `SettingsUiState`: `Content(settings, busyAction, actionError)` where `busyAction ∈ {None, ClearingHistory, Resetting}`.
  - **Events:** `OnTimeoutChanged(Int)`, `OnFollowRedirectsToggled(Boolean)`, `OnClearHistoryConfirmed`, `OnResetConfirmed`, `OnDismissError`.

---

### Feature: docs

#### Domain

**Models:**
- `data class DocsSection(val id: String, val title: String, val kind: DocsKind)` ; `enum class DocsKind { METHODS, STATUS_CODES, HEADERS, BODY_FORMATS }`.
- `data class DocsArticle(val id: String, val title: String, val markdown: String)`.

**Use cases:**
- `GetDocsIndexUseCase` — `suspend () -> AppResult<List<DocsSection>>` — bundled, offline.
- `GetDocsArticleUseCase` — `suspend (id: String) -> AppResult<DocsArticle>` — bundled, offline.
- `SearchDocsUseCase` — `(query: String) -> List<DocsSection>` — filters bundled index.

**Repository interface:**
```kotlin
interface DocsRepository {
    suspend fun getIndex(): AppResult<List<DocsSection>>
    suspend fun getArticle(id: String): AppResult<DocsArticle>
}
```

#### Data
No Room. `DocsRepositoryImpl` reads bundled JSON/Markdown from `assets/docs/`. `AppError.NotFound` if an article id is missing (a packaging bug). No mappers beyond JSON deserialization.

#### UI
- `DocsIndexScreen` → `DocsViewModel(getDocsIndex, searchDocs)` — `DocsUiState: Loading | Content(sections, query) | Error`.
  - **Events:** `OnSectionClicked(id)`, `OnQueryChanged(String)`.
- `DocsArticleScreen` → `DocsArticleViewModel(getDocsArticle)` — `ArticleUiState: Loading | Content(article) | Error`.
  - **Events:** `OnBack`.

---

## DI graph (Hilt)

All modules `@InstallIn(SingletonComponent::class)`. `@Binds` interfaces; `@Provides` for framework objects. Repos/DAOs/engine/cipher/datastore are `@Singleton`; use cases default (unscoped — cheap, stateless).

```kotlin
// core/database/DatabaseModule.kt
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun provideDb(@ApplicationContext c: Context, cipher: SecretCipher): RequestLabDatabase =
        Room.databaseBuilder(c, RequestLabDatabase::class.java, "requestlab.db")
            .addMigrations(/* none yet */).build()
    @Provides fun provideHistoryDao(db: RequestLabDatabase) = db.historyDao()
    @Provides fun provideCollectionDao(db: RequestLabDatabase) = db.collectionDao()
    @Provides fun provideSavedRequestDao(db: RequestLabDatabase) = db.savedRequestDao()
    @Provides fun provideEnvironmentDao(db: RequestLabDatabase) = db.environmentDao()
    @Provides fun provideEnvironmentVariableDao(db: RequestLabDatabase) = db.environmentVariableDao()
    @Provides fun provideWorkingDraftDao(db: RequestLabDatabase) = db.workingDraftDao()
}

// core/common/DispatcherModule.kt
@Module @InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher fun io(): CoroutineDispatcher = Dispatchers.IO
    @Provides @DefaultDispatcher fun default(): CoroutineDispatcher = Dispatchers.Default
}

// core/crypto/CryptoModule.kt
@Module @InstallIn(SingletonComponent::class)
abstract class CryptoModule {
    @Binds @Singleton abstract fun bindCipher(impl: KeystoreSecretCipher): SecretCipher
}

// core/network/NetworkModule.kt
@Module @InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds @Singleton abstract fun bindEngine(impl: OkHttpHttpEngine): HttpEngine
    companion object {
        @Provides @Singleton fun provideClientProvider(): HttpClientProvider = HttpClientProvider()
    }
}

// core/datastore/DataStoreModule.kt — @Provides @Singleton SettingsDataStore
// core/common (connectivity) — @Binds ConnectivityObserver

// feature/builder/data/BuilderDataModule.kt
@Module @InstallIn(SingletonComponent::class)
abstract class BuilderDataModule {
    @Binds @Singleton abstract fun bindDraftRepo(impl: RequestDraftRepositoryImpl): RequestDraftRepository
    @Binds @Singleton abstract fun bindSendRepo(impl: SendRepositoryImpl): SendRepository
}

// feature/history/data/HistoryDataModule.kt
@Module @InstallIn(SingletonComponent::class)
abstract class HistoryDataModule {
    @Binds @Singleton abstract fun bindHistoryRepo(impl: HistoryRepositoryImpl): HistoryRepository
}

// feature/collections/data/CollectionsDataModule.kt
@Binds @Singleton bindCollectionsRepo(impl: CollectionsRepositoryImpl): CollectionsRepository
// feature/environments/data/EnvironmentsDataModule.kt
@Binds @Singleton bindEnvironmentsRepo(impl: EnvironmentsRepositoryImpl): EnvironmentsRepository
// feature/settings/data/SettingsDataModule.kt
@Binds @Singleton bindSettingsRepo(impl: SettingsRepositoryImpl): SettingsRepository
@Provides @Singleton provideResetCoordinator(...all DAOs, dataStore, cipher...): AppResetCoordinator
// feature/docs/data/DocsDataModule.kt
@Binds @Singleton bindDocsRepo(impl: DocsRepositoryImpl): DocsRepository
```

ViewModels are `@HiltViewModel` with `@Inject constructor(...)` taking use cases (+ `SavedStateHandle` where a nav arg / draft source is needed).

---

## Error model

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object NoInternet : AppError                          // gates Send only
    data class Timeout(val seconds: Int) : AppError            // transport
    data object Dns : AppError
    data object Tls : AppError
    data object Cancelled : AppError
    data class Validation(val field: String, val message: String) : AppError  // URL, unresolved var, dup key, file missing
    data object NotFound : AppError                            // history/saved-request/article deleted or missing
    data class Storage(val cause: Throwable) : AppError        // Room/DataStore/SAF/crypto failure
    data class Unknown(val cause: Throwable) : AppError
}
```
Note: transport-level `AppError`s map 1:1 to `FailureKind` when building a `TransportFailure` for history. Non-2xx HTTP responses are **not** errors — they are `AppResult.Success(SendOutcome(response=...))`, since an HTTP exchange completed.

**Mapping table:**
| Data layer source | Becomes | UI shows |
| --- | --- | --- |
| No connectivity at send time (`ConnectivityObserver`) | `AppError.NoInternet` | Builder inline: "You're offline — sending requires a connection." No history entry. |
| `SocketTimeoutException` / exceeds configured timeout | `AppError.Timeout(s)` -> history `FailureKind.TIMEOUT` | Response error card "Timeout after {s} s" + Retry; History `FAILED` chip + reason. |
| `UnknownHostException` (DNS) | `AppError.Dns` -> `FailureKind.DNS` | Response error card "Couldn't resolve host" + Retry; History `FAILED`. |
| `SSLException` / cert failure | `AppError.Tls` -> `FailureKind.TLS` | Response error card "TLS handshake failed" + Retry; History `FAILED`. |
| Coroutine cancellation (back-grounded/Cancel) | `AppError.Cancelled` | Response returns to prior/empty state; no duplicate history entry. |
| Empty/invalid URL, unresolved `{{var}}`, dup env key, missing file | `AppError.Validation(field,msg)` | Builder/EnvDetail inline message naming the field (e.g. "{{baseId}} has no value in the active environment"). Send blocked, no history. |
| Room/DataStore returns null where row expected | `AppError.NotFound` | "This request is no longer available" / "Article not found". |
| `SQLiteException`, `IOException` on local store, Keystore decrypt failure | `AppError.Storage` | "Couldn't load …" / "Couldn't save — try again" with Retry. |
| Any other throwable | `AppError.Unknown` | "Something went wrong. Try again." |

A central `runCatchingToAppError { }` helper in `core/common` wraps data-layer calls and classifies exceptions into `AppError`.

---

## Test plan

Stack: JUnit5, kotlinx-coroutines-test (`runTest`, `StandardTestDispatcher`), Turbine for Flows, Robolectric only where a Room in-memory DB or `Context` is needed for DAO/crypto tests. **Fakes over mocks** — `FakeHistoryRepository`, `FakeEnvironmentsRepository`, `FakeHttpEngine`, `FakeConnectivityObserver`, `FakeSecretCipher` (identity transform for non-crypto tests). MockK only where verifying interaction order/side-effects is the point (e.g. `AppResetCoordinator` calls every DAO clear).

| Feature | Layer | Class | Scenario — what to assert | Test file |
| --- | --- | --- | --- | --- |
| builder | domain | `LoadDraftUseCase` | `New` returns empty GET draft with empty URL | `feature/builder/domain/LoadDraftUseCaseTest.kt` |
| builder | domain | `LoadDraftUseCase` | `FromHistory(id)` rehydrates method/url/headers/body/auth from history detail | same |
| builder | domain | `LoadDraftUseCase` | `Duplicate(id)` returns a draft with a NEW id, `isDirty=true`, `sourceSavedRequestId=null` | same |
| builder | domain | `ResolveDraftUseCase` | substitutes `{{baseId}}` in URL using active env; resolvedUrl has no tokens | `feature/builder/domain/ResolveDraftUseCaseTest.kt` |
| builder | domain | `ResolveDraftUseCase` | unresolved `{{baseId}}` (no active env) -> `Failure(Validation)` naming the variable | same |
| builder | domain | `ResolveDraftUseCase` | empty URL -> `Failure(Validation("url", ...))` | same |
| builder | domain | `SendRequestUseCase` | offline -> `Failure(NoInternet)`, engine NOT called, nothing recorded | `feature/builder/domain/SendRequestUseCaseTest.kt` |
| builder | domain | `SendRequestUseCase` | 200 response -> `Success(SendOutcome(response))` AND history.record called once | same |
| builder | domain | `SendRequestUseCase` | 404 response -> `Success` (not error), outcome carries statusCode=404, recorded | same |
| builder | domain | `SendRequestUseCase` | engine throws `SocketTimeoutException` -> `Success(SendOutcome(failure=TIMEOUT))`, recorded as FAILED | same |
| builder | domain | `SendRequestUseCase` | cancellation -> `Failure(Cancelled)`, no history entry written | same |
| builder | domain | `GenerateCurlUseCase` | Basic auth, includeCredentials=false -> `-u [username]:[redacted]` present, password absent | `feature/builder/domain/GenerateCurlUseCaseTest.kt` |
| builder | domain | `GenerateCurlUseCase` | Bearer, includeCredentials=true -> real token emitted in Authorization header | same |
| builder | domain | `GenerateCurlUseCase` | unresolved var -> left literal `{{baseId}}` AND listed in `unresolvedVariables` | same |
| builder | domain | `GetHeaderSuggestionsUseCase` | prefix "Cont" returns list containing "Content-Type"; empty prefix returns [] or full set per rule | `feature/builder/domain/HeaderSuggestionsTest.kt` |
| builder | domain | `GetHeaderValueSuggestionsUseCase` | key "Content-Type" returns values containing "application/json"; free-form key returns [] | same |
| builder | data | `SendRepositoryImpl` | pulls timeout/followRedirects from SettingsRepository into RequestConfig before calling engine | `feature/builder/data/SendRepositoryImplTest.kt` |
| builder | data | `SendRepositoryImpl` | offline branch: ConnectivityObserver=false -> NoInternet, engine never invoked (offline test) | same |
| builder | data | `SendRepositoryImpl` | success path: records outcome via HistoryRepository and returns it (refresh/write test) | same |
| builder | data | `RequestDraftRepositoryImpl` | saveWorkingDraft then observeWorkingDraft emits the draft; secret round-trips through cipher | `feature/builder/data/RequestDraftRepositoryImplTest.kt` |
| builder | data | `WorkingDraftDao` | upsert + observe round-trip; clear empties the single row | `feature/builder/data/WorkingDraftDaoTest.kt` |
| builder | ui | `BuilderViewModel` | initial `New` -> draftStatus Ready, sendEnabled=false (empty URL), response Empty | `feature/builder/ui/BuilderViewModelTest.kt` |
| builder | ui | `BuilderViewModel` | URL entered -> sendEnabled=true | same |
| builder | ui | `BuilderViewModel` | OnSendClicked happy: Sending -> response Content(200), phone auto-switches to RESPONSE, responseAvailable=true | same |
| builder | ui | `BuilderViewModel` | OnSendClicked while offline -> inlineError offline message, sendState back to Idle, no pane switch | same |
| builder | ui | `BuilderViewModel` | send transport failure -> response Failure card with reason, Retry available | same |
| builder | ui | `BuilderViewModel` | unresolved variable -> inlineError naming variable, engine not invoked | same |
| builder | ui | `SaveToCollectionViewModel` | no collections -> Empty state with prefilled request name | `feature/builder/ui/SaveToCollectionViewModelTest.kt` |
| builder | ui | `SaveToCollectionViewModel` | OnSaveConfirmed success -> dismiss signal; save failure -> Content stays open with error | same |
| builder | ui | `CurlExportViewModel` | toggle includeCredentials re-generates command (redacted vs revealed) | `feature/builder/ui/CurlExportViewModelTest.kt` |
| history | domain | `ObserveHistoryUseCase` | emits cached entries reverse-chronologically while offline (no network involved) | `feature/history/domain/ObserveHistoryUseCaseTest.kt` |
| history | domain | `GetHistoryDetailUseCase` | known id -> full detail with decrypted auth; missing id -> `Failure(NotFound)` | `feature/history/domain/GetHistoryDetailUseCaseTest.kt` |
| history | domain | `RecordSendOutcomeUseCase` | success outcome -> entry with statusCode set, failureKind null | `feature/history/domain/RecordSendOutcomeUseCaseTest.kt` |
| history | domain | `RecordSendOutcomeUseCase` | transport failure outcome -> entry with failureKind set, statusCode null | same |
| history | domain | `ClearHistoryUseCase` | clears repo; subsequent observe emits empty | `feature/history/domain/ClearHistoryUseCaseTest.kt` |
| history | data | `HistoryRepositoryImpl` | record(success) writes uncapped full body; getDetail returns it intact (>500KB body) | `feature/history/data/HistoryRepositoryImplTest.kt` |
| history | data | `HistoryRepositoryImpl` | offline read returns DB rows; secret fields decrypt via cipher (offline test) | same |
| history | data | `HistoryRepositoryImpl` | record then observe emits the new row (write+observe test) | same |
| history | data | `HistoryDao` | insert + observeSummaries round-trip ordered by sent_at DESC | `feature/history/data/HistoryDaoTest.kt` |
| history | data | `HistoryDao` | delete removes one; clearAll empties table | same |
| history | ui | `HistoryListViewModel` | repo emits rows -> Content; empty repo -> Empty | `feature/history/ui/HistoryListViewModelTest.kt` |
| history | ui | `HistoryListViewModel` | repo read error -> Error("Couldn't load history") with retry | same |
| history | ui | `HistoryListViewModel` | offline observer true -> isOffline flag set in Content | same |
| history | ui | `HistoryDetailViewModel` | known id -> Content(detail); deleted id -> NotFound | `feature/history/ui/HistoryDetailViewModelTest.kt` |
| collections | domain | `CreateCollectionUseCase` | creates collection, returns id; appears in observeCollections | `feature/collections/domain/CreateCollectionUseCaseTest.kt` |
| collections | domain | `SaveRequestUseCase` | persists draft into collection; secrets encrypted; count increments | `feature/collections/domain/SaveRequestUseCaseTest.kt` |
| collections | domain | `DeleteCollectionUseCase` | deletes collection and cascades its saved requests | `feature/collections/domain/DeleteCollectionUseCaseTest.kt` |
| collections | domain | `ReorderRequestsUseCase` | reordered ids produce ascending positions reflected in observe | `feature/collections/domain/ReorderRequestsUseCaseTest.kt` |
| collections | domain | `GetSavedRequestDetailUseCase` | returns full detail with decrypted auth; missing -> NotFound | `feature/collections/domain/GetSavedRequestDetailUseCaseTest.kt` |
| collections | data | `CollectionsRepositoryImpl` | saveRequest round-trips through DAO; getRequestDetail decrypts secret (offline) | `feature/collections/data/CollectionsRepositoryImplTest.kt` |
| collections | data | `SavedRequestDao` | reorder transaction renumbers positions 0..n; observe ordered (round-trip) | `feature/collections/data/SavedRequestDaoTest.kt` |
| collections | data | `CollectionDao` | observeWithCounts returns correct requestCount; FK cascade deletes children | `feature/collections/data/CollectionDaoTest.kt` |
| collections | ui | `CollectionsViewModel` | rows -> Content; empty -> Empty; read error -> Error | `feature/collections/ui/CollectionsViewModelTest.kt` |
| collections | ui | `CollectionDetailViewModel` | OnReorder persists then reflects new order; remove updates list | `feature/collections/ui/CollectionDetailViewModelTest.kt` |
| environments | domain | `SaveVariablesUseCase` | duplicate keys -> `Failure(Validation)`; valid set saves encrypted | `feature/environments/domain/SaveVariablesUseCaseTest.kt` |
| environments | domain | `ResolveVariablesUseCase` | `{{k}}` substituted from active env; missing key recorded in unresolved | `feature/environments/domain/ResolveVariablesUseCaseTest.kt` |
| environments | domain | `SetActiveEnvironmentUseCase` | sets id; observeActive emits it; null sets No environment | `feature/environments/domain/SetActiveEnvironmentUseCaseTest.kt` |
| environments | domain | `DeleteEnvironmentUseCase` | deleting the active env falls back active to null | `feature/environments/domain/DeleteEnvironmentUseCaseTest.kt` |
| environments | data | `EnvironmentsRepositoryImpl` | saveVariables encrypts values; resolvedVariables returns decrypted map (offline) | `feature/environments/data/EnvironmentsRepositoryImplTest.kt` |
| environments | data | `EnvironmentVariableDao` | replaceAll transaction swaps the whole set; composite PK rejects dup key insert | `feature/environments/data/EnvironmentVariableDaoTest.kt` |
| environments | data | `EnvironmentDao` | observeWithCounts variableCount correct; cascade deletes variables | `feature/environments/data/EnvironmentDaoTest.kt` |
| environments | ui | `EnvironmentDetailViewModel` | duplicate-key edit surfaces validation; valid save -> success | `feature/environments/ui/EnvironmentDetailViewModelTest.kt` |
| environments | ui | `EnvironmentDetailViewModel` | save failure -> Error("Couldn't save changes") | same |
| environments | ui | `EnvironmentSwitcherViewModel` | OnSelect updates active; activeId reflected in state | `feature/environments/ui/EnvironmentSwitcherViewModelTest.kt` |
| environments | ui | `EnvironmentsViewModel` | rows -> Content with activeId; empty -> Empty | `feature/environments/ui/EnvironmentsViewModelTest.kt` |
| settings | domain | `UpdateTimeoutUseCase` | out-of-range -> `Failure(Validation)`; valid -> persisted | `feature/settings/domain/UpdateTimeoutUseCaseTest.kt` |
| settings | domain | `ResetAppUseCase` | invokes coordinator; verify history/collections/env/draft/datastore all cleared | `feature/settings/domain/ResetAppUseCaseTest.kt` |
| settings | data | `SettingsRepositoryImpl` | setTimeout then observeSettings emits new value; defaults are 30s/redirects-on | `feature/settings/data/SettingsRepositoryImplTest.kt` |
| settings | data | `AppResetCoordinator` | calls every DAO clear + DataStore clear + cipher key rotation exactly once (MockK justified) | `feature/settings/data/AppResetCoordinatorTest.kt` |
| settings | ui | `SettingsViewModel` | OnTimeoutChanged persists; OnResetConfirmed shows busy then success | `feature/settings/ui/SettingsViewModelTest.kt` |
| settings | ui | `SettingsViewModel` | failing reset -> actionError set | same |
| docs | domain | `GetDocsIndexUseCase` | returns four sections (Methods/Status codes/Headers/Body formats) from bundled assets | `feature/docs/domain/GetDocsIndexUseCaseTest.kt` |
| docs | domain | `GetDocsArticleUseCase` | known id -> article; unknown id -> `Failure(NotFound)` | `feature/docs/domain/GetDocsArticleUseCaseTest.kt` |
| docs | domain | `SearchDocsUseCase` | query filters index case-insensitively | `feature/docs/domain/SearchDocsUseCaseTest.kt` |
| docs | ui | `DocsViewModel` | index loads -> Content; query narrows sections | `feature/docs/ui/DocsViewModelTest.kt` |
| docs | ui | `DocsArticleViewModel` | known id -> Content(article); missing -> Error | `feature/docs/ui/DocsArticleViewModelTest.kt` |
| core | crypto | `KeystoreSecretCipher` | encrypt then decrypt round-trips to original; distinct ciphertext per call (IV) | `core/crypto/KeystoreSecretCipherTest.kt` (Robolectric) |
| core | network | `OkHttpHttpEngine` | applies timeout + followRedirects from RequestConfig; classifies timeout/DNS/TLS exceptions to FailureKind | `core/network/OkHttpHttpEngineTest.kt` (MockWebServer) |
| core | common | `runCatchingToAppError` | maps IOException->Storage, SocketTimeout->Timeout, UnknownHost->Dns, SSLException->Tls | `core/common/ErrorMappingTest.kt` |

---

## Gradle / build notes
- **compileSdk / targetSdk:** 35.
- **minSdk:** 26 (Android 8.0). Justified: `AndroidKeyStore` AES/GCM with `setUnlockedDeviceRequired`/reliable GCM support, `java.time` (Instant) without desugaring, and modern SAF persistable URI permissions all land cleanly at 26. No UX flow requires anything below 26; nothing in the UX spec is constrained by this floor.
- **Kotlin 2.0+** with Compose compiler plugin; **KSP** for Room and Hilt (no kapt).
- **Version catalog** (`libs.versions.toml`) for all dependencies.
- Key libs: Jetpack Compose + Material3 + `material3-adaptive`/`WindowSizeClass` (phone tabs vs tablet two-pane), Navigation-Compose, Room (+`room-testing`), Hilt (+`hilt-navigation-compose`), DataStore (Preferences), OkHttp (+`mockwebserver`), kotlinx-serialization (entity JSON columns + docs/header assets), kotlinx-coroutines-test, Turbine, Robolectric, JUnit5.
- Room `exportSchema = true` (schemas committed under `app/schemas/` for migration tests). No migrations yet (schema v1).
- `assets/docs/` holds bundled Markdown/JSON; `assets/headers.json` (or a Kotlin constant table) holds well-known header names + constrained value sets.

---

## UX feedback

1. **(Response — "Cancel" affordance + Edge case 1, backgrounded mid-send)** OkHttp/coroutine cancellation is feasible, but the spec's promise that a backgrounded send "continues or is cancelled cleanly … with no duplicate entries" needs a concrete decision the UX leaves open. Recommendation: scope the send coroutine to a `viewModelScope` that survives configuration changes but **cancels on explicit user Cancel only**, and record the history entry exactly once at terminal state (success/failure/cancelled-after-response). For a true process-death mid-send, the request is lost and no entry is written — please confirm this is acceptable (it is the only safe option without a foreground service). If "continues across process death" is truly required, that forces a foreground `Service`/`WorkManager` send path, which is a meaningful cost increase. Flagging for a decision, not blocking.

2. **(Response — 500 KB render cap)** The "render up to 500 KB, persist full body uncapped" rule is sound, but very large bodies (tens of MB) stored as a Room `TEXT` column can bloat the DB and slow `getDetail`. Cheap and within scope, but recommend a soft persistence ceiling (e.g. warn/skip-persist above, say, 25 MB) to avoid pathological history growth on an "unlimited history" app. Minor; can be a Settings-era follow-up.

3. **(Edge case 13 — SAF permission for binary upload)** Storing only the URI reference (not bytes) means a saved/replayed request can reference a file whose persistable permission was revoked or that was moved/deleted. The UX already handles "file no longer available," which is correct — no change needed. Noting only that we will request `takePersistableUriPermission` at pick time so the reference survives reboots; this matches the spec and needs no UX amendment.

No infeasible or blocking flows. Items above are confirmations/cost-flags, not redesigns.
