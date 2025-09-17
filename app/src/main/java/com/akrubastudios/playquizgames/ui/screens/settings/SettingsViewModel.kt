package com.akrubastudios.playquizgames.ui.screens.settings

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de Ajustes.
 */
data class SettingsState(
    val isMusicEnabled: Boolean = true,
    val areSfxEnabled: Boolean = true,
    val currentLanguageCode: String = "es", // Añadimos el idioma actual
    val musicVolume: Float = 1.0f
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // 1. Inyectamos nuestro LanguageManager. Hilt se encarga de todo.
    private val languageManager: LanguageManager,
    val musicManager: MusicManager, // <-- INYECTAR MUSIC MANAGER
    private val settingsRepository: SettingsRepository // <-- INYECTAR REPOSITORY
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Aseguramos que la música del mapa suene aquí también
        musicManager.play(MusicTrack.MAP)
    }

    init {
        // 2. Nos suscribimos al StateFlow del LanguageManager.
        viewModelScope.launch {
            languageManager.languageStateFlow.collect { langCode ->
                // Cada vez que el idioma cambie, actualizamos el estado de nuestra UI.
                _uiState.update { it.copy(currentLanguageCode = langCode) }
            }
        }
        // Observamos la preferencia de música guardada para que el Switch siempre esté actualizado.
        viewModelScope.launch {
            settingsRepository.musicPreferenceFlow.collect { isEnabled ->
                _uiState.update { it.copy(isMusicEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.musicVolumeFlow.collect { volume ->
                _uiState.update { it.copy(musicVolume = volume) }
            }
        }
    }

    /**
     * 3. Nueva función que será llamada desde la UI (el AlertDialog).
     * Simplemente delega la acción a nuestro gestor centralizado.
     */
    fun onLanguageSelected(languageCode: String) {
        Log.d("LanguageDebug", "[PASO 1] SettingsViewModel: onLanguageSelected llamado con '$languageCode'")
        languageManager.setLanguage(languageCode)
    }
    fun onMusicToggle(isEnabled: Boolean) {
        // Primero, le decimos al manager el nuevo estado general
        musicManager.setMusicEnabled(isEnabled)

        // AÑADIMOS ESTA LÓGICA:
        // Si el usuario acaba de ACTIVAR la música, le ordenamos explícitamente
        // que empiece a sonar la pista del mapa, ya que estamos en ese contexto.
        if (isEnabled) {
            musicManager.play(MusicTrack.MAP)
        }
    }
    /**
     * Se llama cada vez que el usuario desliza la barra de volumen.
     * Pasa el nuevo valor al MusicManager para aplicarlo en tiempo real.
     */
    fun onVolumeChange(volume: Float) {
        // Actualizamos el estado de la UI inmediatamente para una respuesta fluida del slider.
        _uiState.update { it.copy(musicVolume = volume) }
        // Le decimos al MusicManager que aplique el nuevo volumen.
        musicManager.setVolume(volume)
    }

    // El resto de funciones para música, etc., irían aquí en el futuro.
    // fun onMusicToggle(isEnabled: Boolean) { ... }
    // fun onSfxToggle(isEnabled: Boolean) { ... }
}