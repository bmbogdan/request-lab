package eu.mihaibadea.requestlab.feature.builder.domain.model

import eu.mihaibadea.requestlab.core.common.model.AuthConfig
import eu.mihaibadea.requestlab.core.common.model.HttpMethod
import eu.mihaibadea.requestlab.core.common.model.KeyValue
import eu.mihaibadea.requestlab.core.common.model.RequestBody

data class RequestDraft(
    val id: String,
    val method: HttpMethod,
    val url: String,
    val headers: List<KeyValue>,
    val params: List<KeyValue>,
    val body: RequestBody,
    val auth: AuthConfig,
    val sourceSavedRequestId: String?,
    val isDirty: Boolean,
)

sealed interface DraftSource {
    data object New : DraftSource
    data class FromHistory(val id: String) : DraftSource
    data class FromSavedRequest(val id: String) : DraftSource
    data class Duplicate(val historyOrSavedId: String) : DraftSource
}

fun emptyDraft(): RequestDraft = RequestDraft(
    id = "current",
    method = HttpMethod.GET,
    url = "",
    headers = emptyList(),
    params = emptyList(),
    body = RequestBody.None,
    auth = AuthConfig.None,
    sourceSavedRequestId = null,
    isDirty = false,
)
