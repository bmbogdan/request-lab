package com.example.requestlab.feature.docs.domain.usecase

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.testing.fake.FakeDocsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetArticleIndexUseCaseTest {

    private val repo = FakeDocsRepository()
    private val useCase = GetArticleIndexUseCase(repo)

    @Test
    fun `returns index from repository on success`() = runTest {
        val result = useCase()
        assertTrue(result is AppResult.Success)
        assertEquals(FakeDocsRepository.defaultIndex(), (result as AppResult.Success).value)
    }

    @Test
    fun `propagates failure from repository`() = runTest {
        repo.indexResult = AppResult.Failure(AppError.Storage(RuntimeException("disk error")))
        val result = useCase()
        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun `returns empty list when repository returns empty`() = runTest {
        repo.indexResult = AppResult.Success(emptyList())
        val result = useCase()
        assertEquals(emptyList<Any>(), (result as AppResult.Success).value)
    }
}
