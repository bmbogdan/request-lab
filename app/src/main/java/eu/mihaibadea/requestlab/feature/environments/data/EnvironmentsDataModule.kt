package eu.mihaibadea.requestlab.feature.environments.data

import eu.mihaibadea.requestlab.feature.environments.domain.EnvironmentsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EnvironmentsDataModule {
    @Binds
    @Singleton
    abstract fun bindEnvironmentsRepo(impl: EnvironmentsRepositoryImpl): EnvironmentsRepository
}
