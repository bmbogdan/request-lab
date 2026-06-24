package com.example.requestlab.feature.environments.domain.model

data class EnvironmentDetail(
    val id: String,
    val name: String,
    val variables: List<Variable>,
)
