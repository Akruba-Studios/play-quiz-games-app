// en ui/screens/result/ResultViewModel.kt
package com.akrubastudios.playquizgames.ui.screens.result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultState(
    val showXpTutorial: Boolean = false
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultState())
    val uiState = _uiState.asStateFlow()

    init {
        checkTutorialStatus()
    }

    private fun checkTutorialStatus() {
        viewModelScope.launch {
            val userData = gameDataRepository.getUserData()
            if (userData?.hasSeenXpTutorial == false) {
                _uiState.value = ResultState(showXpTutorial = true)
            }
        }
    }

    fun xpTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        // --- INICIO DE LA MODIFICACIÓN ---
        // 1. Ocultamos el diálogo en la UI INMEDIATAMENTE.
        if (_uiState.value.showXpTutorial) {
            _uiState.value = _uiState.value.copy(showXpTutorial = false)
        }
        // --- FIN DE LA MODIFICACIÓN ---

        // 2. La escritura a la base de datos se ejecuta en segundo plano.
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update("hasSeenXpTutorial", true)
            } catch (e: Exception) {
                // Manejar el error si es necesario
                Log.e("ResultViewModel", "Error al actualizar hasSeenXpTutorial", e)
            }
        }
    }
}