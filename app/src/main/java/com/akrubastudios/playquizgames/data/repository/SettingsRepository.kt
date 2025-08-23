package com.akrubastudios.playquizgames.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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

    suspend fun saveLanguagePreference(languageCode: String) {
        Log.d("LanguageDebug", "[PASO 3] SettingsRepository: Guardando '$languageCode' en DataStore.")
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}