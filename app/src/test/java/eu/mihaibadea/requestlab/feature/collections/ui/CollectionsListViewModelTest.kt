package eu.mihaibadea.requestlab.feature.collections.ui

import app.cash.turbine.test
import eu.mihaibadea.requestlab.core.testing.MainDispatcherRule
import eu.mihaibadea.requestlab.core.testing.fake.FakeCollectionsRepository
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.CreateCollectionUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.DeleteCollectionUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.ObserveCollectionsUseCase
import eu.mihaibadea.requestlab.feature.collections.domain.usecase.RenameCollectionUseCase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CollectionsListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repo = FakeCollectionsRepository()
    private fun viewModel() = CollectionsListViewModel(
        observeCollections = ObserveCollectionsUseCase(repo),
        createCollection = CreateCollectionUseCase(repo),
        renameCollection = RenameCollectionUseCase(repo),
        deleteCollection = DeleteCollectionUseCase(repo),
    )

    @Test
    fun `starts Loading then Empty with no collections`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            assertEquals(CollectionsListUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(CollectionsListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Content when collections exist`() = runTest {
        repo.setCollections(listOf(FakeCollectionsRepository.collection()))
        val vm = viewModel()
        vm.uiState.test {
            awaitItem() // Loading
            advanceUntilIdle()
            val content = awaitItem() as CollectionsListUiState.Content
            assertEquals(1, content.collections.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnRequestNewCollection shows dialog`() = runTest {
        repo.setCollections(listOf(FakeCollectionsRepository.collection()))
        val vm = viewModel()
        advanceUntilIdle()

        vm.uiState.test {
            awaitItem() // Content
            vm.onEvent(CollectionsListEvent.OnRequestNewCollection)
            val state = awaitItem() as CollectionsListUiState.Content
            assertTrue(state.dialog is CollectionsDialog.NewCollection)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnConfirmNewCollection creates collection and dismisses dialog`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()

        // Trigger new collection dialog from Empty state (goes via Content with empty list)
        vm.onEvent(CollectionsListEvent.OnRequestNewCollection)
        vm.onEvent(CollectionsListEvent.OnNewCollectionNameChanged("Test API"))

        vm.uiState.test {
            awaitItem() // current state with dialog

            vm.onEvent(CollectionsListEvent.OnConfirmNewCollection)
            advanceUntilIdle()

            // Two transitions: dismissDialog() (sync), then createCollection Flow update (async)
            val state = expectMostRecentItem() as CollectionsListUiState.Content
            assertNull(state.dialog)
            assertTrue(state.collections.any { it.name == "Test API" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDismissDialog clears dialog from state`() = runTest {
        repo.setCollections(listOf(FakeCollectionsRepository.collection()))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(CollectionsListEvent.OnRequestNewCollection)

        vm.uiState.test {
            awaitItem() // state with dialog
            vm.onEvent(CollectionsListEvent.OnDismissDialog)
            val state = awaitItem() as CollectionsListUiState.Content
            assertNull(state.dialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnConfirmDelete removes collection`() = runTest {
        val c = FakeCollectionsRepository.collection("c1", "To Delete")
        repo.setCollections(listOf(c))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(CollectionsListEvent.OnRequestDelete("c1", "To Delete"))

        vm.uiState.test {
            awaitItem() // state with DeleteConfirm dialog
            vm.onEvent(CollectionsListEvent.OnConfirmDelete)
            advanceUntilIdle()
            // Two transitions: dismissDialog() (sync), then deleteCollection Flow update (async)
            val state = expectMostRecentItem()
            assertTrue(state is CollectionsListUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
