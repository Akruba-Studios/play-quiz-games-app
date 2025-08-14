package com.akrubastudios.playquizgames.ui.screens.country

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.LevelMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CountryState(
    val country: Country? = null,
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
            Log.d("CountryVM_Debug", "========================================")
            Log.d("CountryVM_Debug", "INICIANDO DATOS PARA PAÍS: $countryId")

            val country = gameDataRepository.getCountry(countryId)
            val allCategories = gameDataRepository.getCategoryList()
            val userCompletions = gameDataRepository.getAllLevelCompletionData()
            val allLevels = gameDataRepository.getAllLevels() // <-- NUEVO: Obtenemos todos los niveles

            if (country != null) {
                val availableCats = allCategories.filter {
                    country.availableCategories.contains(it.categoryId)
                }
                Log.d("CountryVM_Debug", "Categorías disponibles para '${country.name["es"]}': ${availableCats.size}")

                val completionsMap = userCompletions.associateBy { it.levelId }
                val nextLevelsMap = mutableMapOf<Category, String?>()

                for (category in availableCats) {
                    Log.d("CountryVM_Debug", "--- Analizando Categoría: ${category.categoryId} ---")
                    // --- LÓGICA DE FILTRADO EN LA APP ---
                    // Filtramos la lista completa de niveles por el ID de la categoría
                    val allLevelsInCategory = allLevels.filter { level ->
                        level.levelId.startsWith(category.categoryId)
                    }
                    Log.d("CountryVM_Debug", "   > Niveles filtrados para esta categoría: ${allLevelsInCategory.size}")

                    val hasUnmasteredLevels = allLevelsInCategory.any { level ->
                        val stars = completionsMap[level.levelId]?.starsEarned ?: 0
                        stars < 3
                    }
                    Log.d("CountryVM_Debug", "   > ¿Tiene niveles no maestreados?: $hasUnmasteredLevels")

                    nextLevelsMap[category] = if (hasUnmasteredLevels) category.categoryId else null
                }

                Log.d("CountryVM_Debug", "Mapa final de botones: $nextLevelsMap")

                _uiState.value = CountryState(
                    country = country,
                    availableLevels = nextLevelsMap,
                    currentPc = gameDataRepository.getUserProgressForCountry(countryId)?.currentPc ?: 0,
                    pcRequired = country.pcRequired,
                    isLoading = false
                )
            }
            Log.d("CountryVM_Debug", "========================================")
        }
    }
}