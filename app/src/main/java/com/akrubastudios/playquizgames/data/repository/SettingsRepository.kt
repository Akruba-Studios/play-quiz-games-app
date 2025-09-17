package com.akrubastudios.playquizgames.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// La extensión de Context ya no está aquí, se ha movido a AppModule.

@Singleton
class SettingsRepository @Inject constructor(
    // Ahora recibe la instancia de DataStore directamente desde Hilt.
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val LANGUAGE_KEY = stringPreferencesKey("user_language")
        val DISMISSED_EXPEDITION_KEY = intPreferencesKey("dismissed_expedition_level")
        val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
    }

    val languagePreferenceFlow: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: Locale.getDefault().language
        }

    val musicPreferenceFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // Por defecto, la música estará activada la primera vez que se use la app
            preferences[MUSIC_ENABLED_KEY] ?: true
        }

    val dismissedExpeditionLevelFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[DISMISSED_EXPEDITION_KEY] ?: 0
        }

    suspend fun saveMusicPreference(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = isEnabled
        }
    }

    // Nueva función para guardar el nivel de expedición que se ignora.
    suspend fun saveDismissedExpeditionLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[DISMISSED_EXPEDITION_KEY] = level
        }
    }
    suspend fun saveLanguagePreference(languageCode: String) {
        Log.d("LanguageDebug", "[PASO 3] SettingsRepository: Guardando '$languageCode' en DataStore.")
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}