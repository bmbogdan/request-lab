package com.example.requestlab.feature.builder.domain

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.builder.domain.model.DraftSource
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import kotlinx.coroutines.flow.Flow

interface RequestDraftRepository {
    suspend fun loadDraft(source: DraftSource): AppResult<RequestDraft>
    suspend fun saveWorkingDraft(draft: RequestDraft): AppResult<Unit>
    fun observeWorkingDraft(): Flow<RequestDraft?>
}
