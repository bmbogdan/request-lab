package eu.mihaibadea.requestlab.feature.environments.domain.usecase

import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveEnvironmentUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    operator fun invoke(): Flow<String?> = repo.observeActiveEnvironmentId()
}
