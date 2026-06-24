package eu.mihaibadea.requestlab.feature.docs.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.docs.domain.DocsRepository
import eu.mihaibadea.requestlab.feature.docs.domain.model.ArticleSummary
import javax.inject.Inject

class GetArticleIndexUseCase @Inject constructor(private val repository: DocsRepository) {
    suspend operator fun invoke(): AppResult<List<ArticleSummary>> = repository.getIndex()
}
