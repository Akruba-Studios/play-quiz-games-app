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

// Datos de tematización del Guardián
data class GuardianTheme(
    val name: String,
    val emoji: String,
    val dialogues: List<List<String>>
)

// Estadísticas de batalla
data class BattleStats(
    val startTime: Long = System.currentTimeMillis(),
    val totalTime: Long = 0,
    val accuracy: Float = 0f,
    val longestStreak: Int = 0,
    val currentStreak: Int = 0
)

// Estado de la UI para la pantalla del Jefe Épico
data class BossState(
    val isLoading: Boolean = true,
    val levelName: String = "",
    val currentQuestion: Question? = null,
    val totalQuestions: Int = 1,
    val bossHealth: Float = 1.0f,
    val playerMistakes: Int = 0,
    val maxMistakes: Int = 1,
    val correctAnswersCount: Int = 0,
    val generatedHintLetters: String = "",
    val userAnswer: String = "",
    val usedLetterIndices: Set<Int> = emptySet(),
    // Nuevos estados épicos
    val currentPhase: Int = 1,
    val guardianTheme: GuardianTheme = GuardianTheme("", "", emptyList()),
    val currentDialogue: String = "",
    val isPhaseTransition: Boolean = false,
    val showVictoryAnimation: Boolean = false,
    val showShakeEffect: Boolean = false,
    val battleStats: BattleStats = BattleStats(),
    val lettersReshuffleCounter: Int = 0,
    val timeRemaining: Int = 30, // segundos restantes
    val isTimerRunning: Boolean = false,
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
    private var reshuffleJob: Job? = null
    private var timerJob: Job? = null

    init {
        loadBossLevel()
    }

    private fun loadBossLevel() {
        viewModelScope.launch {
            levelPackage = quizRepository.getLevel(levelId)
            if (levelPackage != null) {
                val theme = generateGuardianTheme(countryId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        levelName = levelPackage!!.levelName["es"] ?: "Desafío del Guardián",
                        totalQuestions = levelPackage!!.questions.size,
                        guardianTheme = theme,
                        currentDialogue = theme.dialogues[0].random(),
                        battleStats = BattleStats()
                    )
                }
                prepareNextQuestion()
            }
        }
    }

    private fun generateGuardianTheme(countryId: String): GuardianTheme {
        return when(countryId.lowercase()) {
            "mexico" -> GuardianTheme(
                "Guardián Azteca 🏛️",
                "⚡",
                listOf(
                    listOf("¡Soy el protector eterno de estas tierras sagradas!", "¿Crees poder desafiar mi sabiduría milenaria?"),
                    listOf("¡Imposible! ¿Cómo conoces estos secretos?", "Mi poder se debilita... pero no me rendiré"),
                    listOf("¡JAMÁS PERMITIRÉ QUE DOMINES MÉXICO!", "¡Uso mi último aliento de furia ancestral!")
                )
            )
            "japan" -> GuardianTheme(
                "Guardián Samurai ⛩️",
                "⚔️",
                listOf(
                    listOf("El honor del Japón fluye por mis venas", "Tu conocimiento será probado por mi katana"),
                    listOf("Tu sabiduría es... impresionante, forastero", "El bushido me enseña a luchar hasta el final"),
                    listOf("¡Por el honor de mis ancestros, resistiré!", "¡Mi último seppuku intelectual!")
                )
            )
            "egypt" -> GuardianTheme(
                "Guardián Faraónico 🔺",
                "☥",
                listOf(
                    listOf("Los secretos del Nilo fluyen por mi esencia", "¿Osas desafiar la sabiduría de los faraones?"),
                    listOf("¡Ra me abandona! Tu conocimiento es vasto", "Las pirámides tiemblan ante tu sabiduría"),
                    listOf("¡Por Anubis, nunca te rendiré mi reino!", "¡Invoco la maldición final de los faraones!")
                )
            )
            "france" -> GuardianTheme(
                "Guardián Galo 🗼",
                "⚜️",
                listOf(
                    listOf("La elegancia francesa nunca será tuya", "¿Mon Dieu! ¿Quién osa desafiarme?"),
                    listOf("C'est impossible! Tu cultura me sorprende", "¡Mi orgullo galo está en peligro!"),
                    listOf("¡Vive la France! ¡Nunca me rendiré!", "¡Mi último croissant de resistencia!")
                )
            )
            else -> GuardianTheme(
                "Guardián Ancestral 🏰",
                "⭐",
                listOf(
                    listOf("Soy el protector eterno de esta tierra", "¿Crees tener el conocimiento para vencerme?"),
                    listOf("Tu sabiduría es... preocupante", "No esperaba tal desafío"),
                    listOf("¡Nunca me rendiré sin luchar!", "¡Mi último aliento de resistencia!")
                )
            )
        }
    }

    private fun prepareNextQuestion() {
        if (currentQuestionIndex < (levelPackage?.questions?.size ?: 0)) {
            val question = levelPackage!!.questions[currentQuestionIndex]
            val newPhase = calculatePhase()
            val phaseChanged = newPhase != uiState.value.currentPhase

            if (phaseChanged && newPhase > 1) {
                triggerPhaseTransition(newPhase)
            }

            _uiState.update {
                it.copy(
                    currentQuestion = question,
                    userAnswer = "",
                    generatedHintLetters = generateHintLettersByPhase(question.correctAnswer, newPhase),
                    usedLetterIndices = emptySet(),
                    currentPhase = newPhase,
                    lettersReshuffleCounter = 0
                )
            }

            // Iniciar reshuffling automático en fase 2 y 3
            if (newPhase >= 2) {
                startLetterReshuffling()
            }
            // ⏰ AÑADIR ESTA LÍNEA:
            startQuestionTimer()

            isAnswerProcessing = false
        } else {
            val finalBossHealth = uiState.value.bossHealth
            if (finalBossHealth <= 0.0f) {
                triggerVictorySequence()
            } else {
                endGame(victory = false)
            }
        }
    }
    private fun startQuestionTimer() {
        timerJob?.cancel()

        val timeForPhase = when(uiState.value.currentPhase) {
            1 -> 30 // 30 segundos fase normal
            2 -> 25 // 25 segundos fase agitada
            3 -> 20 // 20 segundos fase desesperada
            else -> 30
        }

        _uiState.update {
            it.copy(
                timeRemaining = timeForPhase,
                isTimerRunning = true
            )
        }

        timerJob = viewModelScope.launch {
            for (time in timeForPhase downTo 0) {
                _uiState.update { it.copy(timeRemaining = time) }
                if (time == 0) {
                    // Tiempo agotado = respuesta incorrecta automática
                    _uiState.update { it.copy(isTimerRunning = false) }
                    if (!isAnswerProcessing) {
                        checkAnswer() // Esto procesará como respuesta incorrecta
                    }
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun calculatePhase(): Int {
        val health = uiState.value.bossHealth
        return when {
            health > 0.7f -> 1
            health > 0.3f -> 2
            else -> 3
        }
    }

    private fun triggerPhaseTransition(newPhase: Int) {
        val theme = uiState.value.guardianTheme
        val newDialogue = theme.dialogues.getOrNull(newPhase - 1)?.random() ?: ""

        _uiState.update {
            it.copy(
                isPhaseTransition = true,
                currentDialogue = newDialogue
            )
        }

        viewModelScope.launch {
            delay(2000L) // Mostrar transición por 2 segundos
            _uiState.update { it.copy(isPhaseTransition = false) }
        }
    }

    private fun startLetterReshuffling() {
        reshuffleJob?.cancel()
        reshuffleJob = viewModelScope.launch {
            while (uiState.value.currentPhase >= 2 && !isAnswerProcessing) {
                delay(if (uiState.value.currentPhase == 3) 6000L else 8000L)
                if (!isAnswerProcessing && uiState.value.userAnswer.isEmpty()) {
                    val question = uiState.value.currentQuestion
                    if (question != null) {
                        _uiState.update {
                            it.copy(
                                generatedHintLetters = generateHintLettersByPhase(
                                    question.correctAnswer,
                                    it.currentPhase
                                ),
                                lettersReshuffleCounter = it.lettersReshuffleCounter + 1
                            )
                        }
                    }
                }
            }
        }
    }

    private fun generateHintLettersByPhase(correctAnswer: String, phase: Int): String {
        val allCharsInAnswer = correctAnswer.uppercase().toList()
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        // Escalamiento de dificultad por fase
        val totalLettersInBank = when(phase) {
            1 -> 15  // Normal
            2 -> 18  // +3 señuelos
            3 -> 21  // +6 señuelos
            else -> 15
        }

        val letterCountInAnswer = allCharsInAnswer.count { it.isLetter() }
        val decoyLettersCount = (totalLettersInBank - letterCountInAnswer).coerceAtLeast(4)

        val answerLettersOnly = allCharsInAnswer.filter { it.isLetter() }

        // En fase 3, añadir letras confusas (similares visualmente)
        val availableDecoys = if (phase == 3) {
            val confusingLetters = generateConfusingDecoys(answerLettersOnly)
            val remainingNeeded = decoyLettersCount - confusingLetters.size
            confusingLetters + alphabet.toList()
                .filter { !answerLettersOnly.contains(it) && !confusingLetters.contains(it) }
                .shuffled()
                .take(remainingNeeded.coerceAtLeast(0))
        } else {
            alphabet.toList()
                .filter { !answerLettersOnly.contains(it) }
                .shuffled()
                .take(decoyLettersCount)
        }

        return (allCharsInAnswer + availableDecoys).shuffled().joinToString("")
    }

    private fun generateConfusingDecoys(answerLetters: List<Char>): List<Char> {
        val confusingPairs = mapOf(
            'B' to 'P', 'P' to 'B',
            'D' to 'O', 'O' to 'D',
            'M' to 'N', 'N' to 'M',
            'I' to 'L', 'L' to 'I',
            'F' to 'E', 'E' to 'F',
            'C' to 'G', 'G' to 'C'
        )

        return answerLetters.mapNotNull { letter ->
            confusingPairs[letter]
        }.distinct().take(3)
    }

    private fun checkAnswer() {
        if (isAnswerProcessing) return
        isAnswerProcessing = true

        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
        reshuffleJob?.cancel()

        viewModelScope.launch {
            val state = _uiState.value
            val isCorrect = state.currentQuestion?.validAnswers?.contains(state.userAnswer.lowercase()) == true

            if (isCorrect) {
                val newHealth = (state.bossHealth - (1.0f / state.totalQuestions)).coerceAtLeast(0f)
                val newStreak = state.battleStats.currentStreak + 1

                _uiState.update {
                    it.copy(
                        bossHealth = newHealth,
                        correctAnswersCount = it.correctAnswersCount + 1,
                        battleStats = it.battleStats.copy(
                            currentStreak = newStreak,
                            longestStreak = maxOf(it.battleStats.longestStreak, newStreak)
                        )
                    )
                }

                if (newHealth <= 0.0f) {
                    triggerVictorySequence()
                    return@launch
                }
            } else {
                val newMistakes = state.playerMistakes + 1
                _uiState.update {
                    it.copy(
                        playerMistakes = newMistakes,
                        showShakeEffect = true,
                        battleStats = it.battleStats.copy(currentStreak = 0)
                    )
                }

                // Efecto de shake por 500ms
                viewModelScope.launch {
                    delay(500L)
                    _uiState.update { it.copy(showShakeEffect = false) }
                }

                if (newMistakes >= state.maxMistakes) {
                    endGame(victory = false)
                    return@launch
                }
            }

            delay(1500L)
            currentQuestionIndex++
            prepareNextQuestion()
        }
    }

    private fun triggerVictorySequence() {
        val totalTime = System.currentTimeMillis() - uiState.value.battleStats.startTime
        val accuracy = (uiState.value.correctAnswersCount.toFloat() / uiState.value.totalQuestions) * 100f

        _uiState.update {
            it.copy(
                showVictoryAnimation = true,
                battleStats = it.battleStats.copy(
                    totalTime = totalTime,
                    accuracy = accuracy
                )
            )
        }

        viewModelScope.launch {
            delay(3000L) // Mostrar animación por 3 segundos
            endGame(victory = true)
        }
    }

    private fun endGame(victory: Boolean) {
        val result = GameResult(
            score = if (victory) 10000 else 0,
            correctAnswers = uiState.value.correctAnswersCount,
            totalQuestions = uiState.value.totalQuestions,
            starsEarned = if (victory) 3 else 0
        )
        _gameResult.value = result
    }

    // --- Funciones de UI ---
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

    fun dismissPhaseTransition() {
        _uiState.update { it.copy(isPhaseTransition = false) }
    }
}