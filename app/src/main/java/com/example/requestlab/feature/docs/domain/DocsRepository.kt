package com.example.requestlab.feature.docs.domain

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.docs.domain.model.Article
import com.example.requestlab.feature.docs.domain.model.ArticleSummary

interface DocsRepository {
    suspend fun getIndex(): AppResult<List<ArticleSummary>>
    suspend fun getArticle(id: String): AppResult<Article>
}
