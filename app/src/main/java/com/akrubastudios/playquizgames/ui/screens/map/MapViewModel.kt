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
        listenToUserData()
    }

    private fun listenToUserData() {
        viewModelScope.launch {
            val countryList = gameDataRepository.getCountryList()

            // Ponemos el estado de carga explícitamente al empezar a escuchar
            _uiState.value = MapState(isLoading = true)

            gameDataRepository.getUserDataFlow().collect { userData ->

                // Si userData es null la primera vez, el estado de carga se mantendrá
                // y la UI mostrará el indicador.

                if (userData != null) {
                    val conqueredIds = userData.conqueredCountries
                    val availableIdsFromDB = userData.availableCountries

                    val availableIds = mutableSetOf<String>()
                    availableIdsFromDB.forEach { availableIds.add(it) }
                    conqueredIds.forEach { conqueredId ->
                        val conqueredCountry = countryList.find { it.countryId == conqueredId }
                        conqueredCountry?.neighbors?.forEach { neighborId ->
                            availableIds.add(neighborId)
                        }
                    }

                    _uiState.value = MapState(
                        countries = countryList,
                        conqueredCountryIds = conqueredIds,
                        availableCountryIds = availableIds.toList(),
                        isLoading = false // <-- Solo ponemos isLoading a false cuando tenemos datos
                    )
                }
            }
        }
    }
}