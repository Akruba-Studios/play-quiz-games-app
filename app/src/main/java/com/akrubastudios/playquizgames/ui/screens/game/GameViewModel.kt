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

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuizRepository,
    private val db: FirebaseFirestore,       // <-- AÑADE ESTO
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val levelId: String = savedStateHandle.get<String>("levelId")!!
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
            val loadedLevel = repository.getLevel(levelId) // Llama a la nueva función suspend

            if (loadedLevel != null) {
                levelPackage = loadedLevel // Guardamos el nivel cargado
                val firstQuestion = loadedLevel.questions[currentQuestionIndex]
                val hints = generateHintLetters(firstQuestion.correctAnswer)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentQuestion = firstQuestion,
                        questionText = firstQuestion.questionText_es,
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

            // Log para ver la respuesta del usuario mientras la construye
            Log.d("GameViewModel_Debug", "Letra '${letter}' añadida. Respuesta actual: ${uiState.value.userAnswer}")

            if (uiState.value.userAnswer.length == currentAnswerLength) {
                checkAnswer()
            }
        }
    }

    private fun checkAnswer() {
        stopTimer() // Detiene el temporizador inmediatamente
        viewModelScope.launch {
            val state = uiState.value

            // --- LOGS CRÍTICOS PARA DIAGNÓSTICO ---
            Log.d("GameViewModel_Debug", "--- INICIANDO CHECKANSWER ---")
            Log.d("GameViewModel_Debug", "Respuesta del Usuario: '${state.userAnswer.lowercase()}'")
            Log.d("GameViewModel_Debug", "Respuesta Correcta (base): '${state.currentQuestion?.correctAnswer}'")
            Log.d("GameViewModel_Debug", "Respuestas Válidas: ${state.currentQuestion?.validAnswers}")
            // ------------------------------------


            val isCorrect = state.currentQuestion?.validAnswers?.contains(state.userAnswer.lowercase()) == true

            // --- LOG DEL RESULTADO DE LA COMPARACIÓN ---
            Log.d("GameViewModel_Debug", "Resultado de la verificación (isCorrect): $isCorrect")
            // -----------------------------------------

            if (isCorrect) {
                Log.d("GameViewModel_Debug", "✅ ¡Respuesta Correcta! Calculando puntos...")
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

                Log.d("GameViewModel_Debug", "Puntos ganados: $pointsWon. Nuevo puntaje: ${uiState.value.score}")


            } else {
                Log.d("GameViewModel_Debug", "❌ Respuesta Incorrecta.")
            }

            delay(1000L)
            moveToNextQuestion()
            Log.d("GameViewModel_Debug", "--- FIN CHECKANSWER ---")
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
                    questionText = nextQuestion.questionText_es,
                    questionNumber = currentQuestionIndex + 1,
                    userAnswer = "",
                    generatedHintLetters = hints
                )
            }
            startTimer() // Inicia el temporizador para la nueva pregunta
        } else {
            Log.d("GameViewModel", "Juego Terminado. Puntaje final: ${uiState.value.score}")

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

            // 3. Preparamos el objeto GameResult con toda la información
            val result = GameResult(
                score = uiState.value.score,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                starsEarned = starsEarned
            )

            sendScoreRequestToFirebase(result)

            // 4. En lugar de llamar a la Cloud Function aquí, simplemente actualizamos
            // el estado _gameResult. La UI reaccionará a este cambio.
            // La llamada a la Cloud Function la hará la ResultScreen o su futuro ViewModel.
            _gameResult.value = result
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
                    "countryId" to "br", // Esto lo haremos dinámico después
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
            checkAnswer()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun levelIdForNav(): String {
        return levelId
    }
}