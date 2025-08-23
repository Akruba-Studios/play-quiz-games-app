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
import com.akrubastudios.playquizgames.core.LanguageManager
import javax.inject.Inject
import android.app.Application
import com.akrubastudios.playquizgames.R

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
    val questionText: String = "",
    val currentQuestion: Question? = null,
    val currentCorrectAnswer: String = "",
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
    val dialogueIndexInPhase: Int = 0,
)

@HiltViewModel
class BossViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val application: Application,
    private val languageManager: LanguageManager,
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
                val lang = languageManager.languageStateFlow.value
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        levelName = levelPackage!!.levelName[lang] ?: levelPackage!!.levelName["es"] ?: "Guardian Challenge",
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
        // Importa R si es necesario
        val resources = application.resources
        val (nameRes, emoji, dialoguePrefix) = when (countryId.lowercase()) {
            "mexico" -> Triple(R.string.guardian_name_mexico, "⚡", "guardian_dialogue_mexico")
            "japan" -> Triple(R.string.guardian_name_japan, "⚔️", "guardian_dialogue_japan")
            "egypt" -> Triple(R.string.guardian_name_egypt, "☥", "guardian_dialogue_egypt")
            "france" -> Triple(R.string.guardian_name_france, "⚜️", "guardian_dialogue_france")
            else -> Triple(R.string.guardian_name_default, "⭐", "guardian_dialogue_default")
        }

        val dialogues = (1..3).map { phase ->
            val arrayId = resources.getIdentifier("${dialoguePrefix}_phase$phase", "array", application.packageName)
            if (arrayId != 0) {
                resources.getStringArray(arrayId).toList()
            } else {
                emptyList()
            }
        }

        return GuardianTheme(
            name = resources.getString(nameRes),
            emoji = emoji,
            dialogues = dialogues
        )
    }

    private fun updateDialogue() {
        val phaseIndex = uiState.value.currentPhase - 1
        val possibleDialogues = uiState.value.guardianTheme.dialogues.getOrNull(phaseIndex) ?: emptyList()

        if (possibleDialogues.isNotEmpty()) {
            val currentDialogueIndex = uiState.value.dialogueIndexInPhase % possibleDialogues.size
            val selectedDialogue = possibleDialogues[currentDialogueIndex]

            _uiState.update {
                it.copy(
                    currentDialogue = selectedDialogue,
                    dialogueIndexInPhase = it.dialogueIndexInPhase + 1
                )
            }
        }
    }

    private fun prepareNextQuestion() {
        if (currentQuestionIndex < (levelPackage?.questions?.size ?: 0)) {
            val question = levelPackage!!.questions[currentQuestionIndex]
            val newPhase = calculatePhase()
            val phaseChanged = newPhase != uiState.value.currentPhase

            if (phaseChanged && newPhase > 1) {
                triggerPhaseTransition(newPhase)
                // ← AÑADIR ESTA LÍNEA JUSTO DESPUÉS:
                _uiState.update { it.copy(dialogueIndexInPhase = 0) }
            }

            updateDialogue()

            val lang = languageManager.languageStateFlow.value
            val correctAnswerForUi = if (lang == "es") question.correctAnswer_es else question.correctAnswer_en

            val localizedQuestionText = if (lang == "es") question.questionText_es else question.questionText_en

            _uiState.update {
                it.copy(
                    currentQuestion = question,
                    questionText = localizedQuestionText,
                    currentCorrectAnswer = correctAnswerForUi,
                    userAnswer = "",
                    generatedHintLetters = generateHintLettersByPhase(correctAnswerForUi, newPhase),
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
        _uiState.update {
            it.copy(
                isPhaseTransition = true,
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
                    // --- INICIO DE LA CORRECCIÓN ---
                    // Obtenemos la respuesta correcta ya localizada desde el estado.
                    val correctAnswer = uiState.value.currentCorrectAnswer

                    // Verificamos que no esté vacía antes de usarla.
                    if (correctAnswer.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                generatedHintLetters = generateHintLettersByPhase(
                                    correctAnswer, // Usamos la variable local
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

    private fun generateHintLettersByPhase(localizedCorrectAnswer: String, phase: Int): String {
        val allCharsInAnswer = localizedCorrectAnswer.uppercase().filter { it.isLetter() }.toList()
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

        return (answerLettersOnly + availableDecoys).shuffled().joinToString("")
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
            val normalizedUserAnswer = state.userAnswer.replace(" ", "").lowercase()
            val lang = languageManager.languageStateFlow.value

            // Obtenemos la LISTA de respuestas válidas para el idioma actual.
            val validAnswersForLang = state.currentQuestion?.validAnswers?.get(lang) ?: emptyList()

            // Verificamos si la respuesta del usuario está en la lista correcta.
            val isCorrect = validAnswersForLang.any { validAnswer ->
                validAnswer.replace(" ", "").lowercase() == normalizedUserAnswer
            }

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

        val requiredLength = uiState.value.currentCorrectAnswer.count { it.isLetter() }

        if (uiState.value.userAnswer.length < requiredLength) {
            _uiState.update {
                it.copy(
                    userAnswer = it.userAnswer + letter,
                    usedLetterIndices = it.usedLetterIndices + index
                )
            }
            if (uiState.value.userAnswer.length == requiredLength) {
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