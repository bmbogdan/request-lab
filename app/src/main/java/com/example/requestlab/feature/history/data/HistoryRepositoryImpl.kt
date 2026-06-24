package com.example.requestlab.feature.history.data

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.IoDispatcher
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.core.common.runCatchingToAppResult
import com.example.requestlab.feature.history.data.dao.HistoryDao
import com.example.requestlab.feature.history.data.mapper.toDetail
import com.example.requestlab.feature.history.data.mapper.toEntry
import com.example.requestlab.feature.history.data.mapper.toEntity
import com.example.requestlab.feature.history.domain.HistoryRepository
import com.example.requestlab.feature.history.domain.model.HistoryDetail
import com.example.requestlab.feature.history.domain.model.HistoryEntry
import com.example.requestlab.feature.settings.data.AppResetCoordinator
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
