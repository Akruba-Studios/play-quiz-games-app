package com.akrubastudios.playquizgames.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.akrubastudios.playquizgames.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de Perfil.
 */
data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val levelInfo: PlayerLevelManager.LevelInfo? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    // Un "Channel" para enviar eventos únicos, como la navegación de cierre de sesión.
    private val _signOutEvent = Channel<Unit>()
    val signOutEvent = _signOutEvent.receiveAsFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = ProfileState(isLoading = true)
            val currentUser = gameDataRepository.getUserData()
            if (currentUser != null) {
                val levelInfo = PlayerLevelManager.calculateLevelInfo(currentUser.totalXp)
                _uiState.value = ProfileState(
                    isLoading = false,
                    user = currentUser,
                    levelInfo = levelInfo
                )
            } else {
                // Manejar el caso de que el usuario no se encuentre
                _uiState.value = ProfileState(isLoading = false)
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual y envía un evento para notificar a la UI.
     */
    fun signOut() {
        Log.d("SignOut_Debug", "[PASO 1] ProfileViewModel.signOut() llamado.")
        viewModelScope.launch {
            authRepository.signOut()
            _signOutEvent.send(Unit)
            Log.d("SignOut_Debug", "[PASO 4] Evento de navegación enviado.")
        }
    }
}