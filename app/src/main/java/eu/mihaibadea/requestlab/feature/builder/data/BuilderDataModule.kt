package eu.mihaibadea.requestlab.feature.builder.data

import eu.mihaibadea.requestlab.feature.builder.domain.RequestDraftRepository
import eu.mihaibadea.requestlab.feature.builder.domain.SendRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BuilderDataModule {
    @Binds @Singleton
    abstract fun bindDraftRepo(impl: RequestDraftRepositoryImpl): RequestDraftRepository

    @Binds @Singleton
    abstract fun bindSendRepo(impl: SendRepositoryImpl): SendRepository
}
