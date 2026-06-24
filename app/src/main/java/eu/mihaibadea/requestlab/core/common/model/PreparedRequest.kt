package eu.mihaibadea.requestlab.core.common.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestConfig(
    val timeoutSeconds: Int,
    val followRedirects: Boolean,
)

@Serializable
data class PreparedRequest(
    val method: HttpMethod,
    val resolvedUrl: String,
    val headers: List<KeyValue>,
    val params: List<KeyValue>,
    val body: RequestBody,
    val auth: AuthConfig,
    val config: RequestConfig,
)
