package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import com.example.requestlab.feature.environments.domain.model.Environment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEnvironmentsUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    operator fun invoke(): Flow<List<Environment>> = repo.observeEnvironments()
}
