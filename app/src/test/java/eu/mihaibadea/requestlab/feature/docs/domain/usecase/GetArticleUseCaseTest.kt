package eu.mihaibadea.requestlab.feature.docs.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.testing.fake.FakeDocsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetArticleUseCaseTest {

    private val repo = FakeDocsRepository()
    private val useCase = GetArticleUseCase(repo)

    @Test
    fun `returns article from repository on success`() = runTest {
        val expected = FakeDocsRepository.defaultArticle()
        val result = useCase("http-methods")
        assertTrue(result is AppResult.Success)
        assertEquals(expected, (result as AppResult.Success).value)
    }

    @Test
    fun `propagates not-found failure`() = runTest {
        repo.articleResult = AppResult.Failure(AppError.NotFound)
        val result = useCase("unknown-id")
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }
}
