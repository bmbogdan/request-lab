package com.example.requestlab.feature.settings.domain.usecase

import com.example.requestlab.core.common.AppError
import com.example.requestlab.core.common.AppResult
import com.example.requestlab.feature.settings.domain.SettingsRepository
import javax.inject.Inject

class UpdateTimeoutUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(seconds: Int): AppResult<Unit> {
        if (seconds !in 1..600) {
            return AppResult.Failure(AppError.Validation("timeout", "Timeout must be between 1 and 600 seconds"))
        }
        return repo.setTimeout(seconds)
    }
}
