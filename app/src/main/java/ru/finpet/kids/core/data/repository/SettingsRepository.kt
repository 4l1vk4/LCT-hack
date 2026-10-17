package ru.finpet.kids.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

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
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
        val VIRTUAL_DATE = stringPreferencesKey("virtual_date")
        val LAST_REAL_DATE = stringPreferencesKey("last_real_date")
        val PREORDER_ITEMS = stringSetPreferencesKey("preorder_items")
        val ADULT_STAGE_START_DAY = intPreferencesKey("adult_stage_start_day")
    }

    val adultStageStartDay: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ADULT_STAGE_START_DAY] ?: 0
    }

    suspend fun setAdultStageStartDay(day: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ADULT_STAGE_START_DAY] = day
        }
    }

    val preorderItemIds: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.PREORDER_ITEMS] ?: emptySet()
    }

    suspend fun setPreorderItemIds(ids: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREORDER_ITEMS] = ids
        }
    }

    suspend fun togglePreorderItem(itemId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.PREORDER_ITEMS] ?: emptySet()
            preferences[PreferencesKeys.PREORDER_ITEMS] = if (current.contains(itemId)) {
                current - itemId
            } else {
                current + itemId
            }
        }
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
        ru.finpet.kids.core.notification.NotificationPreferences.cachePetName(context, name)
    }

    val isTutorialCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.TUTORIAL_COMPLETED] ?: false
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.TUTORIAL_COMPLETED] = completed
        }
    }

    val virtualDate: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.VIRTUAL_DATE]
    }

    suspend fun getVirtualDateSync(): String? {
        return context.dataStore.data.map { it[PreferencesKeys.VIRTUAL_DATE] }.firstOrNull()
    }

    suspend fun setVirtualDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.VIRTUAL_DATE] = date
        }
    }

    val lastRealDate: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_REAL_DATE]
    }

    suspend fun getLastRealDateSync(): String? {
        return context.dataStore.data.map { it[PreferencesKeys.LAST_REAL_DATE] }.firstOrNull()
    }

    suspend fun setLastRealDate(date: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_REAL_DATE] = date
        }
    }

    suspend fun getVirtualLocalDateSync(): java.time.LocalDate? {
        val s = getVirtualDateSync() ?: return null
        return runCatching { java.time.LocalDate.parse(s) }.getOrNull()
    }

    suspend fun setVirtualDate(date: java.time.LocalDate) {
        setVirtualDate(date.toString())
    }

    suspend fun getLastRealLocalDateSync(): java.time.LocalDate? {
        val s = getLastRealDateSync() ?: return null
        return runCatching { java.time.LocalDate.parse(s) }.getOrNull()
    }

    suspend fun setLastRealDate(date: java.time.LocalDate) {
        setLastRealDate(date.toString())
    }
}