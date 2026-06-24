package eu.mihaibadea.requestlab.feature.environments.domain.model

data class ResolutionResult(
    val text: String,
    val unresolved: List<String>,
)
