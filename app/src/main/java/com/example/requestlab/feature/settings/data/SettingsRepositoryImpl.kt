package com.example.requestlab.feature.settings.data

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.core.common.runCatchingToAppResult
import com.example.requestlab.core.datastore.SettingsDataStore
import com.example.requestlab.feature.settings.domain.SettingsRepository
import com.example.requestlab.feature.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val coordinator: AppResetCoordinator,
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> =
        combine(dataStore.timeoutSeconds, dataStore.followRedirects) { timeout, redirects ->
            AppSettings(timeoutSeconds = timeout, followRedirects = redirects)
        }

    override suspend fun setTimeout(seconds: Int): AppResult<Unit> = runCatchingToAppResult {
        dataStore.setTimeoutSeconds(seconds)
    }

    override suspend fun setFollowRedirects(enabled: Boolean): AppResult<Unit> = runCatchingToAppResult {
        dataStore.setFollowRedirects(enabled)
    }

    override suspend fun resetAll(): AppResult<Unit> = coordinator.resetAll()
}
