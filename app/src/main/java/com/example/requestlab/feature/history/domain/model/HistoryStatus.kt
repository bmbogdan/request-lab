package com.example.requestlab.feature.history.domain.model

import com.example.requestlab.core.common.model.FailureKind

sealed interface HistoryStatus {
    data class Http(val code: Int) : HistoryStatus
    data class Failed(val kind: FailureKind, val reason: String) : HistoryStatus
}
