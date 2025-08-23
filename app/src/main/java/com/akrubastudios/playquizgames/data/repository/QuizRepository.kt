package com.akrubastudios.playquizgames.data.repository

import android.content.Context
import android.util.Log
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

// NOTA: AHORA DEPENDE DE FIRESTORE Y CONTEXT
class QuizRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    private val json = Json { ignoreUnknownKeys = true }

    // La función sigue siendo suspend, pero aún usa el archivo local.
    // Esto es para la transición. En el siguiente paso la cambiaremos.
    suspend fun getLevel(levelId: String): QuizLevelPackage? {
        return try {
            val document = db.collection("quizzes").document(levelId).get().await()
            val levelPackage = document.toObject(QuizLevelPackage::class.java)

            // --- AÑADE ESTAS LÍNEAS ---
            if (validateLevelPackage(levelPackage, levelId)) {
                levelPackage // Es válido, lo devolvemos
            } else {
                null // No es válido, devolvemos null
            }
            // -------------------------

        } catch (e: Exception) {
            Log.e("DataValidation", "Nivel '$levelId': Crash al convertir el documento.", e)
            e.printStackTrace()
            null
        }
    }

    private fun validateLevelPackage(level: QuizLevelPackage?, levelId: String): Boolean {
        val TAG = "DataValidation"

        if (level == null) {
            Log.e(TAG, "Nivel '$levelId': El documento no existe o no se pudo convertir.")
            return false
        }

        var isValid = true

        if (level.levelId.isBlank()) {
            Log.w(TAG, "Nivel '$levelId': El campo 'levelId' está vacío.")
            isValid = false
        }
        if (level.levelName.isEmpty()) {
            Log.w(TAG, "Nivel '$levelId': El mapa 'levelName' está vacío.")
            isValid = false
        }
        if (level.questions.isEmpty()) {
            Log.e(TAG, "Nivel '$levelId': El array 'questions' está vacío. ¡No hay preguntas!")
            isValid = false
        }

        level.questions.forEachIndexed { index, question ->
            if (question.id.isBlank()) {
                Log.w(TAG, "Nivel '$levelId', Pregunta #${index + 1}: El campo 'id' está vacío.")
                isValid = false
            }
            if (question.correctAnswer_es.isBlank() || question.correctAnswer_en.isBlank()) {
                Log.w(TAG, "Nivel '$levelId', Pregunta #${index + 1} ('${question.id}'): Uno de los campos 'correctAnswer' está vacío.")
                isValid = false
            }
            if (question.validAnswers.isEmpty()) {
                Log.w(TAG, "Nivel '$levelId', Pregunta #${index + 1} ('${question.id}'): El array 'validAnswers' está vacío.")
                isValid = false
            }
            if (question.questionText_es.isBlank()) { // <-- Añadimos validación para el texto
                Log.w(TAG, "Nivel '$levelId', Pregunta #${index + 1} ('${question.id}'): El campo 'questionText_es' está vacío.")
                isValid = false
            }
        }

        if (isValid) {
            Log.d(TAG, "Nivel '$levelId': Validación de datos exitosa.")
        }
        return isValid
    }
}