package com.example.requestlab.core.testing.fake

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.docs.domain.DocsRepository
import com.example.requestlab.feature.docs.domain.model.Article
import com.example.requestlab.feature.docs.domain.model.ArticleSummary
import com.example.requestlab.feature.docs.domain.model.DocCategory

class FakeDocsRepository : DocsRepository {

    var indexResult: AppResult<List<ArticleSummary>> = AppResult.Success(defaultIndex())
    var articleResult: AppResult<Article> = AppResult.Success(defaultArticle())

    override suspend fun getIndex(): AppResult<List<ArticleSummary>> = indexResult

    override suspend fun getArticle(id: String): AppResult<Article> = articleResult

    companion object {
        fun defaultSummary(id: String = "http-methods") = ArticleSummary(
            id = id,
            title = "HTTP Methods",
            category = DocCategory.METHODS,
            filename = "http-methods.md",
        )

        fun defaultIndex() = listOf(
            defaultSummary("http-methods"),
            defaultSummary("auth-basic").copy(title = "Basic Auth", category = DocCategory.AUTH),
        )

        fun defaultArticle(id: String = "http-methods") = Article(
            summary = defaultSummary(id),
            markdown = "# HTTP Methods\n\nGET retrieves a resource.",
        )
    }
}
