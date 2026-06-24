package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import com.example.requestlab.feature.environments.domain.model.EnvironmentDetail
import javax.inject.Inject

class GetEnvironmentDetailUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    suspend operator fun invoke(id: String): AppResult<EnvironmentDetail> = repo.getDetail(id)
}
