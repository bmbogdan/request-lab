package eu.mihaibadea.requestlab.feature.settings.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.settings.domain.SettingsRepository
import javax.inject.Inject

class ResetAppUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repo.resetAll()
}
