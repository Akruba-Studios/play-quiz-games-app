package com.akrubastudios.playquizgames.ui.screens.onboarding

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

data class CountrySelectionState(
    val countries: List<Country> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CountrySelectionViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountrySelectionState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()
    private val continentId: String = savedStateHandle.get<String>("continentId")!!

    init {
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            val countryList = gameDataRepository.getCountriesForContinent(continentId)
            _uiState.value = CountrySelectionState(countries = countryList, isLoading = false)
        }
    }

    fun onCountrySelected(countryId: String) {
        val uid = auth.currentUser?.uid // Ahora 'auth' existe
        if (uid == null) {
            Log.e("CountrySelectionVM", "Usuario nulo, no se puede seleccionar país.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val startRequest = hashMapOf(
                    "uid" to uid,
                    "countryId" to countryId,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("start_requests").add(startRequest).await() // Ahora 'db' existe
                _navigationEvent.send(Unit)
            } catch (e: Exception) {
                Log.e("CountrySelectionVM", "Error al enviar start_request", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}