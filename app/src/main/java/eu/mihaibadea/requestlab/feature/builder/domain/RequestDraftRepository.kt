package eu.mihaibadea.requestlab.feature.builder.domain

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.builder.domain.model.DraftSource
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import kotlinx.coroutines.flow.Flow

interface RequestDraftRepository {
    suspend fun loadDraft(source: DraftSource): AppResult<RequestDraft>
    suspend fun saveWorkingDraft(draft: RequestDraft): AppResult<Unit>
    fun observeWorkingDraft(): Flow<RequestDraft?>
}
