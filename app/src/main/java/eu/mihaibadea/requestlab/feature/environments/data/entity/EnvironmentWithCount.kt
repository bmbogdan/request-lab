package eu.mihaibadea.requestlab.feature.environments.data.entity

data class EnvironmentWithCount(
    val id: String,
    val name: String,
    val variableCount: Int,
)
