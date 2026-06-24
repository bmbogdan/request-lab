package com.example.requestlab.feature.history.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.history.domain.HistoryRepository
import com.example.requestlab.feature.history.domain.model.HistoryDetail
import javax.inject.Inject

class GetHistoryDetailUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: String): AppResult<HistoryDetail> = repository.getDetail(id)
}
