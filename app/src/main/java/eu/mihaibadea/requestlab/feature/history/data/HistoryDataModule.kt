package eu.mihaibadea.requestlab.feature.history.data

import eu.mihaibadea.requestlab.feature.history.domain.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryDataModule {
    @Binds
    @Singleton
    abstract fun bindHistoryRepo(impl: HistoryRepositoryImpl): HistoryRepository
}
