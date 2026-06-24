package eu.mihaibadea.requestlab.feature.environments.domain.model

data class Environment(
    val id: String,
    val name: String,
    val isActive: Boolean,
    val variableCount: Int,
)
