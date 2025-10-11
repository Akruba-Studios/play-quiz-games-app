// en ui/screens/splash/SplashViewModel.kt
package com.akrubastudios.playquizgames.ui.screens.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.akrubastudios.playquizgames.core.AppConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import javax.inject.Inject

// Representa el destino final después de la verificación. Control 1-SVM
enum class Destination {
    LOGIN,
    CREATE_PROFILE,
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
    private val db: FirebaseFirestore,
    private val imageLoader: ImageLoader,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashState())
    val uiState = _uiState.asStateFlow()

    val currentUser = auth.currentUser

    /**
     * Pre-carga una única imagen de forma controlada.
     */
    private suspend fun precacheSingleImage(imageUrl: String): Boolean {
        if (imageUrl.isBlank() || imageUrl.contains("PEGAR_AQUÍ")) return false

        return suspendCancellableCoroutine { continuation ->
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .listener(
                    onSuccess = { _, result ->
                        Log.d("Splash_Precache", "✅ Fondo genérico precargado desde ${result.dataSource}")
                        if (continuation.isActive) continuation.resume(true)
                    },
                    onError = { _, result ->
                        Log.e("Splash_Precache", "❌ Error al precargar fondo genérico: ${result.throwable}")
                        if (continuation.isActive) continuation.resume(false)
                    }
                )
                .build()

            imageLoader.enqueue(request)

            continuation.invokeOnCancellation {}
        }
    }

    init {
        // Lanzamos la precarga en una corutina separada.
        // Se ejecutará en paralelo con checkUserStatus.
        viewModelScope.launch {
            coroutineScope {
                Log.d("Splash_Precache", "🚀 Iniciando precarga de fondos genéricos...")
                val jobs = listOf(
                    async(Dispatchers.IO) { precacheSingleImage(AppConstants.ONBOARDING_BACKGROUND_URL) },
                    async(Dispatchers.IO) { precacheSingleImage(AppConstants.MENU_BACKGROUND_URL) },
                    async(Dispatchers.IO) { precacheSingleImage(AppConstants.RESULTS_BACKGROUND_URL) }
                )
                jobs.awaitAll()
                Log.d("Splash_Precache", "✅ Precarga de fondos genéricos completada.")
            }
        }

        checkUserStatus()
    }

    private fun checkUserStatus() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser

            Log.d("SplashDebug", "=== INICIO checkUserStatus ===")
            Log.d("SplashDebug", "FirebaseUser: ${firebaseUser?.uid}")

            if (firebaseUser == null) {
                // Caso 1: No hay token en caché. El usuario no está logueado.
                _uiState.value = SplashState(isLoading = false, destination = Destination.LOGIN)
                return@launch
            }

            try {
                Log.d("SplashDebug", "Leyendo documento desde SERVER...")
                // Caso 2: Hay token. Verificamos si sus datos existen en Firestore.
                val userDoc = db.collection("users").document(firebaseUser.uid)
                    .get(com.google.firebase.firestore.Source.SERVER).await()

                Log.d("SplashDebug", "Documento existe: ${userDoc.exists()}")
                Log.d("SplashDebug", "Metadata - isFromCache: ${userDoc.metadata.isFromCache}")
                Log.d("SplashDebug", "Metadata - hasPendingWrites: ${userDoc.metadata.hasPendingWrites()}")

                if (!userDoc.exists()) {
                    Log.d("SplashDebug", "Documento NO existe, cerrando sesión")
                    // ¡AQUÍ ESTÁ LA SOLUCIÓN A TU PROBLEMA!
                    // El usuario existe en Auth pero no en Firestore. Es un "fantasma".
                    // Lo deslogueamos forzosamente y lo mandamos al Login.
                    auth.signOut()
                    _uiState.value = SplashState(isLoading = false, destination = Destination.LOGIN)
                } else {
                    // El usuario es válido. ¿Es nuevo (sin país) o antiguo?
                    val isConfirmedRaw = userDoc.getBoolean("isProfileConfirmed")
                    Log.d("SplashDebug", "Valor RAW de isProfileConfirmed: $isConfirmedRaw")

                    // Ahora convierte el objeto completo
                    val userData = userDoc.toObject(com.akrubastudios.playquizgames.domain.User::class.java)!!
                    Log.d("SplashDebug", "Valor en objeto User: ${userData.isProfileConfirmed}")


                    Log.d("SplashDebug", "isProfileConfirmed: ${userData.isProfileConfirmed}")
                    Log.d("SplashDebug", "availableCountries: ${userData.availableCountries}")
                    Log.d("SplashDebug", "conqueredCountries: ${userData.conqueredCountries}")

                    // 1. PRIMERA Y MÁS IMPORTANTE COMPROBACIÓN: ¿Ha confirmado su perfil?
                    if (!userData.isProfileConfirmed) {
                        Log.d("SplashDebug", "-> Navegando a CREATE_PROFILE")
                        _uiState.value = SplashState(isLoading = false, destination = Destination.CREATE_PROFILE)

                        // 2. Si ya lo confirmó, ¿tiene país?
                    } else if (userData.availableCountries.isEmpty() && userData.conqueredCountries.isEmpty()) {
                        Log.d("SplashDebug", "-> Navegando a ONBOARDING")
                        _uiState.value = SplashState(isLoading = false, destination = Destination.ONBOARDING)

                        // 3. Si ya tiene todo, va al mapa.
                    } else {
                        Log.d("SplashDebug", "-> Navegando a MAP")
                        _uiState.value = SplashState(isLoading = false, destination = Destination.MAP)
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashDebug", "ERROR en checkUserStatus: ${e.message}", e)
                // Si hay cualquier error de red, lo más seguro es desloguear y mandar al Login.
                auth.signOut()
                _uiState.value = SplashState(isLoading = false, destination = Destination.LOGIN)
            }
        }
    }
}