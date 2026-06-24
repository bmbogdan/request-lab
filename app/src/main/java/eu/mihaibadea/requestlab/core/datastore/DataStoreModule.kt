package eu.mihaibadea.requestlab.core.datastore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// SettingsDataStore is @Singleton via @Inject constructor; this module exists for potential
// future bindings (e.g. Proto DataStore migration).
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule
