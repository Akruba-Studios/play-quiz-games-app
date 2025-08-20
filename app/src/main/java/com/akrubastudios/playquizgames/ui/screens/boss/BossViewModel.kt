package com.akrubastudios.playquizgames.ui.screens.boss

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import com.akrubastudios.playquizgames.domain.GameResult
import com.akrubastudios.playquizgames.domain.Question
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI para la pantalla del Jefe
data class BossState(
    val isLoading: Boolean = true,
    val levelName: String = "",
    val currentQuestion: Question? = null,
    val totalQuestions: Int = 1,
    val bossHealth: Float = 1.0f, // Vida del jefe de 0.0f a 1.0f
    val playerMistakes: Int = 0,
    val maxMistakes: Int = 3,
    val correctAnswersCount: Int = 0,
    val generatedHintLetters: String = "",
    val userAnswer: String = "",
    val usedLetterIndices: Set<Int> = emptySet()
)

@HiltViewModel
class BossViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val levelId: String = savedStateHandle.get<String>("levelId")!!
    val countryId: String = savedStateHandle.get<String>("countryId")!!

    private val _uiState = MutableStateFlow(BossState())
    val uiState = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult = _gameResult.asStateFlow()

    private var levelPackage: QuizLevelPackage? = null
    private var currentQuestionIndex = 0
    private var isAnswerProcessing = false

    init {
        loadBossLevel()
    }

    private fun loadBossLevel() {
        viewModelScope.launch {
            levelPackage = quizRepository.getLevel(levelId)
            if (levelPackage != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        levelName = levelPackage!!.levelName["es"] ?: "Desafío del Guardián",
                        totalQuestions = levelPackage!!.questions.size
                    )
                }
                prepareNextQuestion()
            }
        }
    }

    private fun prepareNextQuestion() {
        if (currentQuestionIndex < (levelPackage?.questions?.size ?: 0)) {
            val question = levelPackage!!.questions[currentQuestionIndex]
            _uiState.update {
                it.copy(
                    currentQuestion = question,
                    userAnswer = "",
                    generatedHintLetters = generateHintLetters(question.correctAnswer),
                    usedLetterIndices = emptySet()
                )
            }
            isAnswerProcessing = false
        } else {
            // No quedan más preguntas. Ahora verificamos si el jefe fue derrotado.
            val finalBossHealth = uiState.value.bossHealth
            if (finalBossHealth <= 0.0f) {
                // Si la vida es 0, es una victoria.
                endGame(victory = true)
            } else {
                // Si aún le quedaba vida, el jugador no lo logró. Es una derrota.
                endGame(victory = false)
            }
        }
    }

    private fun checkAnswer() {
        if (isAnswerProcessing) return
        isAnswerProcessing = true

        viewModelScope.launch {
            val state = _uiState.value
            val isCorrect = state.currentQuestion?.validAnswers?.contains(state.userAnswer.lowercase()) == true

            if (isCorrect) {
                val newHealth = (state.bossHealth - (1.0f / state.totalQuestions)).coerceAtLeast(0f)
                _uiState.update { it.copy(
                    bossHealth = newHealth,
                    correctAnswersCount = it.correctAnswersCount + 1 // <-- AÑADIR
                ) }
                if (newHealth <= 0.0f) {
                    endGame(victory = true)
                    return@launch
                }
            } else {
                val newMistakes = state.playerMistakes + 1
                _uiState.update { it.copy(playerMistakes = newMistakes) }
                if (newMistakes >= state.maxMistakes) {
                    endGame(victory = false)
                    return@launch
                }
            }

            delay(1000L)
            currentQuestionIndex++
            prepareNextQuestion()
        }
    }

    private fun endGame(victory: Boolean) {
        // En una victoria de Jefe, siempre son 3 estrellas. En derrota, 0.
        // El score es simbólico, ya que la recompensa es la "dominación".
        val result = GameResult(
            score = if (victory) 10000 else 0,
            correctAnswers = uiState.value.correctAnswersCount,
            totalQuestions = uiState.value.totalQuestions,
            starsEarned = if (victory) 3 else 0
        )
        // La BossScreen se encargará de enviar la score_request
        _gameResult.value = result
    }

    // --- Lógica de UI (similar a GameViewModel) ---
    private fun generateHintLetters(correctAnswer: String): String {
        // CORRECCIÓN: Usamos la misma lógica robusta del GameViewModel.
        val allCharsInAnswer = correctAnswer.uppercase().toList()

        // Mantenemos la lógica de anagrama con señuelos, pero ahora respeta los espacios.
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val totalLettersInBank = 15

        val letterCountInAnswer = allCharsInAnswer.count { it.isLetter() }
        val decoyLettersCount = (totalLettersInBank - letterCountInAnswer).coerceAtLeast(4)

        val answerLettersOnly = allCharsInAnswer.filter { it.isLetter() }
        val randomLetters = alphabet.toList()
            .filter { !answerLettersOnly.contains(it) }
            .shuffled()
            .take(decoyLettersCount)

        return (allCharsInAnswer + randomLetters).shuffled().joinToString("")
    }

    fun onLetterClick(letter: Char, index: Int) {
        if (uiState.value.usedLetterIndices.contains(index)) return
        val answerLength = uiState.value.currentQuestion?.correctAnswer?.length ?: 0
        if (uiState.value.userAnswer.length < answerLength) {
            _uiState.update {
                it.copy(
                    userAnswer = it.userAnswer + letter,
                    usedLetterIndices = it.usedLetterIndices + index
                )
            }
            if (uiState.value.userAnswer.length == answerLength) {
                checkAnswer()
            }
        }
    }

    fun clearUserAnswer() {
        _uiState.update { it.copy(userAnswer = "", usedLetterIndices = emptySet()) }
    }
}