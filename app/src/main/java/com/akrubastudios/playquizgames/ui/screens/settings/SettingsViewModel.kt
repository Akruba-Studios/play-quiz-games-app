package com.akrubastudios.playquizgames.ui.screens.settings

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.core.SoundManager
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import com.akrubastudios.playquizgames.performance.DevicePerformanceDetector
import com.akrubastudios.playquizgames.performance.OceanConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

/**
 * Estado de la UI para la pantalla de Ajustes.
 */
data class SettingsState(
    val isMusicEnabled: Boolean = true,
    val areSfxEnabled: Boolean = true,
    val sfxVolume: Float = 1.0f,
    val currentLanguageCode: String = "es", // Añadimos el idioma actual
    val musicVolume: Float = 1.0f,
    val currentQualityTier: DevicePerformanceDetector.DeviceTier? = null,
    val isAutoAdjustEnabled: Boolean = true,
    val debugInfo: String = "",
    val isOceanAnimationEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // 1. Inyectamos nuestro LanguageManager. Hilt se encarga de todo.
    private val languageManager: LanguageManager,
    val musicManager: MusicManager, // <-- INYECTAR MUSIC MANAGER
    private val settingsRepository: SettingsRepository, // <-- INYECTAR REPOSITORY
    private val soundManager: SoundManager,
    private val oceanConfigManager: OceanConfigManager
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Aseguramos que la música del mapa suene aquí también
        musicManager.play(MusicTrack.MAP)
    }

    init {
        // Observamos cada flujo por separado. Es más claro, más robusto
        // y evita los límites de la función 'combine'.

        viewModelScope.launch {
            languageManager.languageStateFlow.collect { lang ->
                _uiState.update { it.copy(currentLanguageCode = lang) }
            }
        }
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
        viewModelScope.launch {
            settingsRepository.sfxEnabledFlow.collect { isEnabled ->
                _uiState.update { it.copy(areSfxEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.sfxVolumeFlow.collect { volume ->
                _uiState.update { it.copy(sfxVolume = volume) }
            }
        }
        viewModelScope.launch {
            settingsRepository.userOverrideTierFlow.collect { userOverride ->
                _uiState.update { it.copy(currentQualityTier = userOverride) }
            }
        }
        viewModelScope.launch {
            settingsRepository.autoAdjustEnabledFlow.collect { isEnabled ->
                _uiState.update { it.copy(isAutoAdjustEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.oceanAnimationEnabledFlow.collect { isEnabled ->
                _uiState.update { it.copy(isOceanAnimationEnabled = isEnabled) }
            }
        }
        viewModelScope.launch {
            val stats = oceanConfigManager.getPerformanceStats()
            _uiState.update { it.copy(debugInfo = stats.deviceInfo) }
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
    fun onSfxToggle(isEnabled: Boolean) {
        soundManager.setSfxEnabled(isEnabled)
    }

    fun onSfxVolumeChange(volume: Float) {
        // Actualizamos la UI inmediatamente para que el slider sea fluido
        _uiState.update { it.copy(sfxVolume = volume) }
        soundManager.setSfxVolume(volume)
    }

    fun onQualityTierSelected(tier: DevicePerformanceDetector.DeviceTier) {
        viewModelScope.launch {
            // 1. Guarda la selección manual del usuario
            settingsRepository.saveUserOverrideTier(tier)
            // 2. Apaga el ajuste automático
            settingsRepository.saveAutoAdjustEnabled(false)
            settingsRepository.saveOceanAnimationEnabled(true)
        }
        // 3. Aplica la configuración inmediatamente
        oceanConfigManager.setUserOverrideConfig(tier)
        oceanConfigManager.setAutoAdjustEnabled(false)
    }

    fun onAutomaticQualitySelected() {
        viewModelScope.launch {
            // 1. Borra la selección manual
            settingsRepository.saveUserOverrideTier(null)
            // 2. Enciende el ajuste automático
            settingsRepository.saveAutoAdjustEnabled(true)
            settingsRepository.saveOceanAnimationEnabled(true)
        }
        // 3. Devuelve el control al sistema
        oceanConfigManager.clearUserOverride()
        oceanConfigManager.setAutoAdjustEnabled(true)
    }

    fun onAutoAdjustToggled(isEnabled: Boolean) {
        if (isEnabled) {
            viewModelScope.launch {
                settingsRepository.saveOceanAnimationEnabled(true)
            }
            // Si el usuario lo enciende, volvemos a modo automático
            onAutomaticQualitySelected()
        } else {
            // Si lo apaga, no hacemos nada (la selección manual ya lo apaga)
            // Solo guardamos el estado por si acaso.
            viewModelScope.launch {
                settingsRepository.saveAutoAdjustEnabled(false)
            }
            oceanConfigManager.setAutoAdjustEnabled(false)
        }
    }

    fun onForceRedetection() {
        oceanConfigManager.forceDeviceRedetection()
        // Podríamos querer refrescar la info de debug después de esto,
        // pero por ahora, solo ejecutamos la acción.
    }

    fun onOceanAnimationToggle(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveOceanAnimationEnabled(isEnabled)
            if (!isEnabled) {
                // Si se apaga el océano, también se apaga la calidad automática.
                settingsRepository.saveAutoAdjustEnabled(false)
                oceanConfigManager.setAutoAdjustEnabled(false) // Notificamos al manager
            }
        }
    }
}