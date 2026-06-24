package com.example.requestlab.core.network

import com.example.requestlab.core.common.model.HttpResponse
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.TransportFailure

sealed interface RawHttpResult {
    data class Success(val response: HttpResponse) : RawHttpResult
    data class TransportError(val failure: TransportFailure) : RawHttpResult
}

interface HttpEngine {
    suspend fun execute(request: PreparedRequest): RawHttpResult
}
