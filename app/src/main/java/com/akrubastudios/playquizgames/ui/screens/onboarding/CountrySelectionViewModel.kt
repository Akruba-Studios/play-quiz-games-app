package com.akrubastudios.playquizgames.ui.screens.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CountrySelectionState(
    val countries: List<Country> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CountrySelectionViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountrySelectionState())
    val uiState = _uiState.asStateFlow()
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
}