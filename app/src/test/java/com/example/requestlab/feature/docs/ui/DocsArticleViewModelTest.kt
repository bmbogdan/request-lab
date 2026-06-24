package com.example.requestlab.feature.docs.ui

import app.cash.turbine.test
import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.testing.MainDispatcherRule
import com.example.requestlab.core.testing.fake.FakeDocsRepository
import com.example.requestlab.feature.docs.domain.usecase.GetArticleUseCase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DocsArticleViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repo = FakeDocsRepository()
    private fun viewModel() = DocsArticleViewModel(GetArticleUseCase(repo))

    @Test
    fun `starts loading then emits content after init`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            assertEquals(DocsArticleUiState.Loading, awaitItem())
            vm.init("http-methods")
            advanceUntilIdle()
            val content = awaitItem() as DocsArticleUiState.Content
            assertEquals("HTTP Methods", content.article.summary.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error when repository fails`() = runTest {
        repo.articleResult = AppResult.Failure(AppError.NotFound)
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            vm.init("unknown")
            advanceUntilIdle()
            assertTrue(awaitItem() is DocsArticleUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `second init with same id is a no-op`() = runTest {
        val vm = viewModel()
        vm.init("http-methods")
        advanceUntilIdle()
        val state1 = vm.uiState.value

        vm.init("http-methods")
        advanceUntilIdle()
        val state2 = vm.uiState.value

        assertEquals(state1, state2)
    }
}
