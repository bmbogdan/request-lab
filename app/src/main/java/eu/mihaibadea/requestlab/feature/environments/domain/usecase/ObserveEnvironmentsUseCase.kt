package eu.mihaibadea.requestlab.feature.environments.domain.usecase

import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.model.Environment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEnvironmentsUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    operator fun invoke(): Flow<List<Environment>> = repo.observeEnvironments()
}
