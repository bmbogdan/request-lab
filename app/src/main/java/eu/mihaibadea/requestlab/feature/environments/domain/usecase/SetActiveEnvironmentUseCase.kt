package eu.mihaibadea.requestlab.feature.environments.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import javax.inject.Inject

class SetActiveEnvironmentUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    suspend operator fun invoke(id: String?): AppResult<Unit> = repo.setActive(id)
}
