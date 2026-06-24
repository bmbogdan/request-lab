package com.example.requestlab.feature.collections.domain.model

data class Collection(
    val id: String,
    val name: String,
    val requestCount: Int,
    val position: Int,
)
