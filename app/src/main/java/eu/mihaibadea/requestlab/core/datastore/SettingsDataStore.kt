package eu.mihaibadea.requestlab.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "requestlab_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TIMEOUT_SECONDS = intPreferencesKey("timeout_seconds")
        val FOLLOW_REDIRECTS = booleanPreferencesKey("follow_redirects")
        val ACTIVE_ENVIRONMENT_ID = stringPreferencesKey("active_environment_id")
    }

    val timeoutSeconds: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[Keys.TIMEOUT_SECONDS] ?: DEFAULT_TIMEOUT }

    val followRedirects: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[Keys.FOLLOW_REDIRECTS] ?: true }

    val activeEnvironmentId: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[Keys.ACTIVE_ENVIRONMENT_ID] }

    suspend fun setTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.TIMEOUT_SECONDS] = seconds }
    }

    suspend fun setFollowRedirects(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FOLLOW_REDIRECTS] = enabled }
    }

    suspend fun setActiveEnvironmentId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) prefs[Keys.ACTIVE_ENVIRONMENT_ID] = id
            else prefs.remove(Keys.ACTIVE_ENVIRONMENT_ID)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        const val DEFAULT_TIMEOUT = 30
    }
}
