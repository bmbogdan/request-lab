package com.example.requestlab.feature.collections.data

import com.example.requestlab.feature.collections.domain.CollectionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CollectionsDataModule {
    @Binds
    @Singleton
    abstract fun bindCollectionsRepo(impl: CollectionsRepositoryImpl): CollectionsRepository
}
