package com.example.requestlab.feature.history.domain.model

import com.example.requestlab.core.common.model.HttpResponse
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.TransportFailure

data class HistoryDetail(
    val entry: HistoryEntry,
    val request: PreparedRequest,
    val response: HttpResponse?,
    val failure: TransportFailure?,
)
