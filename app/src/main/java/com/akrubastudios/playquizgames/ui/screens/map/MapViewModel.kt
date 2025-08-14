package com.akrubastudios.playquizgames.ui.screens.map

import androidx.lifecycle.ViewModel
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapState(
    val countries: List<Country> = emptyList(),
    val conqueredCountryIds: List<String> = emptyList(),
    val availableCountryIds: List<String> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameDataRepository: GameDataRepository // <-- Inyectamos el nuevo repo
) : ViewModel() {

    val currentUser = authRepository.currentUser

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    init {
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            val countryList = gameDataRepository.getCountryList()
            val userData = gameDataRepository.getUserData()

            val conqueredIds = userData?.conqueredCountries ?: emptyList()

            // --- NUEVA LÓGICA DE PAÍSES DISPONIBLES ---
            val availableIds = mutableSetOf<String>()
            if (conqueredIds.isEmpty()) {
                // Si el jugador no ha conquistado nada, su país de origen está disponible
                // (Necesitaremos una forma de saber cuál es, por ahora lo hardcodeamos)
                val startingCountry = countryList.firstOrNull { it.continentId == "south_america" }?.countryId
                if (startingCountry != null) {
                    availableIds.add(startingCountry)
                }
            } else {
                // Si ya ha conquistado países, sus vecinos están disponibles
                conqueredIds.forEach { conqueredId ->
                    val conqueredCountry = countryList.find { it.countryId == conqueredId }
                    conqueredCountry?.neighbors?.forEach { neighborId ->
                        availableIds.add(neighborId)
                    }
                }
            }
            // ------------------------------------------

            _uiState.value = MapState(
                countries = countryList,
                conqueredCountryIds = conqueredIds,
                availableCountryIds = availableIds.toList(), // <-- USA LA NUEVA LISTA
                isLoading = false
            )
        }
    }
}