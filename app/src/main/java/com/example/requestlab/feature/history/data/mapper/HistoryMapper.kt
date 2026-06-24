package com.example.requestlab.feature.history.data.mapper

import com.example.requestlab.core.common.model.FailureKind
import com.example.requestlab.core.common.model.HttpResponse
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.core.common.model.TransportFailure
import com.example.requestlab.feature.history.data.entity.HistoryEntity
import com.example.requestlab.feature.history.domain.model.HistoryDetail
import com.example.requestlab.feature.history.domain.model.HistoryEntry
import com.example.requestlab.feature.history.domain.model.HistoryStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

private val json = Json { ignoreUnknownKeys = true }

fun HistoryEntity.toEntry(): HistoryEntry = HistoryEntry(
    id = id,
    method = method,
    resolvedUrl = resolvedUrl,
    status = when (statusType) {
        "HTTP" -> HistoryStatus.Http(statusCode ?: 0)
        else -> HistoryStatus.Failed(
            kind = failureKind?.let { runCatching { FailureKind.valueOf(it) }.getOrDefault(FailureKind.UNKNOWN) }
                ?: FailureKind.UNKNOWN,
            reason = failureReason ?: "",
        )
    },
    latencyMs = latencyMs,
    sentAt = Instant.ofEpochMilli(sentAtMillis),
    environmentName = environmentName,
)

fun HistoryEntity.toDetail(): HistoryDetail {
    val request = json.decodeFromString<PreparedRequest>(requestJson)
    val response = responseJson?.let { json.decodeFromString<HttpResponse>(it) }
    val localKind = failureKind
    val localReason = failureReason
    val failure = if (statusType == "FAILED" && localKind != null && localReason != null) {
        TransportFailure(
            kind = runCatching { FailureKind.valueOf(localKind) }.getOrDefault(FailureKind.UNKNOWN),
            message = localReason,
        )
    } else null
    return HistoryDetail(
        entry = toEntry(),
        request = request,
        response = response,
        failure = failure,
    )
}

fun SendOutcome.toEntity(id: String, environmentName: String?): HistoryEntity {
    val statusType = if (response != null) "HTTP" else "FAILED"
    return HistoryEntity(
        id = id,
        method = request.method.name,
        resolvedUrl = request.resolvedUrl,
        statusType = statusType,
        statusCode = response?.statusCode,
        failureKind = failure?.kind?.name,
        failureReason = failure?.message,
        latencyMs = response?.latencyMs,
        sentAtMillis = sentAt.toEpochMilli(),
        environmentName = environmentName,
        requestJson = json.encodeToString(request),
        responseJson = response?.let { json.encodeToString(it) },
    )
}
