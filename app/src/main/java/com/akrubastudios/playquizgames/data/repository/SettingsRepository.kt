package com.akrubastudios.playquizgames.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Se declara el DataStore como una extensión de Context.
// Esto asegura que solo haya una instancia de DataStore en toda la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class SettingsRepository @Inject constructor(
    // Hilt nos proveerá el contexto de la aplicación de forma segura.
    @ApplicationContext private val context: Context
) {
    // Clave interna para guardar y leer la preferencia del idioma.
    private companion object {
        val LANGUAGE_KEY = stringPreferencesKey("user_language")
    }

    /**
     * Expone un Flow que emite la preferencia de idioma guardada.
     * Si no hay ninguna preferencia guardada, emite el idioma actual del dispositivo como valor por defecto.
     * Si hay un error de lectura, emite un valor por defecto para no crashear la app.
     */
    val languagePreferenceFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            // Si ocurre un error de I/O (ej. disco lleno), emitimos preferencias vacías.
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Leemos el valor de nuestra clave. Si no existe (es null), usamos el idioma del sistema.
            preferences[LANGUAGE_KEY] ?: Locale.getDefault().language
        }

    /**
     * Guarda la preferencia de idioma seleccionada por el usuario en DataStore.
     * Esta es una función 'suspend' porque la escritura en disco es asíncrona.
     *
     * @param languageCode El código de idioma a guardar (ej. "es", "en").
     */
    suspend fun saveLanguagePreference(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}