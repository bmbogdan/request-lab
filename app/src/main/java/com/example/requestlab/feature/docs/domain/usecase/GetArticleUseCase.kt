package com.example.requestlab.feature.docs.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.docs.domain.DocsRepository
import com.example.requestlab.feature.docs.domain.model.Article
import javax.inject.Inject

class GetArticleUseCase @Inject constructor(private val repository: DocsRepository) {
    suspend operator fun invoke(id: String): AppResult<Article> = repository.getArticle(id)
}
