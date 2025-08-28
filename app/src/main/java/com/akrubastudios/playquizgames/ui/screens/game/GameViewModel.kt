package com.akrubastudios.playquizgames.ui.screens.game

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.AdManager
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import com.akrubastudios.playquizgames.domain.GameResult
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.data.repository.GameDataRepository

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuizRepository,
    private val db: FirebaseFirestore,       // <-- AÑADE ESTO
    private val auth: FirebaseAuth,
    private val languageManager: LanguageManager,
    private val gameDataRepository: GameDataRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val levelId: String = savedStateHandle.get<String>("levelId")!!
    val countryId: String = savedStateHandle.get<String>("countryId")!!
    private val difficulty: String = savedStateHandle.get<String>("difficulty")!!
    companion object {
        private const val QUESTION_TIME_LIMIT_SECONDS = 15L // Tiempo del temporizador
    }
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    private var timerJob: Job? = null // Para poder controlar (cancelar) el temporizador
    private var isAnswerProcessing = false
    private var levelPackage: QuizLevelPackage? = null
    private var currentQuestionIndex = 0
    // ---------------------------------------------

    init {
        loadLevel()
    }

    private fun loadLevel() {
        // Lanzamos una coroutine para llamar a nuestra función suspendida
        viewModelScope.launch {
            val loadedLevel = repository.getLevel(levelId) // Llama a la nueva función suspend

            if (loadedLevel != null) {
                levelPackage = loadedLevel // Guardamos el nivel cargado
                val firstQuestion = loadedLevel.questions[currentQuestionIndex]
                val lang = languageManager.languageStateFlow.value

                // 1. Determinamos la respuesta correcta UNA SOLA VEZ.
                val correctAnswerForUi = if (lang == "es") firstQuestion.correctAnswer_es else firstQuestion.correctAnswer_en

                // 2. Generamos las pistas basadas en esa respuesta.
                val hints = generateHintLetters(correctAnswerForUi)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentQuestion = firstQuestion,
                        currentCorrectAnswer = correctAnswerForUi,
                        questionText = if (lang == "es") firstQuestion.questionText_es else firstQuestion.questionText_en,
                        totalQuestions = loadedLevel.questions.size,
                        questionNumber = currentQuestionIndex + 1,
                        generatedHintLetters = hints,
                        difficulty = difficulty,
                        questionResults = List(loadedLevel.questions.size) { null } // Crear lista del tamaño correcto
                    )
                }
                startTimer()
            } else {
                Log.d("GameViewModel", "Error al cargar el nivel desde Firestore.")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun generateHintLetters(localizedCorrectAnswer: String): String {
        // Obtenemos todos los caracteres de la respuesta.
        val allCharsInAnswer = localizedCorrectAnswer.uppercase().toList()

        // Para el banco de letras, SIEMPRE trabajaremos solo con las letras, sin espacios.
        val lettersOnlyFromAnswer = allCharsInAnswer.filter { it.isLetter() }

        if (difficulty == "principiante") {
            // Modo Anagrama: Barajado garantizado, usando solo las letras.
            val originalString = lettersOnlyFromAnswer.joinToString("")
            var shuffledString: String

            do {
                shuffledString = lettersOnlyFromAnswer.shuffled().joinToString("")
            } while (shuffledString == originalString && originalString.length > 1)
            // La condición "originalString.length > 1" evita un bucle infinito
            // para respuestas de una sola letra, donde barajar no tiene efecto.
            return shuffledString

        } else { // Modo Difícil
            // Se utiliza tu lógica original, pero con un ajuste en el conteo de letras.
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val totalLettersInBank = 15

            // El conteo se hace sobre la lista de solo letras.
            val letterCountInAnswer = lettersOnlyFromAnswer.size
            val decoyLettersCount = (totalLettersInBank - letterCountInAnswer).coerceAtLeast(4)

            // Obtenemos solo las letras de la respuesta para el filtro del alfabeto.
            val randomLetters = alphabet.toList()
                .filter { !lettersOnlyFromAnswer.contains(it) } // Filtra usando solo letras
                .shuffled()
                .take(decoyLettersCount)

            // La cadena final se construye con las letras de la respuesta y los señuelos.
            return (lettersOnlyFromAnswer + randomLetters).shuffled().joinToString("")
        }
    }

    fun onLetterClick(letter: Char, index: Int) {
        if (difficulty == "principiante" && uiState.value.usedLetterIndices.contains(index)) {
            return
        }

        // --- LÓGICA CORREGIDA ---
        // Ahora leemos la longitud de la respuesta correcta que guardamos en el estado.
        val requiredLength = uiState.value.currentCorrectAnswer.count { it.isLetter() }

        // 2. Comprobamos contra la longitud actual de la respuesta del usuario (que no tiene espacios).
        if (uiState.value.userAnswer.length < requiredLength) {
            val newAnswer = uiState.value.userAnswer + letter
            val newUsedIndices = if (difficulty == "principiante") {
                uiState.value.usedLetterIndices + index
            } else {
                uiState.value.usedLetterIndices
            }

            _uiState.update { currentState ->
                currentState.copy(
                    userAnswer = newAnswer,
                    usedLetterIndices = newUsedIndices
                )
            }

            // 3. La condición de parada ahora compara longitudes de solo letras.
            if (newAnswer.length == requiredLength) {
                checkAnswer()
            }
        }
    }

    fun clearUserAnswer() {
        _uiState.update { currentState ->
            currentState.copy(
                userAnswer = "",
                usedLetterIndices = emptySet() // Reseteamos los índices usados.
            )
        }
    }

    private fun checkAnswer() {
        if (isAnswerProcessing) return
        // Cerramos el cerrojo para que nadie más pueda entrar.
        isAnswerProcessing = true

        stopTimer() // Detiene el temporizador inmediatamente
        viewModelScope.launch {
            if (uiState.value.remainingTime == 0L) {
                _uiState.update { it.copy(timerExplosion = true) }
                delay(500L) // Tiempo para que onLetterClick termine de actualizar userAnswer
            }
            val state = uiState.value

            // --- LOGS CRÍTICOS PARA DIAGNÓSTICO ---
            Log.d("GameViewModel_Debug", "--- INICIANDO CHECKANSWER ---")
            Log.d("GameViewModel_Debug", "Respuesta del Usuario: '${state.userAnswer.lowercase()}'")
            Log.d("GameViewModel_Debug", "Respuesta Correcta (actual): '${state.currentCorrectAnswer}'")
            Log.d("GameViewModel_Debug", "Respuestas Válidas: ${state.currentQuestion?.validAnswers}")
            // ------------------------------------


            val normalizedUserAnswer = state.userAnswer.replace(" ", "").lowercase()
            val lang = languageManager.languageStateFlow.value

            // Obtenemos la LISTA de respuestas válidas para el idioma actual.
            val validAnswersForLang = state.currentQuestion?.validAnswers?.get(lang) ?: emptyList()

            // Verificamos si la respuesta del usuario está en la lista correcta.
            val isCorrect = validAnswersForLang.any { validAnswer ->
                validAnswer.replace(" ", "").lowercase() == normalizedUserAnswer
            }

            // --- LOG DEL RESULTADO DE LA COMPARACIÓN ---
            Log.d("GameViewModel_Debug", "Resultado de la verificación (isCorrect): $isCorrect")
            // AGREGA ESTOS LOGS AQUÍ:
            Log.d("GameViewModel_Debug", "remainingTime cuando verifica: ${uiState.value.remainingTime}")
            Log.d("GameViewModel_Debug", "basePoints que se calculará: ${1000 + (uiState.value.remainingTime * 100).toInt()}")
            // -----------------------------------------

            if (isCorrect) {
                Log.d("GameViewModel_Debug", "✅ ¡Respuesta Correcta! Calculando puntos...")

                // Activar animación correcta
                _uiState.update { it.copy(showCorrectAnimation = true) }

                // Calcula el puntaje base con el bono de tiempo.
                val basePoints = 1000 + (uiState.value.remainingTime * 100).toInt()

                // Aplica el bono de dificultad si corresponde.
                val pointsWon = if (difficulty == "dificil") {
                    Log.d("GameViewModel_Debug", "Bono de dificultad aplicado.")
                    (basePoints * 1.5).toInt()
                } else {
                    basePoints
                }

                _uiState.update {
                    it.copy(
                        score = it.score + pointsWon,
                        correctAnswersCount = it.correctAnswersCount + 1
                    )
                }

                Log.d("GameViewModel_Debug", "Puntos ganados: $pointsWon. Nuevo puntaje: ${uiState.value.score}")
            } else {
                Log.d("GameViewModel_Debug", "❌ Respuesta Incorrecta.")

                // Activar animación incorrecta
                _uiState.update { it.copy(showIncorrectAnimation = true) }
            }

            // Actualizar el resultado de la pregunta actual
            val updatedResults = uiState.value.questionResults.toMutableList()
            updatedResults[currentQuestionIndex] = isCorrect
            _uiState.update { it.copy(questionResults = updatedResults) }

            delay(1000L)
            moveToNextQuestion()
            Log.d("GameViewModel_Debug", "--- FIN CHECKANSWER ---")
        }
    }

    private fun moveToNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < (levelPackage?.questions?.size ?: 0)) {
            val nextQuestion = levelPackage!!.questions[currentQuestionIndex]
            val lang = languageManager.languageStateFlow.value

            val correctAnswerForUi = if (lang == "es") nextQuestion.correctAnswer_es else nextQuestion.correctAnswer_en
            val hints = generateHintLetters(correctAnswerForUi)

            _uiState.update {
                it.copy(
                    currentQuestion = nextQuestion,
                    currentCorrectAnswer = correctAnswerForUi,
                    questionText = if (lang == "es") nextQuestion.questionText_es else nextQuestion.questionText_en,
                    questionNumber = currentQuestionIndex + 1,
                    userAnswer = "",
                    generatedHintLetters = hints,
                    difficulty = difficulty,
                    usedLetterIndices = emptySet(),
                    showCorrectAnimation = false,
                    showIncorrectAnimation = false,
                    timerExplosion = false,
                )
            }
            isAnswerProcessing = false
            startTimer() // Inicia el temporizador para la nueva pregunta
        } else {
            Log.d("GameViewModel", "Juego Terminado. Puntaje final: ${uiState.value.score}")

            // Cuando el juego termina, lanzamos una nueva corrutina
            // para manejar los cálculos asíncronos de recompensas.
            viewModelScope.launch {
                Log.d("GameViewModel", "Juego Terminado. Calculando recompensas...")

                // 1. Calcular el porcentaje de aciertos
                val correctAnswers = uiState.value.correctAnswersCount
                val totalQuestions = uiState.value.totalQuestions
                val accuracy = if (totalQuestions > 0) {
                    (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100
                } else {
                    0f
                }

                // 2. Aplicar la regla para calcular las estrellas ganadas
                val starsEarned = when {
                    accuracy >= 100f -> 3
                    accuracy >= 80f -> 2
                    accuracy >= 50f -> 1
                    else -> 0
                }

                val pcGained = calculatePcWon(starsEarned)

                // 3. Preparamos el objeto GameResult con toda la información
                val result = GameResult(
                    score = uiState.value.score,
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    starsEarned = starsEarned,
                    pcGained = pcGained
                )
                _gameResult.value = result

                sendScoreRequestToFirebase(result)

                isAnswerProcessing = false
            }
        }
    }

    // --- AÑADE ESTA NUEVA FUNCIÓN COMPLETA ---
    private fun sendScoreRequestToFirebase(result: GameResult) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("GameViewModel", "Error: no se puede enviar puntaje, usuario nulo.")
            return
        }

        viewModelScope.launch {
            try {
                val scoreRequest = hashMapOf(
                    "userId" to uid,
                    "score" to result.score,
                    "starsEarned" to result.starsEarned,
                    "levelId" to levelId,
                    "countryId" to countryId,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("score_requests").add(scoreRequest).await()
                Log.d("GameViewModel", "✅ Petición de puntaje enviada con éxito.")

            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error al enviar petición de puntaje.", e)
            }
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
            if (!isAnswerProcessing) {
                checkAnswer()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun levelIdForNav(): String {
        return levelId
    }

    fun difficultyForNav(): String {
        return difficulty
    }
    // CUIDADO: Esta lógica duplica el cálculo de PC de la Cloud Function 'processScoreRequest'.
    // Si se cambia la regla de negocio, actualizar en AMBOS sitios.
    private suspend fun calculatePcWon(newStars: Int): Int {
        if (countryId == "freemode") return 0

        // Transcripción de: const currentGlobalRecord = completionData.starsEarned || 0;
        val globalRecordStars = gameDataRepository.getUserGlobalLevelProgress(levelId)

        // Transcripción de: if (starsEarned <= currentGlobalRecord) { return; }
        if (newStars <= globalRecordStars) {
            return 0 // No se ganan PC si no se supera el récord global.
        }

        // Transcripción de: const starsInThisCountry = countryProgressData.starsEarnedInCountry || 0;
        val starsAlreadyRewardedInCountry = gameDataRepository.getUserLevelCountryProgress(levelId, countryId)

        // Transcripción de: const rewards = [5000, 10000, 15000];
        val rewards = listOf(5000, 10000, 15000)
        var pcGained = 0

        // Transcripción de: const starsGained = Math.min(starsEarned - currentGlobalRecord, 3 - starsInThisCountry);
        // Calcula cuántas estrellas NUEVAS son elegibles para recompensa.
        val starsGained = minOf(newStars - globalRecordStars, 3 - starsAlreadyRewardedInCountry)

        // Transcripción del bucle 'for' y su lógica interna.
        if (starsGained > 0) {
            for (i in 0 until starsGained) {
                // const starPosition = starsInThisCountry + i + 1;
                val starPositionInCountry = starsAlreadyRewardedInCountry + i + 1
                if (starPositionInCountry <= 3) {
                    // pcGained += rewards[starPosition - 1];
                    pcGained += rewards[starPositionInCountry - 1]
                }
            }
        }

        return pcGained
    }
}