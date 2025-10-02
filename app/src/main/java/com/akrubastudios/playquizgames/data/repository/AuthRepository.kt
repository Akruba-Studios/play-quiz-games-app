package com.akrubastudios.playquizgames.data.repository

import android.util.Log
import com.akrubastudios.playquizgames.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SignInResult(
    val user: User,
    val isNewUser: Boolean
)
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore, // <-- AÑADE FIRESTORE AQUÍ
    private val gameDataRepository: GameDataRepository
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): Result<SignInResult> {
        return try {
            Log.d("AuthRepo", "🔐 signInWithGoogle LLAMADO")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user!!

            // --- INICIO DE LA NUEVA LÓGICA ---

            // 1. Apuntamos a la colección 'users' y al documento con el uid del usuario
            val userRef = db.collection("users").document(firebaseUser.uid)
            val document = userRef.get(com.google.firebase.firestore.Source.SERVER).await()
            Log.d("AuthRepo", "Documento existe: ${document.exists()}")

            val user: User
            var isNewUser = false
            if (!document.exists()) {
                Log.d("AuthRepo", "❌ Creando usuario NUEVO con isProfileConfirmed = false")
                isNewUser = true
                // 2. Si el usuario NO existe, lo creamos
                user = User(
                    uid = firebaseUser.uid,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                // Creamos un mapa para subir a Firestore
                val newUserMap = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl,
                    "totalXp" to 0,
                    "conqueredCountries" to emptyList<String>(),
                    "createdAt" to System.currentTimeMillis(),
                    "hasSeenWelcomeDialog" to false,
                    "hasSeenConquestTutorial" to false,
                    "hasSeenLevelUnlockTutorial" to false,
                    "hasSeenXpTutorial" to false,
                    "hasSeenFreeModeUnlockedDialog" to false,
                    "hasSeenDominationTutorial" to false,
                    "pendingProfileNotifications" to emptyList<String>(),
                    "hasTriggeredConquest2Milestone" to false,
                    "hasTriggeredExpansionMilestone" to false,
                    "hasSeenFunFactTutorial" to false,
                    "gems" to 0,
                    "isProfileConfirmed" to false
                )
                userRef.set(newUserMap).await()
            } else {
                Log.d("AuthRepo", "✅ Usuario ya existe, no se sobrescribe")
                isNewUser = false // <-- Confirmamos que no es nuevo
                user = document.toObject(User::class.java)!!
            }

            val signInResult = SignInResult(user = user, isNewUser = isNewUser)
            Result.success(signInResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        Log.d("SignOut_Debug", "[PASO 2] AuthRepository.signOut() llamado.")
        // --- INICIO DE LA MODIFICACIÓN ---
        // 1. Primero, ordenamos explícitamente detener todas las escuchas.
        gameDataRepository.stopUserDataListener()

        // 2. Solo después, cerramos la sesión de forma segura.
        auth.signOut()
        // --- FIN DE LA MODIFICACIÓN ---
        Log.d("SignOut_Debug", "[PASO 3] auth.signOut() ejecutado.")
    }
}