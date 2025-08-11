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
    private val db: FirebaseFirestore,
    private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true }

    // La función sigue siendo suspend, pero aún usa el archivo local.
    // Esto es para la transición. En el siguiente paso la cambiaremos.
    suspend fun getLevel(levelId: String): QuizLevelPackage? {
        return try {
            // Apuntamos a la colección 'quizzes' y al documento específico
            val document = db.collection("quizzes").document(levelId).get().await()

            // Usamos la utilidad de Firebase KTX para convertir el documento
            // directamente en nuestro objeto QuizLevelPackage.
            // Ahora que las data classes tienen valores por defecto, esto debería funcionar.
            document.toObject(QuizLevelPackage::class.java)
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error al convertir el documento de Firestore.", e)
            e.printStackTrace()
            null
        }
    }
}