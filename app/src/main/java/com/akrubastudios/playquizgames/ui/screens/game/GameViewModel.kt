package com.akrubastudios.playquizgames.ui.screens.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import com.akrubastudios.playquizgames.domain.GameResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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

    companion object {
        private const val QUESTION_TIME_LIMIT_SECONDS = 15L // Tiempo del temporizador
    }
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    private var timerJob: Job? = null // Para poder controlar (cancelar) el temporizador

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
            startTimer() // Inicia el temporizador para la nueva pregunta
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
        stopTimer() // Detiene el temporizador inmediatamente
        viewModelScope.launch {
            val state = uiState.value
            val isCorrect = state.currentQuestion?.validAnswers?.contains(state.userAnswer.lowercase()) == true

            if (isCorrect) {
                Log.d("GameViewModel", "¡Respuesta Correcta!")
                // El puntaje base es 1000.
                // Se añade una bonificación basada en el tiempo restante.
                // Puntaje Ganado = 1000 (base) + (Tiempo Restante * 100)
                // (remainingTime * 100) -> Si quedan 10 seg, suma 1000 extra. Si queda 1 seg, suma 100.
                val pointsWon = 1000 + (uiState.value.remainingTime * 100).toInt()
                _uiState.update {
                    it.copy(
                        score = it.score + pointsWon,
                        correctAnswersCount = it.correctAnswersCount + 1 // <-- AÑADE ESTA LÍNEA
                    )
                }

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
            startTimer() // Inicia el temporizador para la nueva pregunta
        } else {
            Log.d("GameViewModel", "Juego Terminado. Puntaje final: ${uiState.value.score}")
            _gameResult.value = GameResult(
                score = uiState.value.score,
                correctAnswers = uiState.value.correctAnswersCount,
                totalQuestions = uiState.value.totalQuestions
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel() // Cancela cualquier temporizador anterior
        timerJob = viewModelScope.launch {
            var timeLeft = QUESTION_TIME_LIMIT_SECONDS
            _uiState.update { it.copy(remainingTime = timeLeft) }

            while (timeLeft > 0) {
                delay(1000L) // Espera 1 segundo
                timeLeft--
                _uiState.update { it.copy(remainingTime = timeLeft) }
            }

            // Si el tiempo llega a 0, se considera una respuesta incorrecta
            checkAnswer()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }
}