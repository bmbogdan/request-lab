package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.getOrNull
import eu.mihaibadea.requestlab.core.common.model.AuthConfig
import eu.mihaibadea.requestlab.core.common.model.KeyValue
import eu.mihaibadea.requestlab.core.common.model.MultipartPart
import eu.mihaibadea.requestlab.core.common.model.PreparedRequest
import eu.mihaibadea.requestlab.core.common.model.RequestBody
import eu.mihaibadea.requestlab.core.common.model.RequestConfig
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.VariableResolver
import javax.inject.Inject

class ResolveDraftUseCase @Inject constructor(
    private val environmentsRepository: EnvironmentsRepository,
    private val variableResolver: VariableResolver,
) {
    suspend operator fun invoke(
        draft: RequestDraft,
        activeEnvId: String?,
    ): AppResult<PreparedRequest> {
        if (draft.url.isBlank()) {
            return AppResult.Failure(AppError.Validation("url", "URL cannot be empty"))
        }

        val variables: Map<String, String> = if (activeEnvId != null) {
            environmentsRepository.resolvedVariables(activeEnvId).getOrNull() ?: emptyMap()
        } else {
            emptyMap()
        }

        val urlResult = variableResolver.resolve(draft.url, variables)
        if (urlResult.unresolved.isNotEmpty()) {
            val token = urlResult.unresolved.first()
            return AppResult.Failure(
                AppError.Validation("url", "{{$token}} has no value in the active environment"),
            )
        }

        val resolvedHeaders = draft.headers.map { kv ->
            KeyValue(
                key = variableResolver.resolve(kv.key, variables).text,
                value = variableResolver.resolve(kv.value, variables).text,
                enabled = kv.enabled,
            )
        }

        val resolvedParams = draft.params.map { kv ->
            KeyValue(
                key = variableResolver.resolve(kv.key, variables).text,
                value = variableResolver.resolve(kv.value, variables).text,
                enabled = kv.enabled,
            )
        }

        val resolvedBody = resolveBody(draft.body, variables)

        val resolvedAuth = when (val auth = draft.auth) {
            is AuthConfig.None -> auth
            is AuthConfig.Basic -> AuthConfig.Basic(
                username = variableResolver.resolve(auth.username, variables).text,
                password = variableResolver.resolve(auth.password, variables).text,
            )
            is AuthConfig.Bearer -> AuthConfig.Bearer(
                token = variableResolver.resolve(auth.token, variables).text,
            )
        }

        return AppResult.Success(
            PreparedRequest(
                method = draft.method,
                resolvedUrl = urlResult.text,
                headers = resolvedHeaders,
                params = resolvedParams,
                body = resolvedBody,
                auth = resolvedAuth,
                config = RequestConfig(timeoutSeconds = 30, followRedirects = true),
            ),
        )
    }

    private fun resolveBody(body: RequestBody, variables: Map<String, String>): RequestBody =
        when (body) {
            is RequestBody.None -> body
            is RequestBody.Json -> body.copy(text = variableResolver.resolve(body.text, variables).text)
            is RequestBody.RawText -> body.copy(text = variableResolver.resolve(body.text, variables).text)
            is RequestBody.FormUrlEncoded -> body.copy(
                fields = body.fields.map { kv ->
                    KeyValue(
                        key = variableResolver.resolve(kv.key, variables).text,
                        value = variableResolver.resolve(kv.value, variables).text,
                        enabled = kv.enabled,
                    )
                },
            )
            is RequestBody.Multipart -> body.copy(
                parts = body.parts.map { part ->
                    when (part) {
                        is MultipartPart.Text -> part.copy(
                            value = variableResolver.resolve(part.value, variables).text,
                        )
                        is MultipartPart.FilePart -> part
                    }
                },
            )
            is RequestBody.Binary -> body
        }
}
