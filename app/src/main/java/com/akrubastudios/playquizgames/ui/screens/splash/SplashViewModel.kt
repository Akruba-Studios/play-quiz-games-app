// en ui/screens/splash/SplashViewModel.kt
package com.akrubastudios.playquizgames.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Representa el destino final después de la verificación.
enum class Destination {
    LOGIN,
    ONBOARDING,
    MAP
}

data class SplashState(
    val isLoading: Boolean = true,
    val destination: Destination? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashState())
    val uiState = _uiState.asStateFlow()

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                // Caso 1: No hay token en caché. El usuario no está logueado.
                _uiState.value = SplashState(isLoading = false, destination = Destination.LOGIN)
                return@launch
            }

            try {
                // Caso 2: Hay token. Verificamos si sus datos existen en Firestore.
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()

                if (!userDoc.exists()) {
                    // ¡AQUÍ ESTÁ LA SOLUCIÓN A TU PROBLEMA!
                    // El usuario existe en Auth pero no en Firestore. Es un "fantasma".
                    // Lo deslogueamos forzosamente y lo mandamos al Login.
                    auth.signOut()
                    _uiState.value = SplashState(isLoading = false, destination = Destination.LOGIN)
                } else {
                    // El usuario es válido. ¿Es nuevo (sin país) o antiguo?
                    val userData = userDoc.toObject(com.akrubastudios.playquizgames.domain.User::class.java)!!
                    if (userData.availableCountries.isEmpty() && userData.conqueredCountries.isEmpty()) {
                        _uiState.value = SplashState(isLoading = false, destination = Destination.ONBOARDING)
                    } else {
                        _uiState.value = SplashState(isLoading = false, destination = Destination.MAP)
                    }
                }
            } catch (e: Exception) {
                // Si hay cualquier error de red, lo más seguro es desloguear y mandar al Login.
                auth.signOut()
                _uiState.value = SplashState(isLoading = false, destination = Destination.LOGIN)
            }
        }
    }
}