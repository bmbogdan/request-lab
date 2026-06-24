package com.example.requestlab.feature.collections.data.mapper

import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.RequestBody
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import com.example.requestlab.feature.collections.data.dao.CollectionWithCountRow
import com.example.requestlab.feature.collections.data.entity.SavedRequestEntity
import com.example.requestlab.feature.collections.domain.model.Collection
import com.example.requestlab.feature.collections.domain.model.SavedRequest
import com.example.requestlab.feature.collections.domain.model.SavedRequestDetail
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun CollectionWithCountRow.toDomain() = Collection(
    id = id,
    name = name,
    requestCount = requestCount,
    position = position,
)

fun SavedRequestEntity.toSummary() = SavedRequest(
    id = id,
    collectionId = collectionId,
    name = name,
    method = runCatching { HttpMethod.valueOf(method) }.getOrDefault(HttpMethod.GET),
    url = url,
    position = position,
)

fun SavedRequestEntity.toDetail(): SavedRequestDetail = SavedRequestDetail(
    savedRequest = toSummary(),
    headers = json.decodeFromString(headersJson),
    params = json.decodeFromString(paramsJson),
    body = json.decodeFromString(bodyJson),
    auth = json.decodeFromString(authJson),
)

fun RequestDraft.toSavedRequestEntity(
    id: String,
    collectionId: String,
    name: String,
    position: Int,
) = SavedRequestEntity(
    id = id,
    collectionId = collectionId,
    name = name,
    method = method.name,
    url = url,
    position = position,
    headersJson = json.encodeToString<List<KeyValue>>(headers),
    paramsJson = json.encodeToString<List<KeyValue>>(params),
    bodyJson = json.encodeToString(body),
    authJson = json.encodeToString(auth),
)
