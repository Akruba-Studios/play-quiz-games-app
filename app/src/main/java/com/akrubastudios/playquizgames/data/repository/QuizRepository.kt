package com.akrubastudios.playquizgames.data.repository

import android.content.Context
import android.util.Log
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.domain.QuizLevelPackage
import com.akrubastudios.playquizgames.domain.models.CountryVisualTheme
import com.akrubastudios.playquizgames.domain.models.GuardianData
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
    /**
     * AÑADIR ESTA FUNCIÓN A LA CLASE QuizRepository EXISTENTE
     */
    suspend fun getCountryVisualTheme(countryId: String): CountryVisualTheme? {
        return try {
            val document = db.collection("countries")
                .document(countryId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data ?: return null

                // Parsear guardianData
                val guardianDataMap = data["guardianData"] as? Map<*, *>
                val guardianData = if (guardianDataMap != null) {
                    parseGuardianData(guardianDataMap)
                } else {
                    // Fallback si no existen datos de guardián
                    GuardianData(
                        name = mapOf(
                            "es" to "Guardián Ancestral",
                            "en" to "Ancestral Guardian"
                        ),
                        dialogues = createDefaultDialogues()
                    )
                }

                CountryVisualTheme(
                    countryId = countryId,
                    visualArchetype = data["visualArchetype"] as? String ?: "default",
                    accentColor = data["accentColor"] as? String ?: "#D4AF37",
                    guardianData = guardianData
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("QuizRepository", "Error loading visual theme for $countryId", e)
            null
        }
    }

    /**
     * Parsea los datos del guardián desde Firebase
     */
    private fun parseGuardianData(guardianDataMap: Map<*, *>): GuardianData {
        val nameMap = guardianDataMap["name"] as? Map<*, *>
        val dialoguesMap = guardianDataMap["dialogues"] as? Map<*, *>

        val name = mapOf(
            "es" to (nameMap?.get("es") as? String ?: "Guardián"),
            "en" to (nameMap?.get("en") as? String ?: "Guardian")
        )

        val dialogues = mutableMapOf<String, Map<String, List<String>>>()

        if (dialoguesMap != null) {
            for (phase in listOf("phase1", "phase2", "phase3")) {
                val phaseMap = dialoguesMap[phase] as? Map<*, *>
                if (phaseMap != null) {
                    val esDialogues = (phaseMap["es"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val enDialogues = (phaseMap["en"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                    dialogues[phase] = mapOf(
                        "es" to esDialogues,
                        "en" to enDialogues
                    )
                }
            }
        }

        // Si no hay diálogos, usar defaults
        if (dialogues.isEmpty()) {
            dialogues.putAll(createDefaultDialogues())
        }

        return GuardianData(name = name, dialogues = dialogues)
    }

    /**
     * Crea diálogos por defecto si no existen en Firebase
     */
    private fun createDefaultDialogues(): Map<String, Map<String, List<String>>> {
        return mapOf(
            "phase1" to mapOf(
                "es" to listOf(
                    "Bienvenido, viajero...",
                    "¿Crees que puedes vencerme?",
                    "Demuestra tu sabiduría..."
                ),
                "en" to listOf(
                    "Welcome, traveler...",
                    "Do you think you can defeat me?",
                    "Prove your wisdom..."
                )
            ),
            "phase2" to mapOf(
                "es" to listOf(
                    "¡Interesante! Pero no será suficiente...",
                    "Muéstrame más de tu poder...",
                    "La batalla apenas comienza..."
                ),
                "en" to listOf(
                    "Interesting! But it won't be enough...",
                    "Show me more of your power...",
                    "The battle has just begun..."
                )
            ),
            "phase3" to mapOf(
                "es" to listOf(
                    "¡Imposible! ¿Cómo...?",
                    "¡No me rendiré tan fácilmente!",
                    "¡Este es mi último aliento!"
                ),
                "en" to listOf(
                    "Impossible! How...?",
                    "I won't give up so easily!",
                    "This is my last breath!"
                )
            )
        )
    }
}