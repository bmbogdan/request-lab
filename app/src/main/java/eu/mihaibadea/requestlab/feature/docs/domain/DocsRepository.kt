package eu.mihaibadea.requestlab.feature.docs.domain

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.docs.domain.model.Article
import eu.mihaibadea.requestlab.feature.docs.domain.model.ArticleSummary

interface DocsRepository {
    suspend fun getIndex(): AppResult<List<ArticleSummary>>
    suspend fun getArticle(id: String): AppResult<Article>
}
