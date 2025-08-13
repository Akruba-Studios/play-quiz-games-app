package com.akrubastudios.playquizgames.ui.screens.country

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.LevelMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CountryState(
    val countryName: String = "",
    val availableLevels: Map<Category, String?> = emptyMap(),
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
            val allCategories = gameDataRepository.getCategoryList()
            val userCompletions = gameDataRepository.getAllLevelCompletionData()

            if (country != null) {
                val availableCats = allCategories.filter { category ->
                    country.availableCategories.contains(category.categoryId)
                }

                // --- NUEVA LÓGICA PARA ENCONTRAR EL SIGUIENTE NIVEL ---
                val nextLevelsMap = mutableMapOf<Category, String?>()
                availableCats.forEach { category ->
                    // 1. Obtiene todos los niveles de esta categoría
                    val allLevelsInCategory = gameDataRepository.getLevelsForCategory(category.categoryId)
                        .sortedBy { it.levelNumber }

                    // 2. Busca el primer nivel que el usuario NO ha completado (0 estrellas)
                    var nextLevelId: String? = null
                    for (level in allLevelsInCategory) {
                        val completion = userCompletions.find { it.levelId == level.levelId }
                        if (completion == null || completion.starsEarned == 0) {
                            nextLevelId = level.levelId
                            break // Encontramos el siguiente nivel, salimos del bucle
                        }
                    }
                    nextLevelsMap[category] = nextLevelId
                }
                // ----------------------------------------------------

                _uiState.value = CountryState(
                    countryName = country.name["es"] ?: "País",
                    availableLevels = nextLevelsMap,
                    currentPc = gameDataRepository.getUserProgressForCountry(countryId)?.currentPc ?: 0,
                    pcRequired = country.pcRequired,
                    isLoading = false
                )
            }
        }
    }
}