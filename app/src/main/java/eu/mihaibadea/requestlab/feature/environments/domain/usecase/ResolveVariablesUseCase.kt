package eu.mihaibadea.requestlab.feature.environments.domain.usecase

import eu.mihaibadea.requestlab.core.common.getOrNull
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.VariableResolver
import eu.mihaibadea.requestlab.feature.environments.domain.model.ResolutionResult
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
