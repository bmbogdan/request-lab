package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import javax.inject.Inject

class RenameEnvironmentUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    suspend operator fun invoke(id: String, name: String): AppResult<Unit> = repo.rename(id, name)
}
