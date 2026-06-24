package eu.mihaibadea.requestlab.feature.environments.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.model.EnvironmentDetail
import javax.inject.Inject

class GetEnvironmentDetailUseCase @Inject constructor(
    private val repo: EnvironmentsRepository,
) {
    suspend operator fun invoke(id: String): AppResult<EnvironmentDetail> = repo.getDetail(id)
}
