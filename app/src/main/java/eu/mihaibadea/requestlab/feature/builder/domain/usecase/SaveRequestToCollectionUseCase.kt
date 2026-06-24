package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.map
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.collections.domain.CollectionsRepository
import javax.inject.Inject

class SaveRequestToCollectionUseCase @Inject constructor(
    private val collectionsRepository: CollectionsRepository,
) {
    suspend operator fun invoke(
        draft: RequestDraft,
        collectionId: String?,
        newCollectionName: String?,
        requestName: String,
    ): AppResult<String> {
        val targetCollectionId = if (collectionId != null) {
            collectionId
        } else if (!newCollectionName.isNullOrBlank()) {
            val created = collectionsRepository.createCollection(newCollectionName)
            if (created is AppResult.Failure) return created
            (created as AppResult.Success).value
        } else {
            return AppResult.Failure(
                eu.mihaibadea.requestlab.core.common.AppError.Validation(
                    "collection", "Choose an existing collection or enter a new name",
                ),
            )
        }
        return collectionsRepository.saveRequest(targetCollectionId, requestName, draft)
    }
}
