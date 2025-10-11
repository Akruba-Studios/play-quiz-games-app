package com.akrubastudios.playquizgames.ui.screens.createprofile

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

val availableAvatars = listOf(
    "https://firebasestorage.googleapis.com/v0/b/akrubastudios-playquiz-dev.firebasestorage.app/o/avatars%2Fprofile%2Fcanadian.png?alt=media",
    "https://firebasestorage.googleapis.com/v0/b/akrubastudios-playquiz-dev.firebasestorage.app/o/avatars%2Fprofile%2Fwoman.png?alt=media",
    "https://firebasestorage.googleapis.com/v0/b/akrubastudios-playquiz-dev.firebasestorage.app/o/avatars%2Fprofile%2Fman.png?alt=media",
    "https://firebasestorage.googleapis.com/v0/b/akrubastudios-playquiz-dev.firebasestorage.app/o/avatars%2Fprofile%2Fwoman1.png?alt=media"
)

data class CreateProfileState(
    val googleName: String = "",
    val googlePhotoUrl: String = "",
    val username: String = "",
    val useGoogleData: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedAvatarUrl: String = availableAvatars.first()
)

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    val imageLoader: ImageLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProfileState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        val decodedName = URLDecoder.decode(savedStateHandle.get<String>("googleName") ?: "", StandardCharsets.UTF_8.name())
        val decodedPhotoUrl = URLDecoder.decode(savedStateHandle.get<String>("googlePhotoUrl") ?: "", StandardCharsets.UTF_8.name())
        _uiState.update {
            it.copy(
                googleName = decodedName,
                googlePhotoUrl = decodedPhotoUrl,
                username = decodedName
            )
        }
    }

    fun onUsernameChange(newName: String) {
        _uiState.update { it.copy(username = newName, error = null) }
    }

    fun onUseGoogleDataChange(useGoogle: Boolean) {
        _uiState.update {
            it.copy(
                useGoogleData = useGoogle,
                // Si vuelve a usar los datos de Google, restauramos el nombre
                username = if (useGoogle) it.googleName else it.username,
                error = null
            )
        }
    }

    fun onAvatarSelected(avatarUrl: String) {
        _uiState.update { it.copy(selectedAvatarUrl = avatarUrl) }
    }

    fun onContinueClicked() {
        val state = _uiState.value
        if (!state.useGoogleData && state.username.isBlank()) {
            _uiState.update { it.copy(error = "Username cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val uid = auth.currentUser?.uid
            Log.d("CreateProfileDebug", "UID del usuario: $uid")
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }

            try {
                val photoUrlToSave = if (state.useGoogleData) {
                    state.googlePhotoUrl
                } else {
                    state.selectedAvatarUrl
                }
                // Preparamos los datos a actualizar
                val profileData = mapOf(
                    "displayName" to state.username,
                    "photoUrl" to photoUrlToSave,
                    "isProfileConfirmed" to true
                )

                Log.d("CreateProfileDebug", "Datos a actualizar: $profileData")

                // Actualizamos el documento
                db.collection("users").document(uid).update(profileData).await()

                // ⭐ NUEVA LÍNEA CRÍTICA: Forzar lectura desde servidor para actualizar el caché
                db.collection("users").document(uid)
                    .get(com.google.firebase.firestore.Source.SERVER).await()

                Log.d("CreateProfileDebug", "✅ Update ejecutado exitosamente")

                // Verificar que se guardó
                val verifyDoc = db.collection("users").document(uid)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                val confirmed = verifyDoc.getBoolean("isProfileConfirmed")

                Log.d("CreateProfileDebug", "Verificación: isProfileConfirmed = $confirmed")

                // Enviamos el evento de navegación
                _navigationEvent.send(Unit)

            } catch (e: Exception) {
                Log.e("CreateProfileDebug", "❌ ERROR al actualizar: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}