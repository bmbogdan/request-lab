package eu.mihaibadea.requestlab.feature.docs.domain.model

data class ArticleSummary(
    val id: String,
    val title: String,
    val category: DocCategory,
    val filename: String,
)
