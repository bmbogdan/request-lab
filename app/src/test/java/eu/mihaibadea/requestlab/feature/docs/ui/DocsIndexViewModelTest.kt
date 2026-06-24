package eu.mihaibadea.requestlab.feature.docs.ui

import app.cash.turbine.test
import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.core.testing.MainDispatcherRule
import eu.mihaibadea.requestlab.core.testing.fake.FakeDocsRepository
import eu.mihaibadea.requestlab.feature.docs.domain.model.DocCategory
import eu.mihaibadea.requestlab.feature.docs.domain.usecase.GetArticleIndexUseCase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DocsIndexViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repo = FakeDocsRepository()
    private fun viewModel() = DocsIndexViewModel(GetArticleIndexUseCase(repo))

    @Test
    fun `starts loading then emits grouped content`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            advanceUntilIdle()
            val content = awaitItem()
            assertTrue(!content.isLoading)
            assertNull(content.error)
            assertTrue(content.grouped.containsKey(DocCategory.METHODS))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows error when repository fails`() = runTest {
        repo.indexResult = AppResult.Failure(AppError.Storage(RuntimeException()))
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // loading
            advanceUntilIdle()
            val error = awaitItem()
            assertNotNull(error.error)
            assertTrue(error.grouped.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtered returns only matching articles`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(DocsIndexEvent.OnQueryChanged("HTTP"))
        val state = vm.uiState.value
        val allTitles = state.filtered.values.flatten().map { it.title }
        assertTrue(allTitles.all { it.contains("HTTP", ignoreCase = true) })
    }

    @Test
    fun `filtered returns empty map when query matches nothing`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(DocsIndexEvent.OnQueryChanged("xyzzy_nonexistent"))
        assertTrue(vm.uiState.value.filtered.isEmpty())
    }

    @Test
    fun `retry loads index again after failure`() = runTest {
        repo.indexResult = AppResult.Failure(AppError.Storage(RuntimeException()))
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.error != null)

        repo.indexResult = AppResult.Success(FakeDocsRepository.defaultIndex())
        vm.onEvent(DocsIndexEvent.OnRetry)
        advanceUntilIdle()
        assertNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.grouped.isNotEmpty())
    }
}
