package com.example.requestlab.feature.history.domain.usecase

import com.example.requestlab.feature.history.domain.HistoryRepository
import com.example.requestlab.feature.history.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    operator fun invoke(): Flow<List<HistoryEntry>> = repository.observeHistory()
}
