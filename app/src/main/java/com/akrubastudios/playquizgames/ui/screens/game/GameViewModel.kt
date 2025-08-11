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
        // Lanzamos una coroutine para llamar a nuestra función suspendida
        viewModelScope.launch {
            val loadedLevel = repository.getLevel("Jdneptg2H9iyLervZ6LG") // Llama a la nueva función suspend

            if (loadedLevel != null) {
                levelPackage = loadedLevel // Guardamos el nivel cargado
                val firstQuestion = loadedLevel.questions[currentQuestionIndex]
                val hints = generateHintLetters(firstQuestion.correctAnswer)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentQuestion = firstQuestion,
                        totalQuestions = loadedLevel.questions.size,
                        questionNumber = currentQuestionIndex + 1,
                        generatedHintLetters = hints
                    )
                }
                startTimer()
            } else {
                Log.d("GameViewModel", "Error al cargar el nivel desde Firestore.")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun generateHintLetters(correctAnswer: String): String {
        // 1. Define el alfabeto para las letras aleatorias.
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        // 2. Decide cuántas letras señuelo añadir (ej. entre 6 y 8).
        val totalLettersInBank = 12
        val decoyLettersCount = totalLettersInBank - correctAnswer.length

        // 3. Coge las letras de la respuesta correcta.
        val answerLetters = correctAnswer.uppercase().toList()

        // 4. Coge letras aleatorias del alfabeto que NO estén en la respuesta.
        val randomLetters = alphabet.toList()
            .filter { !answerLetters.contains(it) } // Filtra las que ya están
            .shuffled() // Baraja el resto del alfabeto
            .take(decoyLettersCount) // Coge el número de señuelos que necesitamos

        // 5. Junta las letras de la respuesta y las aleatorias, y barájalas.
        return (answerLetters + randomLetters).shuffled().joinToString("")
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
            val nextQuestion = levelPackage!!.questions[currentQuestionIndex]
            val hints = generateHintLetters(nextQuestion.correctAnswer) // <-- Genera las pistas

            _uiState.update {
                it.copy(
                    currentQuestion = nextQuestion,
                    questionNumber = currentQuestionIndex + 1,
                    userAnswer = "",
                    generatedHintLetters = hints
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