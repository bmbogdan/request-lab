package com.example.requestlab.feature.builder.data.mapper

import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.RequestBody
import com.example.requestlab.core.crypto.SecretCipher
import com.example.requestlab.feature.builder.data.entity.WorkingDraftEntity
import com.example.requestlab.feature.builder.domain.model.RequestDraft
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun WorkingDraftEntity.toDomain(cipher: SecretCipher): RequestDraft {
    val auth = when (authType) {
        "BASIC" -> AuthConfig.Basic(
            username = authUsername ?: "",
            password = authSecretCipher?.let { runCatching { cipher.decrypt(it) }.getOrDefault("") } ?: "",
        )
        "BEARER" -> AuthConfig.Bearer(
            token = authSecretCipher?.let { runCatching { cipher.decrypt(it) }.getOrDefault("") } ?: "",
        )
        else -> AuthConfig.None
    }
    return RequestDraft(
        id = id,
        method = HttpMethod.valueOf(method),
        url = url,
        headers = json.decodeFromString<List<KeyValue>>(headersJson),
        params = json.decodeFromString<List<KeyValue>>(paramsJson),
        body = json.decodeFromString<RequestBody>(bodyJson),
        auth = auth,
        sourceSavedRequestId = sourceSavedRequestId,
        isDirty = isDirty,
    )
}

fun RequestDraft.toEntity(cipher: SecretCipher): WorkingDraftEntity {
    val (authType, authUsername, authSecretCipher) = when (val a = auth) {
        is AuthConfig.None -> Triple("NONE", null, null)
        is AuthConfig.Basic -> Triple("BASIC", a.username, runCatching { cipher.encrypt(a.password) }.getOrNull())
        is AuthConfig.Bearer -> Triple("BEARER", null, runCatching { cipher.encrypt(a.token) }.getOrNull())
    }
    return WorkingDraftEntity(
        id = id,
        method = method.name,
        url = url,
        headersJson = json.encodeToString(headers),
        paramsJson = json.encodeToString(params),
        bodyJson = json.encodeToString(body),
        authType = authType,
        authUsername = authUsername,
        authSecretCipher = authSecretCipher,
        sourceSavedRequestId = sourceSavedRequestId,
        isDirty = isDirty,
    )
}
