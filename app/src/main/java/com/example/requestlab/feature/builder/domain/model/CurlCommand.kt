package com.example.requestlab.feature.builder.domain.model

data class CurlCommand(
    val text: String,
    val unresolvedVariables: List<String>,
)
