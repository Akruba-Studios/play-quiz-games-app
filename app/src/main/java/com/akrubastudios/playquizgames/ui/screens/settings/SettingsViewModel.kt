package com.akrubastudios.playquizgames.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de Ajustes.
 * Por ahora está vacío, pero preparado para futuras opciones.
 */
data class SettingsState(
    val isMusicEnabled: Boolean = true,
    val areSfxEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    // En el futuro, aquí irían funciones como:
    // fun onMusicToggle(isEnabled: Boolean) { ... }
    // fun onSfxToggle(isEnabled: Boolean) { ... }
}