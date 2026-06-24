package eu.mihaibadea.requestlab.feature.collections.domain.model

import eu.mihaibadea.requestlab.core.common.model.AuthConfig
import eu.mihaibadea.requestlab.core.common.model.KeyValue
import eu.mihaibadea.requestlab.core.common.model.RequestBody

data class SavedRequestDetail(
    val savedRequest: SavedRequest,
    val headers: List<KeyValue>,
    val params: List<KeyValue>,
    val body: RequestBody,
    val auth: AuthConfig,
)
