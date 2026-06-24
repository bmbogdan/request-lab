package com.example.requestlab.feature.history.ui

import app.cash.turbine.test
import com.example.requestlab.core.testing.MainDispatcherRule
import com.example.requestlab.core.testing.fake.FakeHistoryRepository
import com.example.requestlab.feature.history.domain.usecase.ClearHistoryUseCase
import com.example.requestlab.feature.history.domain.usecase.DeleteHistoryEntryUseCase
import com.example.requestlab.feature.history.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HistoryListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repo = FakeHistoryRepository()
    private fun viewModel() = HistoryListViewModel(
        observeHistory = ObserveHistoryUseCase(repo),
        deleteEntry = DeleteHistoryEntryUseCase(repo),
        clearHistory = ClearHistoryUseCase(repo),
    )

    @Test
    fun `starts loading then emits Empty when no entries`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            assertEquals(HistoryListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(HistoryListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Content when entries exist`() = runTest {
        val entries = listOf(FakeHistoryRepository.entry("e1"), FakeHistoryRepository.entry("e2"))
        repo.setEntries(entries)
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            advanceUntilIdle()
            val content = awaitItem() as HistoryListUiState.Content
            assertEquals(2, content.entries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDeleteEntry removes entry from list`() = runTest {
        repo.setEntries(listOf(FakeHistoryRepository.entry("e1"), FakeHistoryRepository.entry("e2")))
        val vm = viewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val initial = awaitItem() as HistoryListUiState.Content
            assertEquals(2, initial.entries.size)

            vm.onEvent(HistoryListEvent.OnDeleteEntry("e1"))
            advanceUntilIdle()
            val updated = awaitItem() as HistoryListUiState.Content
            assertEquals(1, updated.entries.size)
            assertEquals("e2", updated.entries.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRequestClearAll sets showClearConfirm true`() = runTest {
        repo.setEntries(listOf(FakeHistoryRepository.entry()))
        val vm = viewModel()
        advanceUntilIdle()

        vm.uiState.test {
            awaitItem() // initial content
            vm.onEvent(HistoryListEvent.OnRequestClearAll)
            val state = awaitItem() as HistoryListUiState.Content
            assertTrue(state.showClearConfirm)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnConfirmClearAll clears all entries and dismisses dialog`() = runTest {
        repo.setEntries(listOf(FakeHistoryRepository.entry()))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(HistoryListEvent.OnRequestClearAll)
        advanceUntilIdle()

        vm.uiState.test {
            awaitItem() // state with showClearConfirm=true
            vm.onEvent(HistoryListEvent.OnConfirmClearAll)
            advanceUntilIdle()
            // Two transitions: showClearConfirm=false (sync), then clearAll Flow update (async)
            val empty = expectMostRecentItem()
            assertTrue(empty is HistoryListUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDismissClearConfirm hides dialog`() = runTest {
        repo.setEntries(listOf(FakeHistoryRepository.entry()))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(HistoryListEvent.OnRequestClearAll)
        advanceUntilIdle()

        vm.uiState.test {
            awaitItem() // showClearConfirm=true
            vm.onEvent(HistoryListEvent.OnDismissClearConfirm)
            val state = awaitItem() as HistoryListUiState.Content
            assertFalse(state.showClearConfirm)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
