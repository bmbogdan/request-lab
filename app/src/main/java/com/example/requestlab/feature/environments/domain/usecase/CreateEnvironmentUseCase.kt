package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import javax.inject.Inject

class CreateEnvironmentUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    suspend operator fun invoke(name: String): AppResult<String> = repo.create(name)
}
