package com.akrubastudios.playquizgames.data.repository

import com.akrubastudios.playquizgames.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Usamos @Inject constructor para que Hilt sepa cómo crear este repositorio
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    // Devuelve el usuario de Firebase actual, si existe
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Función para iniciar sesión con el token de Google
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user!!

            // Crea nuestro propio objeto User a partir de los datos de Firebase
            val user = User(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoUrl?.toString()
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}