package eu.mihaibadea.requestlab.core.common.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyValue(
    val key: String,
    val value: String,
    val enabled: Boolean = true,
)
