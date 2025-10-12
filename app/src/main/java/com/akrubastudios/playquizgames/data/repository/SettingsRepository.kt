package com.akrubastudios.playquizgames.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
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
        val MUSIC_VOLUME_KEY = floatPreferencesKey("music_volume")
        val SFX_ENABLED_KEY = booleanPreferencesKey("sfx_enabled")
        val SFX_VOLUME_KEY = floatPreferencesKey("sfx_volume")
        val OCEAN_QUALITY_KEY = stringPreferencesKey("ocean_quality")
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

    val musicVolumeFlow: Flow<Float> = dataStore.data
        .map { preferences ->
            // Por defecto, el volumen estará al máximo (1.0f)
            preferences[MUSIC_VOLUME_KEY] ?: 0.8f
        }

    val sfxEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // Los SFX también estarán activados por defecto
            preferences[SFX_ENABLED_KEY] ?: true
        }

    val sfxVolumeFlow: Flow<Float> = dataStore.data
        .map { preferences ->
            // El volumen de SFX también estará al máximo por defecto
            preferences[SFX_VOLUME_KEY] ?: 1.0f
        }
    // ... (funciones de guardado existentes)

    // V AÑADE ESTA NUEVA FUNCIÓN COMPLETA V
    suspend fun saveMusicVolume(volume: Float) {
        dataStore.edit { preferences ->
            // Aseguramos que el valor esté siempre entre 0.0 y 1.0
            preferences[MUSIC_VOLUME_KEY] = volume.coerceIn(0f, 1f)
        }
    }

    suspend fun saveSfxEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SFX_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun saveSfxVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[SFX_VOLUME_KEY] = volume.coerceIn(0f, 1f)
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

    val oceanQualityFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[OCEAN_QUALITY_KEY] ?: "HIGH" // Valor por defecto "HIGH"
        }
    suspend fun saveOceanQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[OCEAN_QUALITY_KEY] = quality
        }
    }
}