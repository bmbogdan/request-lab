package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.model.PreparedRequest
import eu.mihaibadea.requestlab.core.common.model.SendOutcome
import eu.mihaibadea.requestlab.feature.builder.domain.SendRepository
import javax.inject.Inject

class SendRequestUseCase @Inject constructor(
    private val sendRepository: SendRepository,
) {
    suspend operator fun invoke(prepared: PreparedRequest): AppResult<SendOutcome> =
        sendRepository.send(prepared)
}
