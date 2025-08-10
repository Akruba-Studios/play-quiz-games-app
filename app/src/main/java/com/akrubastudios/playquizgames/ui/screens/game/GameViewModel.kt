package com.akrubastudios.playquizgames.ui.screens.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    // --- CORRECCIÓN: DECLARAMOS LAS VARIABLES AQUÍ ---
    private var levelPackage: QuizLevelPackage? = null
    private var currentQuestionIndex = 0
    // ---------------------------------------------

    init {
        loadLevel()
    }

    private fun loadLevel() {
        levelPackage = repository.getLevel("logos_level_1")

        if (levelPackage != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    currentQuestion = levelPackage!!.questions[currentQuestionIndex],
                    totalQuestions = levelPackage!!.questions.size,
                    questionNumber = currentQuestionIndex + 1
                )
            }
        } else {
            Log.d("GameViewModel", "Error al cargar el nivel.")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onLetterClick(letter: Char) {
        val currentAnswerLength = uiState.value.currentQuestion?.correctAnswer?.length ?: 0
        if (uiState.value.userAnswer.length < currentAnswerLength) {
            _uiState.update { currentState ->
                currentState.copy(userAnswer = currentState.userAnswer + letter)
            }

            if (uiState.value.userAnswer.length == currentAnswerLength) {
                checkAnswer()
            }
        }
    }

    private fun checkAnswer() {
        viewModelScope.launch {
            val state = uiState.value
            val isCorrect = state.currentQuestion?.validAnswers?.contains(state.userAnswer.lowercase()) == true

            if (isCorrect) {
                Log.d("GameViewModel", "¡Respuesta Correcta!")
                _uiState.update { it.copy(score = it.score + 1000) }
            } else {
                Log.d("GameViewModel", "Respuesta Incorrecta.")
            }

            delay(1000L)
            moveToNextQuestion()
        }
    }

    private fun moveToNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < (levelPackage?.questions?.size ?: 0)) {
            _uiState.update {
                it.copy(
                    currentQuestion = levelPackage!!.questions[currentQuestionIndex],
                    questionNumber = currentQuestionIndex + 1,
                    userAnswer = ""
                )
            }
        } else {
            Log.d("GameViewModel", "Juego Terminado. Puntaje final: ${uiState.value.score}")
        }
    }
}