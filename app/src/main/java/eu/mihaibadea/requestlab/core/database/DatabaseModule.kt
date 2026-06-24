package eu.mihaibadea.requestlab.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.mihaibadea.requestlab.feature.builder.data.dao.WorkingDraftDao
import eu.mihaibadea.requestlab.feature.collections.data.dao.CollectionDao
import eu.mihaibadea.requestlab.feature.collections.data.dao.SavedRequestDao
import eu.mihaibadea.requestlab.feature.environments.data.dao.EnvironmentDao
import eu.mihaibadea.requestlab.feature.environments.data.dao.EnvironmentVariableDao
import eu.mihaibadea.requestlab.feature.history.data.dao.HistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RequestLabDatabase =
        Room.databaseBuilder(context, RequestLabDatabase::class.java, "requestlab.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides @Singleton
    fun provideWorkingDraftDao(db: RequestLabDatabase): WorkingDraftDao = db.workingDraftDao()

    @Provides @Singleton
    fun provideEnvironmentDao(db: RequestLabDatabase): EnvironmentDao = db.environmentDao()

    @Provides @Singleton
    fun provideEnvironmentVariableDao(db: RequestLabDatabase): EnvironmentVariableDao = db.environmentVariableDao()

    @Provides @Singleton
    fun provideHistoryDao(db: RequestLabDatabase): HistoryDao = db.historyDao()

    @Provides @Singleton
    fun provideCollectionDao(db: RequestLabDatabase): CollectionDao = db.collectionDao()

    @Provides @Singleton
    fun provideSavedRequestDao(db: RequestLabDatabase): SavedRequestDao = db.savedRequestDao()
}
