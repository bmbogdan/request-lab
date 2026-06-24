package com.example.requestlab.feature.environments.domain

import com.example.requestlab.feature.environments.domain.model.ResolutionResult
import javax.inject.Inject
import javax.inject.Singleton

private val TOKEN_REGEX = Regex("""\{\{([^}]+)\}\}""")

@Singleton
class VariableResolver @Inject constructor() {

    fun resolve(text: String, variables: Map<String, String>): ResolutionResult {
        val unresolved = mutableListOf<String>()
        val resolved = TOKEN_REGEX.replace(text) { match ->
            val key = match.groupValues[1].trim()
            val value = variables[key]
            if (value == null) {
                unresolved += key
                match.value
            } else {
                value
            }
        }
        return ResolutionResult(text = resolved, unresolved = unresolved)
    }

    fun extractTokens(text: String): List<String> =
        TOKEN_REGEX.findAll(text).map { it.groupValues[1].trim() }.toList()
}
