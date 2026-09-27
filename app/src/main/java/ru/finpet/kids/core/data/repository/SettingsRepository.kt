package ru.finpet.kids.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.stringPreferencesKey

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "finpet_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val SELECTED_SKIN = stringPreferencesKey("selected_skin")
        val PET_NAME = stringPreferencesKey("pet_name")
    }

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SOUND_ENABLED] ?: true
    }

    val isMusicEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MUSIC_ENABLED] ?: true
    }

    val musicVolume: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MUSIC_VOLUME] ?: 0.7f
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val isDemoMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEMO_MODE] ?: false
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setMusicVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_VOLUME] = volume
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEMO_MODE] = enabled
        }
    }

    val selectedSkin: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SELECTED_SKIN]
    }

    suspend fun setSelectedSkin(skinId: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_SKIN] = skinId
        }
    }

    val petName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.PET_NAME]
    }

    suspend fun setPetName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PET_NAME] = name
        }
    }
}