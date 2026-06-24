package eu.mihaibadea.requestlab.feature.collections.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.collections.domain.CollectionsRepository
import javax.inject.Inject

class RenameCollectionUseCase @Inject constructor(private val repository: CollectionsRepository) {
    suspend operator fun invoke(id: String, name: String): AppResult<Unit> =
        repository.renameCollection(id, name)
}
