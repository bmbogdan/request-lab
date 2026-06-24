package com.example.requestlab.core.common.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthConfig {
    @Serializable data object None : AuthConfig
    @Serializable data class Basic(val username: String, val password: String) : AuthConfig
    @Serializable data class Bearer(val token: String) : AuthConfig
}
