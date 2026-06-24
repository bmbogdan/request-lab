package com.example.requestlab.feature.settings.domain.usecase

import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.settings.domain.SettingsRepository
import javax.inject.Inject

class ResetAppUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repo.resetAll()
}
