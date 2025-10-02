package com.akrubastudios.playquizgames.ui.screens.game

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
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
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.SoundEffect
import com.akrubastudios.playquizgames.core.SoundManager
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Question
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import coil.request.CachePolicy
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuizRepository,
    private val db: FirebaseFirestore,       // <-- AÑADE ESTO
    private val auth: FirebaseAuth,
    private val languageManager: LanguageManager,
    private val gameDataRepository: GameDataRepository,
    val musicManager: MusicManager,
    private val soundManager: SoundManager,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    val imageLoader: ImageLoader
) : ViewModel() {
    val levelId: String = savedStateHandle.get<String>("levelId")!!
    val countryId: String = savedStateHandle.get<String>("countryId")!!
    val difficulty: String = savedStateHandle.get<String>("difficulty")!!
    val origin: String = savedStateHandle.get<String>("origin")!!
    private val categoryId: String by lazy { levelId.split('_').getOrNull(1) ?: "" }
    private val continentId: String by lazy { levelId.split('_').getOrNull(0) ?: "" }
    companion object {
        private const val QUESTION_TIME_LIMIT_SECONDS = 15L // Tiempo del temporizador
        private const val FUN_FACT_TIME_PENALTY_SECONDS = 4L // Segundo de penalización al usar un Fun fact
        const val HELP_REVEAL_LETTER_COST_INITIAL = 2
        const val HELP_REVEAL_LETTER_COST_INCREMENT = 1
        const val HELP_EXTRA_TIME_SECONDS = 15
        const val HELP_EXTRA_TIME_COST = 4
    }
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val _gameResult = MutableStateFlow<GameResult?>(null)
    val gameResult: StateFlow<GameResult?> = _gameResult.asStateFlow()
    private var timerJob: Job? = null // Para poder controlar (cancelar) el temporizador
    private var isAnswerProcessing = false
    private var levelPackage: QuizLevelPackage? = null
    private var shuffledQuestions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private var previousBestStars: Int = 0
        private var reshuffleJob: Job? = null

    /**
     * Pre-carga una única imagen de forma síncrona en un hilo de fondo.
     * Devuelve true si la carga fue exitosa (desde red o disco).
     */
    private suspend fun precacheSingleImage(imageUrl: String): Boolean = withContext(Dispatchers.IO) {
        if (imageUrl.isBlank()) return@withContext false

        Log.d("GameViewModel_Precache", "ANTES de execute: $imageUrl")

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .diskCachePolicy(CachePolicy.ENABLED)  // ← AÑADIR
            .memoryCachePolicy(CachePolicy.ENABLED) // ← AÑADIR
            .build()

        val result = imageLoader.execute(request)
        if (result is coil.request.SuccessResult) {
            Log.d("GameViewModel_Precache", "DESPUES de execute: DataSource=${result.dataSource}")
            return@withContext true
        }

        return@withContext false
    }
    /**
     * Pre-carga múltiples imágenes en paralelo.
     * Devuelve cuando TODAS han terminado de cargar.
     */
    private suspend fun precacheImagesParallel(imageUrls: List<String>) = coroutineScope {
        if (imageUrls.isEmpty()) return@coroutineScope

        Log.d("GameViewModel_Precache", "🚀 Iniciando precarga paralela de ${imageUrls.size} imágenes")
        val startTime = System.currentTimeMillis()

        val jobs = imageUrls.map { url ->
            async(Dispatchers.IO) {
                precacheSingleImage(url)
            }
        }

        jobs.awaitAll()

        val totalTime = System.currentTimeMillis() - startTime
        Log.d("GameViewModel_Precache", "✅ Precarga paralela completada en ${totalTime}ms")
    }

    init {
        loadLevel()
    }

    private fun loadLevel() {
        // Lanzamos una coroutine para llamar a nuestra función suspendida
        // Usamos Dispatchers.IO para que la carga de red no bloquee el hilo principal
        viewModelScope.launch(Dispatchers.IO) {
            resetRoundState()

            val levelRequest = async { repository.getLevel(levelId) }
            val bestScoreRequest = async { gameDataRepository.getUserGlobalLevelProgress(levelId) }

            val loadedLevel = levelRequest.await()
            previousBestStars = bestScoreRequest.await()

            if (loadedLevel != null) {
                levelPackage = loadedLevel // Guardamos el nivel cargado
                shuffledQuestions = loadedLevel.questions.shuffled()

                if (shuffledQuestions.isNotEmpty()) {
                    // Mostrar loading de precarga
                    _uiState.update { it.copy(isPreloadingImages = true) }

                    Log.d("GameViewModel_Precache", "🚀 Precargando TODAS las ${shuffledQuestions.size} imágenes...")
                    val startTime = System.currentTimeMillis()

                    // Precarga TODAS las imágenes en paralelo
                    val allImageUrls = shuffledQuestions
                        .map { it.imageUrl }
                        .filter { it.isNotBlank() }

                    precacheImagesParallel(allImageUrls)

                    val totalTime = System.currentTimeMillis() - startTime
                    Log.d("GameViewModel_Precache", "✅ TODAS las imágenes precargadas en ${totalTime}ms")

                    // Ocultar loading
                    _uiState.update { it.copy(isPreloadingImages = false) }
                }

                val firstQuestion = shuffledQuestions[currentQuestionIndex]
                val lang = languageManager.languageStateFlow.value

                // Leemos los datos del usuario para ver si ha dominado este nivel.
                val userData = gameDataRepository.getUserData()
                val areFactsUnlocked = userData?.masteredLevelIds?.contains(levelId) ?: false

                // 1. Determinamos la respuesta correcta UNA SOLA VEZ.
                val correctAnswerForUi = if (lang == "es") firstQuestion.correctAnswer_es else firstQuestion.correctAnswer_en

                // 2. Generamos las pistas basadas en esa respuesta.
                val hints = generateHintLetters(correctAnswerForUi)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentQuestion = firstQuestion,
                        totalQuestions = shuffledQuestions.size,
                        currentCorrectAnswer = correctAnswerForUi,
                        questionText = if (lang == "es") firstQuestion.questionText_es else firstQuestion.questionText_en,
                        questionNumber = currentQuestionIndex + 1,
                        generatedHintLetters = hints,
                        difficulty = difficulty,
                        questionResults = List(loadedLevel.questions.size) { null }, // Crear lista del tamaño correcto
                        areFunFactsUnlockedForLevel = areFactsUnlocked,
                        hasSeenFunFactTutorial = userData?.hasSeenFunFactTutorial ?: true,
                        currentGems = userData?.gems ?: 0
                    )
                }
                startTimer()
                startLetterReshuffleTimer()
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
        soundManager.playSound(SoundEffect.LETTER_CLICK)
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
        if (uiState.value.userAnswer.isNotEmpty()) {
            soundManager.playSound(SoundEffect.CLEAR_ANSWER)
        }
        viewModelScope.launch {
            // Activar animación de clear
            _uiState.update { it.copy(showClearAnimation = true) }

            // Esperar un poco para que se vea la animación
            delay(300L)

            // Limpiar y resetear animación
            _uiState.update { currentState ->
                currentState.copy(
                    userAnswer = "",
                    usedLetterIndices = emptySet(),
                    showClearAnimation = false
                )
            }
        }
    }

    private fun checkAnswer() {
        if (isAnswerProcessing) return
        // Cerramos el cerrojo para que nadie más pueda entrar.
        isAnswerProcessing = true

        stopTimer() // Detiene el temporizador inmediatamente
        reshuffleJob?.cancel()
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
                soundManager.playSound(SoundEffect.CORRECT_ANSWER)
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
                soundManager.playSound(SoundEffect.INCORRECT_ANSWER)
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
        if (currentQuestionIndex < shuffledQuestions.size) {
            viewModelScope.launch {

                // Activar transición fade
                _uiState.update { it.copy(questionTransition = true) }
                delay(150L) // Esperar a que termine el fade out

                val nextQuestion = shuffledQuestions[currentQuestionIndex]
                val lang = languageManager.languageStateFlow.value

                val correctAnswerForUi =
                    if (lang == "es") nextQuestion.correctAnswer_es else nextQuestion.correctAnswer_en
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
                        showClearAnimation = false,
                        questionTransition = false,
                        revealLetterUses = 0,
                        revealedLetterPositions = emptySet(),
                        isExtraTimeUsed = false
                    )
                }
                isAnswerProcessing = false
                startTimer() // Inicia el temporizador para la nueva pregunta
                startLetterReshuffleTimer()
            }
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

                val gemsGained = calculateGemsWon(starsEarned)

                // 3. Preparamos el objeto GameResult con toda la información
                val result = GameResult(
                    score = uiState.value.score,
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    starsEarned = starsEarned,
                    pcGained = pcGained,
                    gemsGained = gemsGained,
                    previousBestStars = previousBestStars
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
                    "timestamp" to System.currentTimeMillis(),
                    "difficulty" to difficulty
                )

                db.collection("score_requests").add(scoreRequest).await()
                Log.d("GameViewModel", "✅ Petición de puntaje enviada con éxito.")

            } catch (e: Exception) {
                Log.e("GameViewModel", "❌ Error al enviar petición de puntaje.", e)
            }
        }
    }

    private fun startTimer(startTime: Long = QUESTION_TIME_LIMIT_SECONDS) {
        timerJob?.cancel() // Cancela cualquier temporizador anterior
        timerJob = viewModelScope.launch {
            var timeLeft = startTime
            _uiState.update { it.copy(remainingTime = timeLeft) }

            while (timeLeft > 0) {
                delay(1000L) // Espera 1 segundo
                timeLeft--
                _uiState.update { it.copy(remainingTime = timeLeft) }
                if (timeLeft in 1..3) {
                    soundManager.playSound(SoundEffect.TIMER_TICK)
                }
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

    private fun startLetterReshuffleTimer() {
        // 1. Cancela cualquier temporizador de reordenamiento anterior para evitar duplicados.
        reshuffleJob?.cancel()

        // 2. La lógica solo se aplica si estamos en modo "dificil".
        if (difficulty == "dificil") {
            // 3. Lanza una nueva corutina y guarda la referencia en nuestra variable 'reshuffleJob'.
            reshuffleJob = viewModelScope.launch {
                delay(5000L) // Espera 5 segundos.

                // 4. LA REGLA DE ORO: Después de la espera, comprueba si el usuario YA ha empezado a escribir.
                if (_uiState.value.userAnswer.isEmpty()) {
                    Log.d("GameViewModel_Reshuffle", "5 segundos pasaron, el usuario no ha respondido. ¡Reordenando letras!")

                    // 5. Si la respuesta está vacía, vuelve a generar las letras y actualiza el estado.
                    val newHints = generateHintLetters(_uiState.value.currentCorrectAnswer)
                    _uiState.update { it.copy(generatedHintLetters = newHints) }
                } else {
                    Log.d("GameViewModel_Reshuffle", "5 segundos pasaron, pero el usuario ya empezó. No se reordena.")
                }
            }
        }
    }

    fun levelIdForNav(): String {
        return levelId
    }

    fun difficultyForNav(): String {
        return difficulty
    }
    fun categoryIdForNav(): String {
        return categoryId
    }

    fun continentIdForNav(): String {
        return continentId
    }

    fun originForNav(): String {
        return origin
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

    // CUIDADO: Esta lógica duplica el cálculo de Gemas de la Cloud Function.
    // Si se cambia la regla de negocio, actualizar en AMBOS sitios.
    private suspend fun calculateGemsWon(newStars: Int): Int {
        if (newStars < 2) return 0

        val completionData = gameDataRepository.getLevelCompletion(levelId)
        val gemsAlreadyEarned = completionData?.gemsEarned ?: 0

        // Calculamos la recompensa MÁXIMA potencial para este resultado.
        var maxPotentialGems = 0
        if (newStars >= 2) {
            maxPotentialGems = if (difficulty == "dificil") 3 else 2
        }

        // Otorgamos solo la diferencia neta.
        var gemsToAdd = 0
        if (maxPotentialGems > gemsAlreadyEarned) {
            gemsToAdd = maxPotentialGems - gemsAlreadyEarned
        }

        return gemsToAdd
    }

    /**
     * Se llama cuando el usuario hace clic en el botón de Fun Fact.
     */
    fun onFunFactClicked() {
        val state = uiState.value

        // Comprobamos si el botón está lógicamente activo
        if (!state.isFunFactUsedInRound || state.areFunFactsUnlockedForLevel) {
            timerJob?.cancel() // Pausamos el tiempo en ambos casos

            if (!state.hasSeenFunFactTutorial) {
                // CASO 1: Es la primera vez que el usuario hace clic. Mostramos el tutorial.
                _uiState.update { it.copy(showFunFactTutorialDialog = true) }
            } else {
                // CASO 2: Ya ha visto el tutorial. Mostramos el Fun Fact directamente.
                val lang = languageManager.languageStateFlow.value
                val funFactText = if (lang == "es") {
                    state.currentQuestion?.fun_fact_es
                } else {
                    state.currentQuestion?.fun_fact_en
                }
                _uiState.update { it.copy(showFunFactDialog = true, currentFunFact = funFactText ?: "") }
            }
        }
    }

    /**
     * Se llama cuando el usuario cierra el diálogo del Fun Fact.
     */
    fun onFunFactDialogDismissed() {
        // Oculta el diálogo y marca la pista como usada.
        _uiState.update {
            it.copy(
                showFunFactDialog = false,
                isFunFactUsedInRound = true
            )
        }

        // Aplica la penalización de tiempo.
        val newTime = (uiState.value.remainingTime - FUN_FACT_TIME_PENALTY_SECONDS).coerceAtLeast(0L)
        _uiState.update { it.copy(remainingTime = newTime) }

        // Si el tiempo llega a 0 por la penalización, termina la pregunta.
        if (newTime == 0L) {
            checkAnswer()
        } else {
            // Si no, reanuda el temporizador con el tiempo restante.
            startTimer(startTime = newTime)
        }
    }
    fun funFactTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        // Cierra el diálogo del tutorial en la UI y marca el tutorial como "visto" en el estado local.
        _uiState.update { it.copy(showFunFactTutorialDialog = false, hasSeenFunFactTutorial = true) }

        // Reanuda el temporizador sin penalización de tiempo.
        startTimer(startTime = uiState.value.remainingTime)

        // Actualiza la bandera en Firestore en segundo plano para que no se vuelva a mostrar.
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("hasSeenFunFactTutorial", true)
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error al actualizar hasSeenFunFactTutorial", e)
            }
        }
    }

    fun openHelpsSheet() {
        _uiState.update { it.copy(showHelpsSheet = true) }
    }

    fun closeHelpsSheet() {
        _uiState.update { it.copy(showHelpsSheet = false) }
    }

    fun useRevealLetterHelp() {
        val state = _uiState.value
        val cost = HELP_REVEAL_LETTER_COST_INITIAL + (state.revealLetterUses * HELP_REVEAL_LETTER_COST_INCREMENT)
        val lettersToRevealCount = state.currentCorrectAnswer.replace(" ", "").length

        if (state.currentGems < cost || state.userAnswer.length >= lettersToRevealCount) {
            return
        }

        _uiState.update { it.copy(isProcessingHelp = true) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("GameViewModel", "Error: Usuario nulo al usar Revelar Letra.")
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
                        Log.d("GameViewModel", "✅ Petición para 'Revelar Letra' enviada.")

                        val correctAnswerNoSpaces = state.currentCorrectAnswer.replace(" ", "")
                        val userAnswer = state.userAnswer
                        val hintLetters = state.generatedHintLetters
                        val nextCorrectLetter = correctAnswerNoSpaces[userAnswer.length]

                        val letterIndexInBank = hintLetters.withIndex()
                            .find { (index, char) ->
                                char.uppercaseChar() == nextCorrectLetter.uppercaseChar() &&
                                        !state.usedLetterIndices.contains(index)
                            }?.index

                        val finalUsedIndices = if (letterIndexInBank != null) {
                            state.usedLetterIndices + letterIndexInBank
                        } else {
                            state.usedLetterIndices
                        }

                        val newAnswer = userAnswer + nextCorrectLetter
                        val newRevealedPositions = state.revealedLetterPositions + userAnswer.length

                        _uiState.update {
                            it.copy(
                                userAnswer = newAnswer,
                                usedLetterIndices = finalUsedIndices,
                                revealedLetterPositions = newRevealedPositions,
                                revealLetterUses = it.revealLetterUses + 1,
                                currentGems = it.currentGems - cost
                            )
                        }

                        if (newAnswer.length == correctAnswerNoSpaces.length) {
                            checkAnswer()
                        }

                    } else {
                        Log.e("GameViewModel", "❌ Error al crear gem_spend_request.", task.exception)
                    }
                    _uiState.update { it.copy(isProcessingHelp = false) }
                    closeHelpsSheet()
                }
            }
    }

    fun useExtraTimeHelp() {
        val cost = HELP_EXTRA_TIME_COST
        val state = _uiState.value

        if (state.currentGems < cost || state.isExtraTimeUsed) {
            return
        }

        _uiState.update { it.copy(isProcessingHelp = true) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("GameViewModel", "Error: Usuario nulo al usar Tiempo Extra.")
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
                        Log.d("GameViewModel", "✅ Petición para 'Tiempo Extra' enviada.")

                        val newTotalTime = _uiState.value.remainingTime + HELP_EXTRA_TIME_SECONDS

                        _uiState.update {
                            it.copy(
                                isExtraTimeUsed = true,
                                currentGems = it.currentGems - cost
                            )
                        }

                        // Reinicia el temporizador principal con el nuevo tiempo
                        startTimer(startTime = newTotalTime)

                    } else {
                        Log.e("GameViewModel", "❌ Error al crear gem_spend_request.", task.exception)
                    }

                    _uiState.update { it.copy(isProcessingHelp = false) }
                    closeHelpsSheet()
                }
            }
    }

    /**
     * Reinicia el estado de la ronda al pasar a la siguiente pregunta o al final del juego.
     * Este cambio es para la siguiente versión de startTimer().
     */
    private fun resetRoundState() {
        _uiState.update { it.copy(isFunFactUsedInRound = false) }
    }
}