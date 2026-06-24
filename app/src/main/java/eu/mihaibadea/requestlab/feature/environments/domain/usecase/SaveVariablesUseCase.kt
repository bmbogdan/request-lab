package eu.mihaibadea.requestlab.feature.environments.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.model.Variable
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
