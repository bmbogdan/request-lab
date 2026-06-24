package eu.mihaibadea.requestlab.feature.docs.data

import android.content.res.AssetManager
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.common.IoDispatcher
import eu.mihaibadea.requestlab.core.common.runCatchingToAppResult
import eu.mihaibadea.requestlab.feature.docs.domain.DocsRepository
import eu.mihaibadea.requestlab.feature.docs.domain.model.Article
import eu.mihaibadea.requestlab.feature.docs.domain.model.ArticleSummary
import eu.mihaibadea.requestlab.feature.docs.domain.model.DocCategory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class ArticleIndexEntry(
    val id: String,
    val title: String,
    val category: String,
    val filename: String,
)

@Singleton
class DocsRepositoryImpl @Inject constructor(
    private val assets: AssetManager,
    @IoDispatcher private val io: CoroutineDispatcher,
) : DocsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private var cachedIndex: List<ArticleSummary>? = null

    override suspend fun getIndex(): AppResult<List<ArticleSummary>> = withContext(io) {
        runCatchingToAppResult {
            cachedIndex ?: loadIndex().also { cachedIndex = it }
        }
    }

    override suspend fun getArticle(id: String): AppResult<Article> = withContext(io) {
        runCatchingToAppResult {
            val index = cachedIndex ?: loadIndex().also { cachedIndex = it }
            val summary = index.firstOrNull { it.id == id }
                ?: error("Article not found: $id")
            val markdown = assets.open("docs/${summary.filename}").bufferedReader().readText()
            Article(summary = summary, markdown = markdown)
        }
    }

    private fun loadIndex(): List<ArticleSummary> {
        val raw = assets.open("docs/index.json").bufferedReader().readText()
        return json.decodeFromString<List<ArticleIndexEntry>>(raw).map { entry ->
            ArticleSummary(
                id = entry.id,
                title = entry.title,
                category = runCatching { DocCategory.valueOf(entry.category) }
                    .getOrDefault(DocCategory.METHODS),
                filename = entry.filename,
            )
        }
    }
}
