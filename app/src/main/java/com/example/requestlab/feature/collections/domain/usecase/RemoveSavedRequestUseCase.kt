package com.example.requestlab.feature.collections.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.collections.domain.CollectionsRepository
import javax.inject.Inject

class RemoveSavedRequestUseCase @Inject constructor(private val repository: CollectionsRepository) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.removeRequest(id)
}
