// en ui/screens/result/ResultViewModel.kt
package com.akrubastudios.playquizgames.ui.screens.result

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.SoundManager
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultState(
    val showXpTutorial: Boolean = false,
    val pendingBossChallengeCountryId: String? = null, // Guardará el ID del país del jefe
    val bossLevelId: String? = null, // Guardará el ID del nivel del jefe
    val pendingBossChallengeCountryName: String? = null
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    val soundManager: SoundManager,
    private val savedStateHandle: SavedStateHandle,
    private val languageManager: LanguageManager
) : ViewModel() {

    val countryId: String = savedStateHandle.get<String>("countryId") ?: ""
    val levelId: String = savedStateHandle.get<String>("levelId") ?: ""

    private val _uiState = MutableStateFlow(ResultState())
    val uiState = _uiState.asStateFlow()

    init {
        // La inicialización ahora solo llama a nuestra nueva función observadora.
        observeUserState()
    }

    private fun observeUserState() {
        viewModelScope.launch {
            // Nos suscribimos al flujo de datos del usuario desde el repositorio.
            // Esto se ejecutará cada vez que los datos del usuario cambien.
            gameDataRepository.userStateFlow.collect { user ->
                if (user != null) {
                    // Verificamos si hay un desafío de jefe pendiente.
                    if (user.pendingBossChallenge != null && user.pendingBossChallenge == countryId) {

                        // Si hay un desafío, necesitamos encontrar el ID del nivel del jefe.
                        val countryData = gameDataRepository.getCountry(user.pendingBossChallenge)
                        val lang = languageManager.languageStateFlow.value
                        val countryName = countryData?.name?.get(lang) ?: countryData?.name?.get("es")

                        _uiState.update {
                            it.copy(
                                showXpTutorial = !user.hasSeenXpTutorial, // También manejamos el tutorial aquí
                                pendingBossChallengeCountryId = user.pendingBossChallenge,
                                bossLevelId = countryData?.bossLevelId,
                                pendingBossChallengeCountryName = countryName
                            )
                        }
                    } else {
                        // Si no hay desafío, solo manejamos el tutorial.
                        _uiState.update {
                            it.copy(
                                showXpTutorial = !user.hasSeenXpTutorial
                            )
                        }
                    }
                }
            }
        }
    }

    // --- INICIO DEL CÓDIGO A AÑADIR ---
    /**
     * Limpia la bandera de desafío pendiente en Firestore.
     * Se llamará desde la UI después de que el usuario interactúe con el diálogo.
     */
    fun clearPendingBossChallenge() {
        val uid = auth.currentUser?.uid ?: return

        // Ponemos el estado en null inmediatamente para que el diálogo desaparezca.
        _uiState.update { it.copy(
            pendingBossChallengeCountryId = null,
            bossLevelId = null,
            pendingBossChallengeCountryName = null // <-- AÑADE ESTA LÍNEA PARA LIMPIAR EL NOMBRE
        )}

        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .update("pendingBossChallenge", FieldValue.delete())
            } catch (e: Exception) {
                Log.e("ResultViewModel", "Error al limpiar pendingBossChallenge", e)
            }
        }
    }
    fun xpTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        if (_uiState.value.showXpTutorial) {
            _uiState.value = _uiState.value.copy(showXpTutorial = false)
        }

        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("hasSeenXpTutorial", true)
            } catch (e: Exception) {
                Log.e("ResultViewModel", "Error al actualizar hasSeenXpTutorial", e)
            }
        }
    }
}