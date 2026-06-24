package com.example.requestlab.feature.history.data

import androidx.room.Room
import app.cash.turbine.test
import org.robolectric.RuntimeEnvironment
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.model.FailureKind
import com.example.requestlab.core.common.model.HttpResponse
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.common.model.KeyValue
import com.example.requestlab.core.common.model.PreparedRequest
import com.example.requestlab.core.common.model.RequestBody
import com.example.requestlab.core.common.model.AuthConfig
import com.example.requestlab.core.common.model.RequestConfig
import com.example.requestlab.core.common.model.SendOutcome
import com.example.requestlab.core.common.model.TransportFailure
import com.example.requestlab.core.database.RequestLabDatabase
import com.example.requestlab.feature.history.domain.model.HistoryStatus
import com.example.requestlab.feature.settings.data.AppResetCoordinator
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class HistoryRepositoryImplTest {

    private lateinit var db: RequestLabDatabase
    private lateinit var repo: HistoryRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            RequestLabDatabase::class.java,
        ).allowMainThreadQueries().build()

        repo = HistoryRepositoryImpl(
            dao = db.historyDao(),
            coordinator = mockk(relaxed = true),
            io = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `record inserts entry and observeHistory emits it`() = runTest(testDispatcher) {
        val result = repo.record(successOutcome(), environmentName = null)
        assertTrue(result is AppResult.Success)

        repo.observeHistory().test {
            val entries = awaitItem()
            assertEquals(1, entries.size)
            assertEquals("GET", entries.first().method)
            assertEquals(HistoryStatus.Http(200), entries.first().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete removes specific entry, others remain`() = runTest(testDispatcher) {
        repo.record(successOutcome(), null)
        repo.record(successOutcome(), null)
        testDispatcher.scheduler.advanceUntilIdle()

        repo.observeHistory().test {
            val initial = awaitItem()
            assertEquals(2, initial.size)

            val idToDelete = initial.first().id
            repo.delete(idToDelete)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertTrue(updated.none { it.id == idToDelete })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearAll removes all entries`() = runTest(testDispatcher) {
        repo.record(successOutcome(), "Dev")
        repo.record(successOutcome(), null)
        testDispatcher.scheduler.advanceUntilIdle()

        repo.clearAll()
        testDispatcher.scheduler.advanceUntilIdle()

        repo.observeHistory().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure outcome stores Failed status with correct kind`() = runTest(testDispatcher) {
        repo.record(failureOutcome(), null)
        testDispatcher.scheduler.advanceUntilIdle()

        repo.observeHistory().test {
            val status = awaitItem().first().status
            assertTrue(status is HistoryStatus.Failed)
            assertEquals(FailureKind.TIMEOUT, (status as HistoryStatus.Failed).kind)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getDetail returns correct environment name and status`() = runTest(testDispatcher) {
        val result = repo.record(successOutcome(), "Staging") as AppResult.Success
        testDispatcher.scheduler.advanceUntilIdle()

        val detail = repo.getDetail(result.value) as AppResult.Success
        assertEquals("Staging", detail.value.entry.environmentName)
        assertEquals(200, (detail.value.entry.status as HistoryStatus.Http).code)
    }

    @Test
    fun `getDetail returns Failure for unknown id`() = runTest(testDispatcher) {
        val result = repo.getDetail("does-not-exist")
        assertTrue(result is AppResult.Failure)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private fun successOutcome() = SendOutcome(
        request = PreparedRequest(
            method = HttpMethod.GET,
            resolvedUrl = "https://api.example.com/users",
            headers = emptyList(),
            params = emptyList(),
            body = RequestBody.None,
            auth = AuthConfig.None,
            config = RequestConfig(timeoutSeconds = 30, followRedirects = true),
        ),
        response = HttpResponse(
            statusCode = 200,
            statusMessage = "OK",
            headers = listOf(KeyValue("Content-Type", "application/json")),
            body = """{"users":[]}""",
            bodySizeBytes = 12,
            latencyMs = 150,
            isJson = true,
        ),
        failure = null,
        sentAt = Instant.ofEpochMilli(1_718_000_000_000L),
    )

    private fun failureOutcome() = SendOutcome(
        request = PreparedRequest(
            method = HttpMethod.GET,
            resolvedUrl = "https://api.example.com/slow",
            headers = emptyList(),
            params = emptyList(),
            body = RequestBody.None,
            auth = AuthConfig.None,
            config = RequestConfig(timeoutSeconds = 30, followRedirects = true),
        ),
        response = null,
        failure = TransportFailure(kind = FailureKind.TIMEOUT, message = "timed out after 30s"),
        sentAt = Instant.ofEpochMilli(1_718_000_000_000L),
    )
}
