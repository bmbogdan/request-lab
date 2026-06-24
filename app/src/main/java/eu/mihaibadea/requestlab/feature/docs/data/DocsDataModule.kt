package eu.mihaibadea.requestlab.feature.docs.data

import android.content.Context
import android.content.res.AssetManager
import eu.mihaibadea.requestlab.feature.docs.domain.DocsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DocsDataModule {

    @Binds
    @Singleton
    abstract fun bindDocsRepository(impl: DocsRepositoryImpl): DocsRepository

    companion object {
        @Provides
        fun provideAssetManager(@ApplicationContext context: Context): AssetManager =
            context.assets
    }
}
