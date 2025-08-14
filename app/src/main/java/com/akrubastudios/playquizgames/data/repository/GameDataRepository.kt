package com.akrubastudios.playquizgames.data.repository

import android.util.Log
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.RankedUser
import com.akrubastudios.playquizgames.domain.User
import com.akrubastudios.playquizgames.domain.UserCountryProgress
import com.akrubastudios.playquizgames.domain.UserLevelCompletion
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.LevelMetadata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GameDataRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    // Esta función obtiene TODOS los documentos de la colección 'countries'
    suspend fun getCountryList(): List<Country> {
        return try {
            val snapshot = db.collection("countries").get().await()
            snapshot.toObjects(Country::class.java) // Convierte todos los documentos a una lista de objetos Country
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Devuelve una lista vacía si hay un error
        }
    }

    // Obtiene un único documento de la colección 'countries'
    suspend fun getCountry(countryId: String): Country? {
        return try {
            db.collection("countries").document(countryId).get().await()
                .toObject(Country::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Obtiene el progreso del usuario actual para un país específico
    suspend fun getUserProgressForCountry(countryId: String): UserCountryProgress? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val progressDocId = "${uid}_${countryId}"
        return try {
            db.collection("user_country_progress").document(progressDocId).get().await()
                .toObject(UserCountryProgress::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Obtiene el documento del usuario actual desde la colección 'users'
    suspend fun getUserData(): User? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return try {
            db.collection("users").document(uid).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    @Suppress("UNCHECKED_CAST")
    suspend fun getRanking(): List<RankedUser> {
        try {
            val result = functions.getHttpsCallable("getGlobalRanking").call().await()
            val data = result.data as? List<Map<String, Any>> // Usa un cast seguro

            // Si data es null o no es una lista, devuelve una lista vacía
            if (data == null) {
                Log.e("GameDataRepository", "Los datos del ranking son nulos o no son una lista.")
                return emptyList()
            }

            // Mapea los datos de forma segura
            val rankedList = data.mapNotNull { userMap ->
                try {
                    RankedUser(
                        // Convierte cualquier tipo de número a Long y luego a Int
                        rank = (userMap["rank"] as? Number)?.toInt() ?: 0,
                        displayName = userMap["displayName"] as? String ?: "Error de Nombre",
                        totalXp = (userMap["totalXp"] as? Number)?.toLong() ?: 0L,
                        photoUrl = userMap["photoUrl"] as? String
                    )
                } catch (e: Exception) {
                    Log.e("GameDataRepository", "Error al parsear un usuario del ranking: $userMap", e)
                    null // Si un usuario falla, lo saltamos en lugar de romper toda la lista
                }
            }
            return rankedList
        } catch (e: Exception) {
            Log.e("GameDataRepository", "Error al llamar a la función getGlobalRanking.", e)
            e.printStackTrace()
            return emptyList()
        }
    }

    suspend fun getCategoryList(): List<Category> {
        return try {
            val snapshot = db.collection("categories").get().await()
            snapshot.toObjects(Category::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllLevels(): List<LevelMetadata> {
        return try {
            val snapshot = db.collection("quizzes").get().await()
            val levels = snapshot.toObjects(LevelMetadata::class.java)
            Log.d("Repo_Debug", "getAllLevels: Se encontraron ${levels.size} niveles en la colección 'quizzes'.")
            return levels
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Obtiene TODOS los registros de completado del usuario actual de una vez
    suspend fun getAllLevelCompletionData(): List<UserLevelCompletion> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("user_level_completion").whereEqualTo("userId", uid).get().await()
            val completions = snapshot.toObjects(UserLevelCompletion::class.java)
            Log.d("Repo_Debug", "getAllLevelCompletionData: Se encontraron ${completions.size} registros de completado para el usuario.")
            completions.forEach { Log.d("Repo_Debug", "  - Progreso: ${it.levelId} tiene ${it.starsEarned} estrellas.") }
            return completions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Obtiene los datos de una sola categoría (para el título)
    suspend fun getCategory(categoryId: String): Category? {
        return try {
            db.collection("categories").document(categoryId).get().await()
                .toObject(Category::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
