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
import android.util.Log
import com.akrubastudios.playquizgames.R
import android.content.res.Configuration
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.SoundEffect
import com.akrubastudios.playquizgames.core.SoundManager
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.models.ParsedVisualTheme
import com.akrubastudios.playquizgames.domain.models.toParsed
import com.akrubastudios.playquizgames.ui.screens.boss.background.ArchetypeRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.util.Locale

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
    val maxMistakes: Int = 3,
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
    val currentGems: Int = 0,
    val showHelpsSheet: Boolean = false,
    val isProcessingHelp: Boolean = false,
    val isExtraTimeUsed: Boolean = false,
    val isRemoveLettersUsed: Boolean = false,
    val revealLetterUses: Int = 0,
    val isShowHintUsed: Boolean = false,
    val showFunFactDialog: Boolean = false,
    val currentFunFact: String = "",
    val revealedLetterPositions: Set<Int> = emptySet(),
    val showCorrectEffect: Boolean = false,
    val showIncorrectEffect: Boolean = false,
    val showClearAnimation: Boolean = false,
    val visualTheme: ParsedVisualTheme? = null
)

@HiltViewModel
class BossViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val application: Application,
    private val languageManager: LanguageManager,
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    val musicManager: MusicManager,
    private val soundManager: SoundManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        const val HELP_EXTRA_TIME_SECONDS = 15 // Tiempo agregado al usar el cheat timer
        const val HELP_EXTRA_TIME_COST = 4 // Costo en gemas del cheat timer
        const val HELP_REMOVE_LETTERS_COST = 5 // Costo gemas del cheat remove letras
        const val HELP_REVEAL_LETTER_COST_INITIAL = 2 // Costo gemas cheat revelar letras
        const val HELP_REVEAL_LETTER_COST_INCREMENT = 1 // costo gemas cheat revelar letras incremental
        const val HELP_SHOW_HINT_COST = 7 // Costo gemas cheat mostrar pista o hint
    }

    private val archetypeRegistry = ArchetypeRegistry()

    val levelId: String = savedStateHandle.get<String>("levelId")!!
    val countryId: String = savedStateHandle.get<String>("countryId")!!

    private val _uiState = MutableStateFlow(BossState())
    val uiState = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult = _gameResult.asStateFlow()

    private var levelPackage: QuizLevelPackage? = null
    private var shuffledQuestions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private var isAnswerProcessing = false
    private var reshuffleJob: Job? = null
    private var timerJob: Job? = null
    private var currentLanguage: String = ""

    init {
        Log.d("BossViewModel", "Init empezando")
        Log.d("BossViewModel", "Idioma inicial: ${languageManager.languageStateFlow.value}")

        loadBossLevel()

        viewModelScope.launch {
            Log.d("BossViewModel", "Iniciando observer de idioma")
            languageManager.languageStateFlow.collect { newLang ->
                Log.d("BossViewModel", "Idioma detectado: $newLang, actual: $currentLanguage")
                if (currentLanguage.isNotEmpty() && currentLanguage != newLang) {
                    Log.d("BossViewModel", "CAMBIO DETECTADO: $currentLanguage -> $newLang")
                    updateDialoguesForNewLanguage()
                }
                currentLanguage = newLang
            }
        }
    }

    private fun loadBossLevel() {
        viewModelScope.launch {
            levelPackage = quizRepository.getLevel(levelId)
            val userData = gameDataRepository.getUserData()

            // NUEVO: Cargar tema visual desde Firebase
            val countryVisualTheme = quizRepository.getCountryVisualTheme(countryId)
            val parsedTheme = countryVisualTheme?.toParsed(archetypeRegistry)

            if (levelPackage != null && parsedTheme != null) {
                shuffledQuestions = levelPackage!!.questions.shuffled()

                // Usar datos del guardián desde Firebase
                val guardianName = parsedTheme.guardianData.name
                val guardianDialogues = parsedTheme.guardianData.dialogues

                val lang = languageManager.languageStateFlow.value
                val phase1Dialogues = guardianDialogues["phase1"]?.get(lang) ?: emptyList()

                val theme = GuardianTheme(
                    name = guardianName[lang] ?: guardianName["es"] ?: "Guardian",
                    emoji = getGuardianEmoji(countryId),
                    dialogues = listOf(
                        guardianDialogues["phase1"]?.get(lang) ?: emptyList(),
                        guardianDialogues["phase2"]?.get(lang) ?: emptyList(),
                        guardianDialogues["phase3"]?.get(lang) ?: emptyList()
                    )
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        levelName = levelPackage!!.levelName[lang] ?: levelPackage!!.levelName["es"] ?: "Guardian Challenge",
                        totalQuestions = shuffledQuestions.size,
                        guardianTheme = theme,
                        currentDialogue = phase1Dialogues.randomOrNull() ?: "",
                        battleStats = BattleStats(),
                        currentGems = userData?.gems ?: 0,
                        visualTheme = parsedTheme
                    )
                }
                prepareNextQuestion()
            } else {
                // Fallback si no se pueden cargar los datos
                Log.e("BossViewModel", "Error: No se pudo cargar levelPackage o visualTheme")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateDialoguesForNewLanguage() {
        Log.d("BossViewModel", "Actualizando diálogos por cambio de idioma")

        val visualTheme = uiState.value.visualTheme ?: return
        val newLang = languageManager.languageStateFlow.value

        val guardianName = visualTheme.guardianData.name[newLang]
            ?: visualTheme.guardianData.name["es"]
            ?: "Guardian"

        val guardianDialogues = visualTheme.guardianData.dialogues
        val newTheme = GuardianTheme(
            name = guardianName,
            emoji = getGuardianEmoji(countryId),
            dialogues = listOf(
                guardianDialogues["phase1"]?.get(newLang) ?: emptyList(),
                guardianDialogues["phase2"]?.get(newLang) ?: emptyList(),
                guardianDialogues["phase3"]?.get(newLang) ?: emptyList()
            )
        )

        val currentPhase = uiState.value.currentPhase
        val currentDialogueIndex = uiState.value.dialogueIndexInPhase
        val newDialogue = if (currentPhase > 0 && currentPhase <= newTheme.dialogues.size) {
            val phaseDialogues = newTheme.dialogues[currentPhase - 1]
            if (phaseDialogues.isNotEmpty()) {
                phaseDialogues[currentDialogueIndex % phaseDialogues.size]
            } else ""
        } else ""

        _uiState.update {
            it.copy(
                guardianTheme = newTheme,
                currentDialogue = newDialogue
            )
        }
    }

    private fun generateGuardianTheme(countryId: String): GuardianTheme {
        // FORZAR idioma de la app, no del sistema
        val appLanguage = languageManager.languageStateFlow.value
        val locale = Locale(appLanguage)
        val config = Configuration(application.resources.configuration)
        config.setLocale(locale)
        val localizedResources = application.createConfigurationContext(config).resources

        Log.d("BossViewModel", "Generando tema con idioma: $appLanguage")

        val (nameRes, emoji, dialoguePrefix) = when (countryId.lowercase()) {
            "mexico" -> Triple(R.string.guardian_name_mexico, "⚡", "guardian_dialogue_mexico")
            "japan" -> Triple(R.string.guardian_name_japan, "⚔️", "guardian_dialogue_japan")
            "egypt" -> Triple(R.string.guardian_name_egypt, "☥", "guardian_dialogue_egypt")
            "france" -> Triple(R.string.guardian_name_france, "⚜️", "guardian_dialogue_france")
            else -> Triple(R.string.guardian_name_default, "⭐", "guardian_dialogue_default")
        }

        val dialogues = (1..3).map { phase ->
            val arrayId = localizedResources.getIdentifier("${dialoguePrefix}_phase$phase", "array", application.packageName)
            if (arrayId != 0) {
                localizedResources.getStringArray(arrayId).toList()
            } else {
                emptyList()
            }
        }

        return GuardianTheme(
            name = localizedResources.getString(nameRes),
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
        if (currentQuestionIndex < shuffledQuestions.size) {
            val question = shuffledQuestions[currentQuestionIndex]
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
                    lettersReshuffleCounter = 0,
                    isExtraTimeUsed = false,
                    isRemoveLettersUsed = false,
                    revealLetterUses = 0,
                    isShowHintUsed = false,
                    revealedLetterPositions = emptySet(),
                    showCorrectEffect = false,
                    showIncorrectEffect = false
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
            // Calculamos cuántos aciertos se necesitaban para ganar.
            val requiredCorrectAnswers = shuffledQuestions.size - (uiState.value.maxMistakes - 1)
            val isVictory = uiState.value.correctAnswersCount >= requiredCorrectAnswers

            if (isVictory) {
                // Si ha ganado, disparamos la secuencia de victoria.
                triggerVictorySequence()
            } else {
                // Si no, termina el juego como una derrota.
                endGame(victory = false)
            }
        }
    }
    private fun startQuestionTimer(startTime: Long? = null) {
        timerJob?.cancel()

        val initialTime = startTime?.toInt() ?: when(uiState.value.currentPhase) {
            1 -> 30 // 30 segundos fase normal
            2 -> 25 // 25 segundos fase agitada
            3 -> 20 // 20 segundos fase desesperada
            else -> 30
        }

        _uiState.update {
            it.copy(
                timeRemaining = initialTime,
                isTimerRunning = true
            )
        }

        timerJob = viewModelScope.launch {
            for (time in initialTime downTo 0) {
                _uiState.update { it.copy(timeRemaining = time) }
                if (time in 1..3) {
                    soundManager.playSound(SoundEffect.TIMER_TICK)
                }
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
                soundManager.playSound(SoundEffect.CORRECT_ANSWER)
                val newHealth = (state.bossHealth - (1.0f / state.totalQuestions)).coerceAtLeast(0.05f)
                val newStreak = state.battleStats.currentStreak + 1
                _uiState.update {
                    it.copy(
                        bossHealth = newHealth,
                        correctAnswersCount = it.correctAnswersCount + 1,
                        showCorrectEffect = true,
                        showIncorrectEffect = false,
                        battleStats = it.battleStats.copy(
                            currentStreak = newStreak,
                            longestStreak = maxOf(it.battleStats.longestStreak, newStreak)
                        )
                    )
                }
            } else {
                soundManager.playSound(SoundEffect.INCORRECT_ANSWER)
                val newMistakes = state.playerMistakes + 1
                _uiState.update {
                    it.copy(
                        playerMistakes = newMistakes,
                        showCorrectEffect = false,
                        showIncorrectEffect = true,
                        battleStats = it.battleStats.copy(currentStreak = 0)
                    )
                }

                if (newMistakes >= state.maxMistakes) {
                    endGame(victory = false)
                    return@launch
                }
            }
            delay(1000L) // Mostrar el efecto por 1 segundo
            _uiState.update {
                it.copy(
                    showCorrectEffect = false,
                    showIncorrectEffect = false
                )
            }

            delay(500L) // Pausa adicional antes de continuar
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

        val correctAnswers = uiState.value.correctAnswersCount

        // 1. Calculamos el puntaje de XP basado en el número de aciertos.
        val score = if (victory) {
            when (correctAnswers) {
                shuffledQuestions.size -> 20000 // Todas correctas (ej. 10 de 10)
                shuffledQuestions.size - 1 -> 15000 // Un error (ej. 9 de 10)
                shuffledQuestions.size - 2 -> 10000 // Dos errores (ej. 8 de 10)
                else -> 10000 // Por si acaso, un valor base para la victoria
            }
        } else {
            0 // Si es derrota, el puntaje es 0
        }

        // 2. Creamos el objeto GameResult con el nuevo puntaje dinámico.
        val result = GameResult(
            score = score,
            correctAnswers = correctAnswers,
            totalQuestions = uiState.value.totalQuestions,
            // Las estrellas siguen siendo 3 si hay victoria, para las recompensas de dominación.
            starsEarned = if (victory) 3 else 0
        )

        _gameResult.value = result
    }

    // --- Funciones de UI ---
    fun onLetterClick(letter: Char, index: Int) {
        soundManager.playSound(SoundEffect.LETTER_CLICK)
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
                timerJob?.cancel()
                checkAnswer()
            }
        }
    }

    fun clearUserAnswer() {
        if (uiState.value.userAnswer.isNotEmpty()) {
            soundManager.playSound(SoundEffect.CLEAR_ANSWER)
        }
        // No hacer nada si ya se está borrando o la respuesta está vacía.
        if (_uiState.value.showClearAnimation || _uiState.value.userAnswer.isEmpty()) return

        viewModelScope.launch {
            // 1. Activa la animación.
            _uiState.update { it.copy(showClearAnimation = true) }

            // 2. Espera un breve momento para que la animación se vea.
            delay(300L)

            // 3. Borra los datos y desactiva la animación.
            _uiState.update {
                it.copy(
                    userAnswer = "",
                    usedLetterIndices = emptySet(),
                    revealedLetterPositions = emptySet(),
                    showClearAnimation = false
                )
            }
        }
    }

    fun dismissPhaseTransition() {
        _uiState.update { it.copy(isPhaseTransition = false) }
    }

    // --- Funciones para el Menú de Ayudas ---
    fun openHelpsSheet() {
        _uiState.update { it.copy(showHelpsSheet = true) }
    }

    fun closeHelpsSheet() {
        _uiState.update { it.copy(showHelpsSheet = false) }
    }

    fun useExtraTimeHelp() {
        val cost = HELP_EXTRA_TIME_COST
        val state = uiState.value

        if (state.currentGems < cost || state.isExtraTimeUsed) {
            return
        }

        _uiState.update { it.copy(isProcessingHelp = true) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("BossViewModel", "Error: Usuario nulo al intentar usar Tiempo Extra.")
            _uiState.update { it.copy(isProcessingHelp = false) }
            closeHelpsSheet()
            return
        }

        val spendRequest = hashMapOf(
            "userId" to uid,
            "amount" to cost,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("gem_spend_requests").add(spendRequest)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        Log.d("BossViewModel", "✅ Petición para 'Tiempo Extra' enviada.")

                        // --- INICIO DE LA LÓGICA CORREGIDA ---

                        // 1. Calculamos el nuevo tiempo total.
                        val newTotalTime = uiState.value.timeRemaining + HELP_EXTRA_TIME_SECONDS

                        // 2. Actualizamos el estado de la UI con los cambios.
                        _uiState.update {
                            it.copy(
                                isExtraTimeUsed = true,
                                timeRemaining = newTotalTime, // Actualizamos el número visible
                                currentGems = it.currentGems - cost
                            )
                        }

                        // 3. ¡CRUCIAL! Cancelamos el temporizador viejo y empezamos uno nuevo
                        //    con el tiempo actualizado.
                        startQuestionTimer(startTime = newTotalTime.toLong())

                        // --- FIN DE LA LÓGICA CORREGIDA ---

                    } else {
                        Log.e("BossViewModel", "❌ Error al crear gem_spend_request para 'Tiempo Extra'.", task.exception)
                    }

                    // Pase lo que pase, cerramos el menú.
                    _uiState.update { it.copy(isProcessingHelp = false) }
                    closeHelpsSheet()
                }
            }
    }

    fun useRemoveLettersHelp() {
        val cost = HELP_REMOVE_LETTERS_COST
        val state = uiState.value

        if (state.currentGems < cost || state.isRemoveLettersUsed) {
            return
        }

        _uiState.update { it.copy(isProcessingHelp = true) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("BossViewModel", "Error: Usuario nulo al intentar usar ayuda.")
            _uiState.update { it.copy(isProcessingHelp = false) }
            closeHelpsSheet()
            return
        }

        val spendRequest = hashMapOf(
            "userId" to uid,
            "amount" to cost,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("gem_spend_requests").add(spendRequest)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        Log.d("BossViewModel", "✅ Petición para 'Eliminar Letras' enviada.")

                        // APLICAMOS EL EFECTO VISUAL DESPUÉS DE CONFIRMAR LA ESCRITURA
                        val correctAnswerLetters = state.currentCorrectAnswer.uppercase().filter { it.isLetter() }.toSet()
                        val bankLetters = state.generatedHintLetters.toMutableList()

                        val decoyLettersIndices = bankLetters.withIndex()
                            .filter { !correctAnswerLetters.contains(it.value) }
                            .map { it.index }
                            .shuffled()

                        // Eliminamos TODOS los señuelos, no solo la mitad
                        val indicesToRemove = decoyLettersIndices.toSet()

                        val newBankLetters = bankLetters.withIndex()
                            .filter { !indicesToRemove.contains(it.index) }
                            .joinToString("") { it.value.toString() }

                        _uiState.update {
                            it.copy(
                                isRemoveLettersUsed = true,
                                generatedHintLetters = newBankLetters,
                                // El saldo real se actualizará por el listener,
                                // pero lo descontamos visualmente para feedback inmediato.
                                currentGems = it.currentGems - cost
                            )
                        }
                    } else {
                        Log.e("BossViewModel", "❌ Error al crear gem_spend_request para 'Eliminar Letras'.", task.exception)
                        // Opcional: Mostrar un Snackbar de error al usuario
                    }

                    // Pase lo que pase, terminamos la animación y cerramos el menú.
                    _uiState.update { it.copy(isProcessingHelp = false) }
                    closeHelpsSheet()
                }
            }
    }
    fun useRevealLetterHelp() {
        val state = uiState.value
        val cost = HELP_REVEAL_LETTER_COST_INITIAL + (state.revealLetterUses * HELP_REVEAL_LETTER_COST_INCREMENT)
        val lettersToRevealCount = state.currentCorrectAnswer.replace(" ", "").length

        if (state.currentGems < cost || state.userAnswer.length >= lettersToRevealCount) {
            return
        }

        _uiState.update { it.copy(isProcessingHelp = true) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("BossViewModel", "Error: Usuario nulo al intentar usar Revelar Letra.")
            _uiState.update { it.copy(isProcessingHelp = false) }
            closeHelpsSheet()
            return
        }

        val spendRequest = hashMapOf(
            "userId" to uid,
            "amount" to cost,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("gem_spend_requests").add(spendRequest)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        Log.d("BossViewModel", "✅ Petición para 'Revelar Letra' enviada.")

                        // --- LÓGICA SIMPLIFICADA ---
                        val correctAnswerNoSpaces = state.currentCorrectAnswer.replace(" ", "")
                        val userAnswer = state.userAnswer
                        val hintLetters = state.generatedHintLetters

                        // 1. Obtenemos la siguiente letra correcta que necesitamos
                        val nextCorrectLetter = correctAnswerNoSpaces[userAnswer.length]

                        // 2. Buscamos una letra disponible en el banco
                        val letterIndexInBank = hintLetters.withIndex()
                            .find { (index, char) ->
                                char.uppercaseChar() == nextCorrectLetter.uppercaseChar() &&
                                        !state.usedLetterIndices.contains(index)
                            }?.index

                        // 3. Si no encontramos letra disponible, creamos una "virtual"
                        val finalUsedIndices = if (letterIndexInBank != null) {
                            state.usedLetterIndices + letterIndexInBank
                        } else {
                            state.usedLetterIndices // No modificamos si es letra creada
                        }
                        // --- FIN LÓGICA SIMPLIFICADA ---

                        run {
                            val newAnswer = userAnswer + nextCorrectLetter
                            val newUsedIndices = finalUsedIndices
                            val newRevealedPositions = state.revealedLetterPositions + userAnswer.length

                            _uiState.update {
                                it.copy(
                                    userAnswer = newAnswer.uppercase(),
                                    usedLetterIndices = newUsedIndices,
                                    revealedLetterPositions = newRevealedPositions,
                                    revealLetterUses = it.revealLetterUses + 1,
                                    currentGems = it.currentGems - cost
                                )
                            }

                            if (newAnswer.length == correctAnswerNoSpaces.length) {
                                checkAnswer() // Llama a checkAnswer si se completó la palabra
                            }
                        }
                    } else {
                        Log.e("BossViewModel", "❌ Error al crear gem_spend_request para 'Revelar Letra'.", task.exception)
                    }
                    _uiState.update { it.copy(isProcessingHelp = false) }
                    closeHelpsSheet()
                }
            }
    }
    fun onFunFactDialogDismissed() {
        _uiState.update { it.copy(showFunFactDialog = false) }
        // Al cerrar el diálogo, ahora sí cerramos el sheet y reanudamos el juego.
        closeHelpsSheet()
    }

    fun useShowHintHelp() {
        val cost = HELP_SHOW_HINT_COST
        val state = uiState.value

        if (state.currentGems < cost || state.isShowHintUsed) {
            return
        }

        _uiState.update { it.copy(isProcessingHelp = true) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("BossViewModel", "Error: Usuario nulo al intentar usar Ver Pista.")
            _uiState.update { it.copy(isProcessingHelp = false) }
            closeHelpsSheet()
            return
        }

        val spendRequest = hashMapOf(
            "userId" to uid,
            "amount" to cost,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("gem_spend_requests").add(spendRequest)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        Log.d("BossViewModel", "✅ Petición para 'Ver Pista' enviada.")

                        // Lógica para mostrar el Fun Fact
                        val lang = languageManager.languageStateFlow.value
                        val funFactText = if (lang == "es") {
                            state.currentQuestion?.fun_fact_es
                        } else {
                            state.currentQuestion?.fun_fact_en
                        }

                        _uiState.update {
                            it.copy(
                                isShowHintUsed = true,
                                currentGems = it.currentGems - cost,
                                showHelpsSheet = false,
                                currentFunFact = funFactText ?: ""
                            )
                        }
                        delay(300L) // Pausa para que la animación del BottomSheet se complete
                        _uiState.update { it.copy(showFunFactDialog = true) }

                    } else {
                        Log.e("BossViewModel", "❌ Error al crear gem_spend_request para 'Ver Pista'.", task.exception)
                    }
                    _uiState.update { it.copy(isProcessingHelp = false) }
                    // ¡Importante! No cerramos el sheet aquí. Se cerrará cuando el usuario
                    // cierre el diálogo del Fun Fact.
                }
            }
    }
    private fun getGuardianEmoji(countryId: String): String {
        return when (countryId.lowercase()) {
            "mexico", "gt" -> "⚡"
            "jp" -> "⚔️"
            "eg" -> "☥"
            "fr" -> "⚜️"
            "br" -> "🌿"
            "ar", "cl", "uy" -> "⭐"
            "it", "es", "gr" -> "🏛️"
            "se", "no" -> "❄️"
            "ca", "us" -> "🦅"
            else -> "👑"
        }
    }
}