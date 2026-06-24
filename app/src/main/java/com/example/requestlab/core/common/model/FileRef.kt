package com.example.requestlab.core.common.model

import kotlinx.serialization.Serializable

@Serializable
data class FileRef(
    val uri: String,
    val displayName: String,
    val sizeBytes: Long?,
    val mimeType: String?,
)
