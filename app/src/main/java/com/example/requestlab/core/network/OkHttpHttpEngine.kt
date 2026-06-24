package com.example.requestlab.core.network

import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.FailureKind
import com.example.requestlab.core.common.model.HttpResponse
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.MultipartPart
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.RequestBody
import com.example.requestlab.core.common.model.TransportFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException

@Singleton
class OkHttpHttpEngine @Inject constructor() : HttpEngine {

    override suspend fun execute(request: PreparedRequest): RawHttpResult =
        withContext(Dispatchers.IO) {
            val client = buildClient(request)
            val okRequest = buildRequest(request)
            val startMs = System.currentTimeMillis()
            try {
                client.newCall(okRequest).execute().use { response ->
                    val latencyMs = System.currentTimeMillis() - startMs
                    val rawBody = response.body?.string() ?: ""
                    val isJson = response.body?.contentType()?.let {
                        it.type == "application" && it.subtype.contains("json")
                    } ?: rawBody.trimStart().let { it.startsWith("{") || it.startsWith("[") }
                    val responseHeaders = response.headers.map { (name, value) ->
                        KeyValue(key = name, value = value)
                    }
                    RawHttpResult.Success(
                        HttpResponse(
                            statusCode = response.code,
                            statusMessage = response.message,
                            headers = responseHeaders,
                            body = rawBody,
                            bodySizeBytes = rawBody.toByteArray(Charsets.UTF_8).size.toLong(),
                            latencyMs = latencyMs,
                            isJson = isJson,
                        ),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SocketTimeoutException) {
                RawHttpResult.TransportError(
                    TransportFailure(FailureKind.TIMEOUT, "Timeout after ${request.config.timeoutSeconds} s"),
                )
            } catch (e: UnknownHostException) {
                RawHttpResult.TransportError(
                    TransportFailure(FailureKind.DNS, "Couldn't resolve host: ${e.message}"),
                )
            } catch (e: SSLException) {
                RawHttpResult.TransportError(
                    TransportFailure(FailureKind.TLS, "TLS handshake failed: ${e.message}"),
                )
            } catch (e: Exception) {
                RawHttpResult.TransportError(
                    TransportFailure(FailureKind.UNKNOWN, e.message ?: "Unknown error"),
                )
            }
        }

    private fun buildClient(request: PreparedRequest): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(request.config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(request.config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(request.config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .followRedirects(request.config.followRedirects)
            .followSslRedirects(request.config.followRedirects)
            .build()

    private fun buildRequest(request: PreparedRequest): Request {
        val urlWithParams = buildUrl(request)
        val okBody = buildBody(request.body)

        val builder = Request.Builder().url(urlWithParams)

        // Apply auth
        when (val auth = request.auth) {
            is AuthConfig.Basic -> builder.header("Authorization", Credentials.basic(auth.username, auth.password))
            is AuthConfig.Bearer -> builder.header("Authorization", "Bearer ${auth.token}")
            AuthConfig.None -> Unit
        }

        // Apply headers (may override auth if user explicitly sets Authorization)
        request.headers.filter { it.enabled && it.key.isNotBlank() }.forEach { builder.header(it.key, it.value) }

        builder.method(request.method.name, okBody)
        return builder.build()
    }

    private fun buildUrl(request: PreparedRequest): String {
        if (request.params.isEmpty()) return request.resolvedUrl
        val params = request.params.filter { it.enabled }.joinToString("&") {
            "${it.key.encodeUrl()}=${it.value.encodeUrl()}"
        }
        return if (request.resolvedUrl.contains("?")) "${request.resolvedUrl}&$params"
        else "${request.resolvedUrl}?$params"
    }

    private fun String.encodeUrl() = java.net.URLEncoder.encode(this, "UTF-8")

    private fun buildBody(body: RequestBody): okhttp3.RequestBody? = when (body) {
        is RequestBody.None -> null
        is RequestBody.Json -> body.text.toRequestBody("application/json; charset=utf-8".toMediaType())
        is RequestBody.RawText -> body.text.toRequestBody(body.contentType.toMediaTypeOrNull())
        is RequestBody.FormUrlEncoded -> FormBody.Builder().apply {
            body.fields.filter { it.enabled }.forEach { add(it.key, it.value) }
        }.build()
        is RequestBody.Multipart -> MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            body.parts.forEach { part ->
                when (part) {
                    is MultipartPart.Text -> addFormDataPart(part.name, part.value)
                    is MultipartPart.FilePart -> {
                        val file = File(android.net.Uri.parse(part.file.uri).path ?: return@forEach)
                        if (file.exists()) {
                            val mt = (part.file.mimeType ?: "application/octet-stream").toMediaTypeOrNull()
                            addFormDataPart(part.name, part.file.displayName, file.asRequestBody(mt))
                        }
                    }
                }
            }
        }.build()
        is RequestBody.Binary -> {
            val file = File(android.net.Uri.parse(body.file.uri).path ?: return null)
            if (!file.exists()) return null
            val mt = (body.file.mimeType ?: "application/octet-stream").toMediaTypeOrNull()
            file.asRequestBody(mt)
        }
    }
}
