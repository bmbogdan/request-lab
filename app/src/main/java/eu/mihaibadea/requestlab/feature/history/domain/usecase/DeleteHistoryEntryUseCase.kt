package eu.mihaibadea.requestlab.feature.history.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import javax.inject.Inject

class DeleteHistoryEntryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.delete(id)
}
