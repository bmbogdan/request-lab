package com.example.requestlab.feature.collections.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.collections.domain.CollectionsRepository
import com.example.requestlab.feature.collections.domain.model.SavedRequestDetail
import javax.inject.Inject

class GetSavedRequestDetailUseCase @Inject constructor(private val repository: CollectionsRepository) {
    suspend operator fun invoke(id: String): AppResult<SavedRequestDetail> =
        repository.getRequestDetail(id)
}
