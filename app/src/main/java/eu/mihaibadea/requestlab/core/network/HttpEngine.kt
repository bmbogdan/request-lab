package eu.mihaibadea.requestlab.core.network

import eu.mihaibadea.requestlab.core.common.model.HttpResponse
import eu.mihaibadea.requestlab.core.common.model.PreparedRequest
import eu.mihaibadea.requestlab.core.common.model.TransportFailure

sealed interface RawHttpResult {
    data class Success(val response: HttpResponse) : RawHttpResult
    data class TransportError(val failure: TransportFailure) : RawHttpResult
}

interface HttpEngine {
    suspend fun execute(request: PreparedRequest): RawHttpResult
}
