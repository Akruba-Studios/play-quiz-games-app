package com.akrubastudios.playquizgames.ui.screens.country

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CountryState(
    val countryName: String = "",
    val availableCategories: List<Category> = emptyList(),
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
            val allCategories = gameDataRepository.getCategoryList() // <-- NUEVA LLAMADA

            if (country != null) {
                // Filtramos para obtener solo las categorías disponibles para este país
                val availableCats = allCategories.filter { category ->
                    country.availableCategories.contains(category.categoryId)
                }

                _uiState.value = CountryState(
                    countryName = country.name["es"] ?: "País",
                    availableCategories = availableCats, // <-- USA LA LISTA FILTRADA
                    currentPc = progress?.currentPc ?: 0,
                    pcRequired = country.pcRequired,
                    isLoading = false
                )
            }
        }
    }
}