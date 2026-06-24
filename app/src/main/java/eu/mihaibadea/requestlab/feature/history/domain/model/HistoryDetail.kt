package eu.mihaibadea.requestlab.feature.history.domain.model

import eu.mihaibadea.requestlab.core.common.model.HttpResponse
import eu.mihaibadea.requestlab.core.common.model.PreparedRequest
import eu.mihaibadea.requestlab.core.common.model.TransportFailure

data class HistoryDetail(
    val entry: HistoryEntry,
    val request: PreparedRequest,
    val response: HttpResponse?,
    val failure: TransportFailure?,
)
