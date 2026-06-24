package eu.mihaibadea.requestlab.core.testing.fake

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.model.SendOutcome
import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryDetail
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryEntry
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class FakeHistoryRepository : HistoryRepository {

    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())

    fun setEntries(entries: List<HistoryEntry>) { _entries.value = entries }

    override fun observeHistory(): Flow<List<HistoryEntry>> = _entries.asStateFlow()

    override suspend fun getDetail(id: String): AppResult<HistoryDetail> =
        AppResult.Failure(eu.mihaibadea.requestlab.core.common.AppError.NotFound)

    override suspend fun record(outcome: SendOutcome, environmentName: String?): AppResult<String> =
        AppResult.Success("fake-id")

    override suspend fun delete(id: String): AppResult<Unit> {
        _entries.value = _entries.value.filter { it.id != id }
        return AppResult.Success(Unit)
    }

    override suspend fun clearAll(): AppResult<Unit> {
        _entries.value = emptyList()
        return AppResult.Success(Unit)
    }

    companion object {
        fun entry(id: String = "e1") = HistoryEntry(
            id = id,
            method = "GET",
            resolvedUrl = "https://api.example.com/users",
            status = HistoryStatus.Http(200),
            latencyMs = 123,
            sentAt = Instant.ofEpochMilli(1_718_000_000_000L),
            environmentName = null,
        )
    }
}
