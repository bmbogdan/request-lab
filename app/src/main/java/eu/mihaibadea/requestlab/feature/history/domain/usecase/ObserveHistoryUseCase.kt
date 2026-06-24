package eu.mihaibadea.requestlab.feature.history.domain.usecase

import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import eu.mihaibadea.requestlab.feature.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    operator fun invoke(): Flow<List<HistoryEntry>> = repository.observeHistory()
}
