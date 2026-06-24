package eu.mihaibadea.requestlab.feature.history.data

import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.model.SendOutcome
import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryDetail
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpHistoryRepository @Inject constructor() : HistoryRepository {
    override fun observeHistory(): Flow<List<HistoryEntry>> = flowOf(emptyList())
    override suspend fun getDetail(id: String): AppResult<HistoryDetail> =
        AppResult.Failure(AppError.NotFound)
    override suspend fun record(outcome: SendOutcome, environmentName: String?): AppResult<String> =
        AppResult.Success("")
    override suspend fun delete(id: String): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun clearAll(): AppResult<Unit> = AppResult.Success(Unit)
}
