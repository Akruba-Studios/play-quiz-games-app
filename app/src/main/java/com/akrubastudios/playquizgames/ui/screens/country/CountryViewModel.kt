package com.akrubastudios.playquizgames.ui.screens.country

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- NUEVO: Enum para representar el estado del país ---
enum class CountryStatus {
    LOCKED,       // El jugador no tiene acceso
    AVAILABLE,    // El jugador puede jugar en él
    CONQUERED,    // El jugador ha alcanzado el PC requerido
    DOMINATED     // El jugador ha vencido al Jefe
}

// --- MODIFICADO: El State ahora usa el Enum y guarda los studyTopics ---
data class CountryState(
    val country: Country? = null,
    val countryStatus: CountryStatus = CountryStatus.LOCKED,
    val availableCategories: List<Category> = emptyList(),
    val studyTopics: List<String> = emptyList(),
    val currentPc: Long = 0,
    val pcRequired: Long = 1, // Evita división por cero
    val isLoading: Boolean = true
)

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val quizRepository: com.akrubastudios.playquizgames.data.repository.QuizRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountryState())
    val uiState = _uiState.asStateFlow()

    private val countryId: String = savedStateHandle.get<String>("countryId")!!

    init {
        loadCountryData()
    }

    private fun loadCountryData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // --- Realizamos todas las llamadas de red necesarias en paralelo ---
            val countryAsync = gameDataRepository.getCountry(countryId)
            val allCategoriesAsync = gameDataRepository.getCategoryList()
            val userDataAsync = gameDataRepository.getUserData()
            val userProgressAsync = gameDataRepository.getUserProgressForCountry(countryId)
            // Obtenemos los datos del quiz del jefe para los studyTopics
            val bossQuizAsync = countryAsync?.bossLevelId?.let {
                if (it.isNotBlank()) quizRepository.getLevel(it) else null
            }

            // --- Esperamos a que todas las llamadas terminen ---
            val country = countryAsync
            val allCategories = allCategoriesAsync
            val userData = userDataAsync
            val userProgress = userProgressAsync
            val bossQuiz = bossQuizAsync

            if (country == null || userData == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            // --- LÓGICA PARA DETERMINAR EL ESTADO DEL PAÍS ---
            val status = when {
                userData.dominatedCountries.contains(countryId) -> CountryStatus.DOMINATED
                userData.conqueredCountries.contains(countryId) -> CountryStatus.CONQUERED
                (userData.availableCountries + userData.conqueredCountries).contains(countryId) -> CountryStatus.AVAILABLE
                else -> CountryStatus.LOCKED
            }

            // --- LÓGICA PARA FILTRAR CATEGORÍAS ---
            val filteredCategories = country.availableCategories.mapNotNull { (catId, isAvailable) ->
                // Una categoría se muestra si:
                // 1. Está marcada como 'true' (disponible para todos)
                // 2. O si el país está DOMINADO (desbloquea las categorías 'false')
                if (isAvailable || status == CountryStatus.DOMINATED) {
                    allCategories.find { it.categoryId == catId }
                } else {
                    null
                }
            }

            _uiState.value = CountryState(
                country = country,
                countryStatus = status,
                availableCategories = filteredCategories,
                studyTopics = bossQuiz?.studyTopics ?: emptyList(),
                currentPc = userProgress?.currentPc ?: 0,
                pcRequired = country.pcRequired.takeIf { it > 0 } ?: 1,
                isLoading = false
            )
        }
    }
}