package com.example.requestlab.feature.settings.domain

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setTimeout(seconds: Int): AppResult<Unit>
    suspend fun setFollowRedirects(enabled: Boolean): AppResult<Unit>
    suspend fun resetAll(): AppResult<Unit>
}
