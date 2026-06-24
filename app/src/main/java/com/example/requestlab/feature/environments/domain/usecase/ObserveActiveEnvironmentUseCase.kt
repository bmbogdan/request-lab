package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveEnvironmentUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    operator fun invoke(): Flow<String?> = repo.observeActiveEnvironmentId()
}
