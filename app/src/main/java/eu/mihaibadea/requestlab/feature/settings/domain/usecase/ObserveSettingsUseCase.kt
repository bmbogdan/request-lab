package eu.mihaibadea.requestlab.feature.settings.domain.usecase

import eu.mihaibadea.requestlab.feature.settings.domain.SettingsRepository
import eu.mihaibadea.requestlab.feature.settings.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val repo: SettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = repo.observeSettings()
}
