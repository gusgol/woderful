package me.goldhardt.woderful.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.goldhardt.woderful.data.local.UserPreferencesRepository.PreferencesKeys.COUNTER_INSTRUCTIONS_SHOWN
import javax.inject.Inject

/**
 * Repository for user preferences.
 */
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private object PreferencesKeys {
        val COUNTER_INSTRUCTIONS_SHOWN = booleanPreferencesKey("counter_instructions_shown")
    }

    suspend fun hasShownCounterInstructions(): Boolean =
        withContext(ioDispatcher) {
            dataStore.data.first().let { preferences ->
                preferences[COUNTER_INSTRUCTIONS_SHOWN] ?: false
            }
        }

    suspend fun setCounterInstructionsShown(shown: Boolean) {
        dataStore.edit { settings ->
            settings[COUNTER_INSTRUCTIONS_SHOWN] = shown
        }
    }
}
