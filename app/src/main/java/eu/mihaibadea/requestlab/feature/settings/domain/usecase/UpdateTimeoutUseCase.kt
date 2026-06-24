package eu.mihaibadea.requestlab.feature.settings.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppError
import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.settings.domain.SettingsRepository
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
