package com.akrubastudios.playquizgames.ui.screens.login

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.SignInResult
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.PrecacheManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val signInResult: SignInResult? = null,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val application: Application,
    private val precacheManager: PrecacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    fun onSignInResult(idToken: String?) {
        if (idToken == null) {
            _uiState.update { it.copy(error = application.getString(R.string.login_failed)) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 🎨 NUEVO: Disparamos la precarga de avatares en paralelo con el login
            android.util.Log.d("LoginViewModel", "🎨 Iniciando precarga de avatares durante login de Google")
            val avatarUrls = com.akrubastudios.playquizgames.ui.screens.createprofile.availableAvatars
            precacheManager.precacheAvatarsInBackground(avatarUrls)

            val result = repository.signInWithGoogle(idToken)
            result.onSuccess { signInResult ->
                _uiState.update {
                    it.copy(isLoading = false, signInResult = signInResult)
                }
            }
            result.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}