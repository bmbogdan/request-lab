package com.example.requestlab.feature.settings.data

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.IoDispatcher
import com.example.requestlab.core.common.runCatchingToAppResult
import com.example.requestlab.core.crypto.SecretCipher
import com.example.requestlab.core.datastore.SettingsDataStore
import com.example.requestlab.feature.builder.data.dao.WorkingDraftDao
import com.example.requestlab.feature.environments.data.dao.EnvironmentDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinated wipe of all local data. DAOs for history and collections are injected
 * incrementally as those features are built (Chunks 5 and 6).
 */
@Singleton
class AppResetCoordinator @Inject constructor(
    private val workingDraftDao: WorkingDraftDao,
    private val environmentDao: EnvironmentDao,
    private val dataStore: SettingsDataStore,
    private val cipher: SecretCipher,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    // Cleared in Chunk 5 when HistoryDao is available
    var clearHistory: (suspend () -> Unit)? = null

    // Cleared in Chunk 6 when CollectionDao + SavedRequestDao are available
    var clearCollections: (suspend () -> Unit)? = null

    suspend fun clearHistoryOnly(): AppResult<Unit> = runCatchingToAppResult {
        withContext(io) { clearHistory?.invoke() }
    }

    suspend fun resetAll(): AppResult<Unit> = runCatchingToAppResult {
        withContext(io) {
            clearHistory?.invoke()
            clearCollections?.invoke()
            workingDraftDao.clear()
            environmentDao.clearAll()       // FK CASCADE deletes all environment_variable rows
            dataStore.clearAll()
            cipher.rotateKey()
        }
    }
}
