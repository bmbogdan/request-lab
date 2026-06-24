package com.example.requestlab.feature.builder.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.builder.domain.RequestDraftRepository
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import javax.inject.Inject

class UpdateDraftUseCase @Inject constructor(
    private val draftRepository: RequestDraftRepository,
) {
    suspend operator fun invoke(draft: RequestDraft): AppResult<Unit> =
        draftRepository.saveWorkingDraft(draft.copy(isDirty = true))
}
