package com.akrubastudios.playquizgames.ui.screens.freemode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.UserLevelCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla del Modo Libre.
 *
 * @param isLoading Indica si se están cargando los datos.
 * @param masteredLevels La lista de niveles con 3 estrellas que ha completado el usuario.
 */
data class FreeModeState(
    val isLoading: Boolean = true,
    val masteredLevels: List<UserLevelCompletion> = emptyList()
)

@HiltViewModel
class FreeModeViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreeModeState())
    val uiState = _uiState.asStateFlow()

    // Estado para guardar la dificultad seleccionada.
    private val _selectedDifficulty = MutableStateFlow("principiante")
    val selectedDifficulty = _selectedDifficulty.asStateFlow()

    /**
     * Se llama desde la UI cuando el usuario cambia la selección de dificultad.
     */
    fun onDifficultyChange(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    init {
        loadMasteredLevels()
    }

    /**
     * Llama al repositorio para obtener la lista de niveles con 3 estrellas
     * y actualiza el estado de la UI con el resultado.
     */
    private fun loadMasteredLevels() {
        // Lanzamos una coroutine para ejecutar la llamada de red en segundo plano.
        viewModelScope.launch {
            // Ponemos el estado en 'cargando' antes de la llamada.
            _uiState.value = FreeModeState(isLoading = true)

            // Obtenemos los datos desde el repositorio.
            val levels = gameDataRepository.getMasteredLevels()

            // Actualizamos el estado con los datos cargados y desactivamos 'isLoading'.
            _uiState.value = FreeModeState(isLoading = false, masteredLevels = levels)
        }
    }
}