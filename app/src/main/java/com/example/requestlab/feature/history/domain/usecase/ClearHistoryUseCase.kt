package com.example.requestlab.feature.history.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.history.domain.HistoryRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.clearAll()
}
