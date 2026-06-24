package eu.mihaibadea.requestlab.feature.history.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryDetail
import javax.inject.Inject

class GetHistoryDetailUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: String): AppResult<HistoryDetail> = repository.getDetail(id)
}
