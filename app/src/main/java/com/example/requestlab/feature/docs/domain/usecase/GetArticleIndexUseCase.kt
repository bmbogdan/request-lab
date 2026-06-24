package com.example.requestlab.feature.docs.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.docs.domain.DocsRepository
import com.example.requestlab.feature.docs.domain.model.ArticleSummary
import javax.inject.Inject

class GetArticleIndexUseCase @Inject constructor(private val repository: DocsRepository) {
    suspend operator fun invoke(): AppResult<List<ArticleSummary>> = repository.getIndex()
}
