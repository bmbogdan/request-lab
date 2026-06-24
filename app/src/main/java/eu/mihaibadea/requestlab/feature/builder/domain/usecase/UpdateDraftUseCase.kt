package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.builder.domain.RequestDraftRepository
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import javax.inject.Inject

class UpdateDraftUseCase @Inject constructor(
    private val draftRepository: RequestDraftRepository,
) {
    suspend operator fun invoke(draft: RequestDraft): AppResult<Unit> =
        draftRepository.saveWorkingDraft(draft.copy(isDirty = true))
}
