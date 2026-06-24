package eu.mihaibadea.requestlab.feature.settings.data

import eu.mihaibadea.requestlab.feature.settings.domain.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDataModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepo(impl: SettingsRepositoryImpl): SettingsRepository
}
