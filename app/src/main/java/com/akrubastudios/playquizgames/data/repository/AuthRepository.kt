package com.akrubastudios.playquizgames.data.repository

import com.akrubastudios.playquizgames.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore // <-- AÑADE FIRESTORE AQUÍ
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user!!

            // --- INICIO DE LA NUEVA LÓGICA ---

            // 1. Apuntamos a la colección 'users' y al documento con el uid del usuario
            val userRef = db.collection("users").document(firebaseUser.uid)
            val document = userRef.get().await()

            val user: User
            if (!document.exists()) {
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
                    "createdAt" to System.currentTimeMillis()
                )
                userRef.set(newUserMap).await()
            } else {
                // 3. Si el usuario SÍ existe, simplemente creamos nuestro objeto local
                // (En el futuro, aquí podríamos actualizar su nombre o foto si ha cambiado)
                user = User(
                    uid = document.getString("uid")!!,
                    displayName = document.getString("displayName"),
                    photoUrl = document.getString("photoUrl")
                )
            }

            // --- FIN DE LA NUEVA LÓGICA ---

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}