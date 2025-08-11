package com.akrubastudios.playquizgames.ui.screens.country

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CountryState(
    val countryName: String = "",
    val currentPc: Long = 0,
    val pcRequired: Long = 50000,
    val isLoading: Boolean = true
)

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    savedStateHandle: SavedStateHandle // Para obtener el countryId de la ruta de navegación
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountryState())
    val uiState = _uiState.asStateFlow()

    private val countryId: String = savedStateHandle.get<String>("countryId")!!

    init {
        loadCountryData()
    }

    private fun loadCountryData() {
        viewModelScope.launch {
            val country = gameDataRepository.getCountry(countryId)
            val progress = gameDataRepository.getUserProgressForCountry(countryId)

            if (country != null) {
                _uiState.value = CountryState(
                    countryName = country.name["es"] ?: "País", // Muestra el nombre en español
                    currentPc = progress?.currentPc ?: 0,
                    pcRequired = country.pcRequired,
                    isLoading = false
                )
            }
        }
    }
}