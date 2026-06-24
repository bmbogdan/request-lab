package com.example.requestlab.feature.history.domain.model

import java.time.Instant

data class HistoryEntry(
    val id: String,
    val method: String,
    val resolvedUrl: String,
    val status: HistoryStatus,
    val latencyMs: Long?,
    val sentAt: Instant,
    val environmentName: String?,
)
