package eu.mihaibadea.requestlab.feature.history.data

import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.IoDispatcher
import eu.mihaibadea.requestlab.core.common.model.SendOutcome
import eu.mihaibadea.requestlab.core.common.runCatchingToAppResult
import eu.mihaibadea.requestlab.feature.history.data.dao.HistoryDao
import eu.mihaibadea.requestlab.feature.history.data.mapper.toDetail
import eu.mihaibadea.requestlab.feature.history.data.mapper.toEntry
import eu.mihaibadea.requestlab.feature.history.data.mapper.toEntity
import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryDetail
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryEntry
import eu.mihaibadea.requestlab.feature.settings.data.AppResetCoordinator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
    coordinator: AppResetCoordinator,
    @IoDispatcher private val io: CoroutineDispatcher,
) : HistoryRepository {

    init {
        coordinator.clearHistory = { dao.clearAll() }
    }

    override fun observeHistory(): Flow<List<HistoryEntry>> =
        dao.observeAll().map { entities -> entities.map { it.toEntry() } }

    override suspend fun getDetail(id: String): AppResult<HistoryDetail> =
        runCatchingToAppResult {
            withContext(io) {
                dao.getById(id)?.toDetail() ?: throw NoSuchElementException(id)
            }
        }

    override suspend fun record(outcome: SendOutcome, environmentName: String?): AppResult<String> =
        runCatchingToAppResult {
            withContext(io) {
                val id = UUID.randomUUID().toString()
                dao.insert(outcome.toEntity(id, environmentName))
                id
            }
        }

    override suspend fun delete(id: String): AppResult<Unit> =
        runCatchingToAppResult { withContext(io) { dao.deleteById(id) } }

    override suspend fun clearAll(): AppResult<Unit> =
        runCatchingToAppResult { withContext(io) { dao.clearAll() } }
}
