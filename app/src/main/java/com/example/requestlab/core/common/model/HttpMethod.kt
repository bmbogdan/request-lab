package com.example.requestlab.core.common.model

import kotlinx.serialization.Serializable

@Serializable
enum class HttpMethod { GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS }
