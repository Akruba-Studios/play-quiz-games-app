package com.akrubastudios.playquizgames.ui.screens.freemode

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.UserLevelCompletion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.MusicTrack
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla del Modo Libre.
 *
 * @param isLoading Indica si se están cargando los datos.
 * @param masteredLevels La lista de niveles con 3 estrellas que ha completado el usuario.
 */
// Representa un nivel masterizado con su nombre ya localizado para la UI.
data class MasteredLevelUiState(
    val levelId: String,
    val levelName: String,
    val highScore: Int,
    val maxScore: Int
)

data class FreeModeState(
    val isLoading: Boolean = true,
    val masteredLevels: List<MasteredLevelUiState> = emptyList()
)

@HiltViewModel
class FreeModeViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val languageManager: LanguageManager,
    val musicManager: MusicManager
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(FreeModeState())
    val uiState = _uiState.asStateFlow()

    // Estado para guardar la dificultad seleccionada.
    private val _selectedDifficulty = MutableStateFlow("principiante")
    val selectedDifficulty = _selectedDifficulty.asStateFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        musicManager.play(MusicTrack.MAP)
    }

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Obtenemos los datos del repositorio una sola vez.
            val allMasteredLevels = gameDataRepository.getMasteredLevels()
            val levels = allMasteredLevels.filter { !it.levelId.contains("_boss_") }

            // Nos suscribimos a los cambios de idioma.
            languageManager.languageStateFlow.collect { langCode ->
                // Mapeamos los datos del dominio a nuestro nuevo UiState.
                val masteredLevelsForUi = levels.map { levelCompletion ->
                    MasteredLevelUiState(
                        levelId = levelCompletion.levelId,
                        // Obtenemos el nombre localizado aquí.
                        levelName = levelCompletion.levelName[langCode] ?: levelCompletion.levelName["es"] ?: levelCompletion.levelId,
                        highScore = levelCompletion.highScore,
                        maxScore = levelCompletion.maxScore
                    )
                }

                // Actualizamos el estado de la UI con la lista ya procesada.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        masteredLevels = masteredLevelsForUi
                    )
                }
            }
        }
    }
}