package eu.mihaibadea.requestlab.feature.builder.domain

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.model.PreparedRequest
import eu.mihaibadea.requestlab.core.common.model.SendOutcome

interface SendRepository {
    suspend fun send(prepared: PreparedRequest): AppResult<SendOutcome>
}
