package eu.mihaibadea.requestlab.feature.builder.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.getOrNull
import eu.mihaibadea.requestlab.core.common.model.AuthConfig
import eu.mihaibadea.requestlab.core.common.model.HttpMethod
import eu.mihaibadea.requestlab.core.common.model.MultipartPart
import eu.mihaibadea.requestlab.core.common.model.RequestBody
import eu.mihaibadea.requestlab.feature.builder.domain.model.CurlCommand
import eu.mihaibadea.requestlab.feature.builder.domain.model.RequestDraft
import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import eu.mihaibadea.requestlab.feature.environments.domain.VariableResolver
import javax.inject.Inject

class GenerateCurlUseCase @Inject constructor(
    private val environmentsRepository: EnvironmentsRepository,
    private val variableResolver: VariableResolver,
) {
    suspend operator fun invoke(
        draft: RequestDraft,
        activeEnvId: String?,
        includeCredentials: Boolean,
    ): AppResult<CurlCommand> {
        val variables: Map<String, String> = if (activeEnvId != null) {
            environmentsRepository.resolvedVariables(activeEnvId).getOrNull() ?: emptyMap()
        } else {
            emptyMap()
        }

        val unresolvedVars = mutableListOf<String>()
        val sb = StringBuilder("curl")

        if (draft.method != HttpMethod.GET) {
            sb.append(" -X ${draft.method.name}")
        }

        val urlResult = variableResolver.resolve(draft.url.ifBlank { "<url>" }, variables)
        unresolvedVars.addAll(urlResult.unresolved)

        val urlWithParams = if (draft.params.filter { it.enabled }.isNotEmpty()) {
            val paramsStr = draft.params.filter { it.enabled }.joinToString("&") { kv ->
                val kr = variableResolver.resolve(kv.key, variables)
                val vr = variableResolver.resolve(kv.value, variables)
                unresolvedVars.addAll(kr.unresolved)
                unresolvedVars.addAll(vr.unresolved)
                "${kr.text}=${vr.text}"
            }
            if (urlResult.text.contains("?")) "${urlResult.text}&$paramsStr"
            else "${urlResult.text}?$paramsStr"
        } else {
            urlResult.text
        }
        sb.append(" '$urlWithParams'")

        when (val auth = draft.auth) {
            is AuthConfig.Basic -> {
                val password = if (includeCredentials) auth.password else "[redacted]"
                sb.append(" -u '${auth.username}:$password'")
            }
            is AuthConfig.Bearer -> {
                val token = if (includeCredentials) auth.token else "[redacted]"
                sb.append(" -H 'Authorization: Bearer $token'")
            }
            is AuthConfig.None -> Unit
        }

        draft.headers.filter { it.enabled }.forEach { kv ->
            val kr = variableResolver.resolve(kv.key, variables)
            val vr = variableResolver.resolve(kv.value, variables)
            unresolvedVars.addAll(kr.unresolved)
            unresolvedVars.addAll(vr.unresolved)
            sb.append(" \\\n  -H '${kr.text}: ${vr.text}'")
        }

        when (val body = draft.body) {
            is RequestBody.None -> Unit
            is RequestBody.Json -> {
                val br = variableResolver.resolve(body.text, variables)
                unresolvedVars.addAll(br.unresolved)
                sb.append(" \\\n  -H 'Content-Type: application/json'")
                sb.append(" \\\n  -d '${br.text.replace("'", "\\'")}'")
            }
            is RequestBody.RawText -> {
                val br = variableResolver.resolve(body.text, variables)
                unresolvedVars.addAll(br.unresolved)
                if (body.contentType.isNotEmpty()) {
                    sb.append(" \\\n  -H 'Content-Type: ${body.contentType}'")
                }
                sb.append(" \\\n  -d '${br.text.replace("'", "\\'")}'")
            }
            is RequestBody.FormUrlEncoded -> {
                body.fields.filter { it.enabled }.forEach { kv ->
                    val kr = variableResolver.resolve(kv.key, variables)
                    val vr = variableResolver.resolve(kv.value, variables)
                    unresolvedVars.addAll(kr.unresolved)
                    unresolvedVars.addAll(vr.unresolved)
                    sb.append(" \\\n  --data-urlencode '${kr.text}=${vr.text}'")
                }
            }
            is RequestBody.Multipart -> {
                body.parts.forEach { part ->
                    when (part) {
                        is MultipartPart.Text -> sb.append(" \\\n  -F '${part.name}=${part.value}'")
                        is MultipartPart.FilePart -> sb.append(" \\\n  -F '${part.name}=@${part.file.displayName}'")
                    }
                }
            }
            is RequestBody.Binary -> {
                sb.append(" \\\n  --data-binary '@${body.file.displayName}'")
            }
        }

        return AppResult.Success(
            CurlCommand(
                text = sb.toString(),
                unresolvedVariables = unresolvedVars.distinct(),
            ),
        )
    }
}
