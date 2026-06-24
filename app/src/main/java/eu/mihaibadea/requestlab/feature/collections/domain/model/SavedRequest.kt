package eu.mihaibadea.requestlab.feature.collections.domain.model

import eu.mihaibadea.requestlab.core.common.model.HttpMethod

data class SavedRequest(
    val id: String,
    val collectionId: String,
    val name: String,
    val method: HttpMethod,
    val url: String,
    val position: Int,
)
