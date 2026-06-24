package eu.mihaibadea.requestlab.feature.history.domain.model

import eu.mihaibadea.requestlab.core.common.model.FailureKind

sealed interface HistoryStatus {
    data class Http(val code: Int) : HistoryStatus
    data class Failed(val kind: FailureKind, val reason: String) : HistoryStatus
}
