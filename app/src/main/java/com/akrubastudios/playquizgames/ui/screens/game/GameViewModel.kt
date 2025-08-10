package com.akrubastudios.playquizgames.ui.screens.game

import androidx.lifecycle.ViewModel
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    // ESTADO INTERNO: Solo el ViewModel puede modificarlo.
    private val _uiState = MutableStateFlow(GameState())

    // ESTADO PÚBLICO: La UI lo observa, es de solo lectura.
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        loadLevel()
    }

    private fun loadLevel() {
        val level = repository.getLevel("logos_level_1")

        if (level != null) {
            // Actualizamos el estado con los datos cargados.
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    currentQuestion = level.questions.firstOrNull(), // Tomamos la primera pregunta
                    totalQuestions = level.questions.size
                )
            }
        } else {
            // En caso de error, también lo reflejamos en el estado.
            _uiState.update { it.copy(isLoading = false) } // Dejamos de cargar
        }
    }

    fun onLetterClick(letter: Char) {
        // Solo añadimos la letra si la respuesta del usuario aún no es tan larga como la correcta
        if (uiState.value.userAnswer.length < (uiState.value.currentQuestion?.correctAnswer?.length
                ?: 0)
        ) {
            _uiState.update { currentState ->
                currentState.copy(userAnswer = currentState.userAnswer + letter)
            }
        }
    }

}