package com.example.requestlab.feature.settings.domain.model

data class AppSettings(
    val timeoutSeconds: Int = 30,
    val followRedirects: Boolean = true,
)
