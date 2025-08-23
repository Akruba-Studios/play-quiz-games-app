package com.akrubastudios.playquizgames.core

import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Un servicio singleton que centraliza la lógica de gestión del idioma de la aplicación.
 * Mantiene el estado del idioma actual y permite su modificación.
 */
@Singleton
class LanguageManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    // Creamos un CoroutineScope personalizado para este singleton.
    // SupervisorJob() asegura que si una corrutina hija falla, no cancele las demás.
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // StateFlow privado que contiene el idioma actual. Se inicializa con el idioma del sistema.
    private val _languageStateFlow = MutableStateFlow(Locale.getDefault().language)

    /**
     * StateFlow público e inmutable para que el resto de la app (ViewModels) lo observe.
     * Emitirá el nuevo código de idioma ("es" o "en") cada vez que cambie.
     */
    val languageStateFlow: StateFlow<String> = _languageStateFlow.asStateFlow()

    init {
        // En el momento en que se crea la instancia, comenzamos a escuchar las preferencias guardadas.
        scope.launch {
            settingsRepository.languagePreferenceFlow.collect { savedLanguage ->
                // Cada vez que el DataStore emite un valor (ya sea el guardado o el del sistema),
                // actualizamos nuestro StateFlow.
                _languageStateFlow.value = savedLanguage
            }
        }
    }

    /**
     * Función pública para cambiar el idioma de la aplicación.
     * Guarda la nueva preferencia, lo que automáticamente provocará que
     * el `languagePreferenceFlow` emita el nuevo valor y actualice `languageStateFlow`.
     *
     * @param languageCode El nuevo código de idioma a establecer (ej. "es", "en").
     */
    fun setLanguage(languageCode: String) {
        scope.launch {
            settingsRepository.saveLanguagePreference(languageCode)
        }
    }
}