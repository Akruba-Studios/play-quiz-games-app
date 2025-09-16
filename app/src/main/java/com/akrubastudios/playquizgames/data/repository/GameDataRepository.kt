package com.akrubastudios.playquizgames.data.repository

import android.util.Log
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.RankedUser
import com.akrubastudios.playquizgames.domain.User
import com.akrubastudios.playquizgames.domain.UserCountryProgress
import com.akrubastudios.playquizgames.domain.UserLevelCompletion
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.LevelMetadata
import com.akrubastudios.playquizgames.domain.UserLevelCountryProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserRankRequest(
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserRankResponse(
    val rank: Int = 0,
    val totalXp: Long = 0,
    val nextPlayerXp: Long? = null,
    val error: String? = null
)

class GameDataRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    // --- INICIO DE LA NUEVA ARQUITECTURA DE DATOS DE USUARIO ---

    // 1. StateFlow privado para gestionar el estado internamente.
    private val _userStateFlow = MutableStateFlow<User?>(null)
    // 2. StateFlow público e inmutable para que los ViewModels lo observen.
    val userStateFlow: StateFlow<User?> = _userStateFlow

    // 3. Referencia a la escucha de Firestore para poder cancelarla.
    private var userListener: ListenerRegistration? = null

    // --- NUEVA ARQUITECTURA PARA PROGRESO DE PAÍS ---
    private val _userCountryProgressStateFlow = MutableStateFlow<UserCountryProgress?>(null)
    val userCountryProgressStateFlow: StateFlow<UserCountryProgress?> = _userCountryProgressStateFlow
    private var countryProgressListener: ListenerRegistration? = null
    private var currentCountryId: String? = null

    // *** FIN DE LO QUE SE AGREGA AQUÍ ***

    /**
     * Inicia la escucha en tiempo real del documento del usuario.
     * Debe llamarse cuando el usuario inicia sesión.
     */
    fun startUserDataListener() {
        // Si ya hay una escucha, la detenemos antes de crear una nueva para evitar duplicados.
        stopUserDataListener()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _userStateFlow.value = null
            return
        }

        val userRef = db.collection("users").document(uid)
        userListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("GameDataRepository", "Error en la escucha de datos del usuario.", error)
                _userStateFlow.value = null // En caso de error, reseteamos.
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                _userStateFlow.value = snapshot.toObject(User::class.java)
            } else {
                Log.w("GameDataRepository", "El documento del usuario no existe.")
                _userStateFlow.value = null
            }
        }
        Log.d("GameDataRepository", "✅ Escucha de datos de usuario iniciada para $uid.")
    }

    /**
     * Detiene la escucha en tiempo real del documento del usuario.
     * Debe llamarse cuando el usuario cierra sesión.
     */
    fun stopUserDataListener() {
        userListener?.remove()
        userListener = null
        _userStateFlow.value = null

        // NUEVO: También detener el listener del progreso del país
        stopCountryProgressListener()

        Log.d("GameDataRepository", "⏹️ Escucha de datos de usuario detenida.")
    }
    // --- FIN DE LA NUEVA ARQUITECTURA ---
    /**
     * Inicia la escucha en tiempo real del progreso del usuario para un país específico.
     */
    fun startCountryProgressListener(countryId: String) {
        if (currentCountryId == countryId && countryProgressListener != null) {
            return
        }

        stopCountryProgressListener()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _userCountryProgressStateFlow.value = null
            return
        }

        currentCountryId = countryId
        val progressDocId = "${uid}_${countryId}"
        val progressRef = db.collection("user_country_progress").document(progressDocId)

        countryProgressListener = progressRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("GameDataRepository", "Error en la escucha de progreso del país.", error)
                _userCountryProgressStateFlow.value = null
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                _userCountryProgressStateFlow.value = snapshot.toObject(UserCountryProgress::class.java)
                Log.d("GameDataRepository", "✅ Progreso de país actualizado: ${snapshot.toObject(UserCountryProgress::class.java)?.currentPc} PC")
            } else {
                _userCountryProgressStateFlow.value = UserCountryProgress(
                    userId = uid,
                    countryId = countryId,
                    currentPc = 0
                )
                Log.d("GameDataRepository", "📄 Documento de progreso no existe, creando estado inicial.")
            }
        }
        Log.d("GameDataRepository", "✅ Escucha de progreso de país iniciada para $countryId.")
    }

    /**
     * Detiene la escucha en tiempo real del progreso del país.
     */
    fun stopCountryProgressListener() {
        countryProgressListener?.remove()
        countryProgressListener = null
        currentCountryId = null
        _userCountryProgressStateFlow.value = null
        Log.d("GameDataRepository", "⏹️ Escucha de progreso de país detenida.")
    }

    suspend fun getUserRank(): Result<UserRankResponse> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            return Result.failure(Exception("Usuario no autenticado."))
        }

        // Usamos suspendCancellableCoroutine para manejar el listener de forma segura
        return try {
            suspendCancellableCoroutine { continuation ->
                val request = UserRankRequest(userId = uid)
                val requestRef = db.collection("user_rank_requests").document()

                val listener = requestRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        continuation.resume(Result.failure(error)) { /* No action on cancel */ }
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val response = snapshot.toObject(UserRankResponse::class.java)
                        // Esperamos a que la función complete el documento
                        if (snapshot.contains("completedAt")) {
                            if (response?.error != null) {
                                continuation.resume(Result.failure(Exception(response.error))) { }
                            } else if (response != null) {
                                continuation.resume(Result.success(response)) { }
                            }
                        }
                    }
                }

                // Si la corutina se cancela, removemos el listener
                continuation.invokeOnCancellation {
                    listener.remove()
                }

                // Enviamos la petición después de establecer el listener
                requestRef.set(request)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun getCountriesForContinent(continentId: String): List<Country> {
        return try {
            db.collection("countries").whereEqualTo("continentId", continentId).get().await()
                .toObjects(Country::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
            Log.d("FirebaseDebug", "[GameDataRepository] getAllLevels: Se encontraron ${snapshot.size()} documentos en la colección 'quizzes'.")
            Log.d("FirebaseDebug", "[GameDataRepository] getAllLevels: Se convirtieron ${levels.size} objetos LevelMetadata.")

            // Para ver si los datos vienen bien, imprimimos los primeros 5:
            levels.take(5).forEach { level ->
                Log.d("FirebaseDebug", "  - Leído LevelID: ${level.levelId}, TierID: ${level.tierId}")
            }

            return levels
        } catch (e: Exception) {
            Log.e("FirebaseDebug", "[GameDataRepository] getAllLevels: CRASH al leer la colección 'quizzes'", e)
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

    /**
     * Obtiene el récord de estrellas GLOBAL de un usuario para un nivel específico.
     * Consulta la colección 'user_level_completion'.
     */
    suspend fun getUserGlobalLevelProgress(levelId: String): Int {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return 0
        val completionDocId = "${uid}_${levelId}"
        return try {
            val document = db.collection("user_level_completion").document(completionDocId).get().await()
            document.toObject(UserLevelCompletion::class.java)?.starsEarned ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Obtiene el contador de estrellas LOCAL de un usuario para un nivel en un país específico.
     * Consulta la nueva colección 'user_level_country_progress'.
     */
    suspend fun getUserLevelCountryProgress(levelId: String, countryId: String): Int {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return 0
        val progressDocId = "${uid}_${levelId}_${countryId}"
        return try {
            val document = db.collection("user_level_country_progress").document(progressDocId).get().await()
            document.toObject(UserLevelCountryProgress::class.java)?.starsEarnedInCountry ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    /**
     * Obtiene una lista de todos los niveles que el usuario ha completado con 3 estrellas.
     * La consulta está optimizada para devolver documentos que ya contienen
     * el nombre del nivel y el puntaje máximo.
     */
    suspend fun getMasteredLevels(): List<UserLevelCompletion> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("user_level_completion")
                .whereEqualTo("userId", uid)
                .whereEqualTo("starsEarned", 3)
                .get().await()
            // La conversión usará la data class UserLevelCompletion actualizada en el paso anterior.
            snapshot.toObjects(UserLevelCompletion::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Devuelve una lista vacía si hay algún error.
        }
    }
    suspend fun getLevelCompletion(levelId: String): UserLevelCompletion? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val completionDocId = "${uid}_${levelId}"
        return try {
            db.collection("user_level_completion").document(completionDocId).get().await()
                .toObject(UserLevelCompletion::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
