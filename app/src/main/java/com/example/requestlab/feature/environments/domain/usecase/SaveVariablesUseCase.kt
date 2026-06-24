package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import com.example.requestlab.feature.environments.domain.model.Variable
import javax.inject.Inject

class SaveVariablesUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    suspend operator fun invoke(id: String, variables: List<Variable>): AppResult<Unit> {
        val duplicateKey = variables.groupBy { it.key }.entries.firstOrNull { it.value.size > 1 }?.key
        if (duplicateKey != null) {
            return AppResult.Failure(AppError.Validation("key", "Duplicate key: $duplicateKey"))
        }
        return repo.saveVariables(id, variables)
    }
}
