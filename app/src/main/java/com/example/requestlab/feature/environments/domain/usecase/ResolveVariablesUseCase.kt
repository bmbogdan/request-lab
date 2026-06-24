package com.example.requestlab.feature.environments.domain.usecase

import com.example.requestlab.core.common.getOrNull
import com.example.requestlab.feature.environments.domain.EnvironmentsRepository
import com.example.requestlab.feature.environments.domain.VariableResolver
import com.example.requestlab.feature.environments.domain.model.ResolutionResult
import javax.inject.Inject

class ResolveVariablesUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
    private val resolver: VariableResolver,
) {
    suspend operator fun invoke(text: String, activeEnvId: String?): ResolutionResult {
        if (activeEnvId == null) {
            val unresolved = resolver.extractTokens(text)
            return ResolutionResult(text = text, unresolved = unresolved)
        }
        val variables = repo.resolvedVariables(activeEnvId).getOrNull() ?: emptyMap()
        return resolver.resolve(text, variables)
    }
}
