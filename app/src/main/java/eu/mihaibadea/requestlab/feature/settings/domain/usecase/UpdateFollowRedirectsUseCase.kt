package eu.mihaibadea.requestlab.feature.settings.domain.usecase

import eu.mihaibadea.requestlab.core.common.AppResult
import eu.mihaibadea.requestlab.feature.settings.domain.SettingsRepository
import javax.inject.Inject

class UpdateFollowRedirectsUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): AppResult<Unit> = repo.setFollowRedirects(enabled)
}
