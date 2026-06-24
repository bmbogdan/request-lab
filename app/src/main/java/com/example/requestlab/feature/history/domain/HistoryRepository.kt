package com.example.requestlab.feature.history.domain

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.feature.history.domain.model.HistoryDetail
import com.example.requestlab.feature.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryEntry>>
    suspend fun getDetail(id: String): AppResult<HistoryDetail>
    suspend fun record(outcome: SendOutcome, environmentName: String?): AppResult<String>
    suspend fun delete(id: String): AppResult<Unit>
    suspend fun clearAll(): AppResult<Unit>
}
