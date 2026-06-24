package eu.mihaibadea.requestlab.feature.collections.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.collections.domain.CollectionsRepository
import eu.mihaibadea.requestlab.feature.collections.domain.model.SavedRequestDetail
import javax.inject.Inject

class GetSavedRequestDetailUseCase @Inject constructor(private val repository: CollectionsRepository) {
    suspend operator fun invoke(id: String): AppResult<SavedRequestDetail> =
        repository.getRequestDetail(id)
}
