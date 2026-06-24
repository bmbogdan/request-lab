package com.example.requestlab.core.common.model

import java.time.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class FailureKind { TIMEOUT, NO_INTERNET, DNS, TLS, CANCELLED, UNKNOWN }

@Serializable
data class TransportFailure(
    val kind: FailureKind,
    val message: String,
)

@Serializable
data class HttpResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: List<KeyValue>,
    val body: String,
    val bodySizeBytes: Long,
    val latencyMs: Long,
    val isJson: Boolean,
)

data class SendOutcome(
    val request: PreparedRequest,
    val response: HttpResponse?,
    val failure: TransportFailure?,
    val sentAt: Instant,
) {
    init {
        require((response != null) xor (failure != null)) {
            "Exactly one of response or failure must be non-null"
        }
    }
}
