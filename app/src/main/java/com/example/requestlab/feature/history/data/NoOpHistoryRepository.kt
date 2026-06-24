package com.example.requestlab.feature.history.data

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.feature.history.domain.HistoryRepository
import com.example.requestlab.feature.history.domain.model.HistoryDetail
import com.example.requestlab.feature.history.domain.model.HistoryEntry
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
