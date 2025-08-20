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

            // Obtenemos todos los datos necesarios
            val country = gameDataRepository.getCountry(countryId)
            val allCategories = gameDataRepository.getCategoryList()
            val userData = gameDataRepository.getUserData()
            val userProgress = gameDataRepository.getUserProgressForCountry(countryId)

            // Hacemos la llamada al quizRepository directamente aquí
            val bossQuiz = country?.bossLevelId?.let {
                if (it.isNotBlank()) quizRepository.getLevel(it) else null
            }

            if (country == null || userData == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                // Opcional: Podríamos añadir un estado de error aquí
                return@launch
            }

            // --- INICIO DE LA LÓGICA DE ESTADO MEJORADA ---

            // 1. Obtenemos la lista completa de países para poder buscar vecinos.
            val allCountries = gameDataRepository.getCountryList()

            // 2. Creamos un Set con TODOS los países que deberían ser "jugables"
            val playableCountryIds = mutableSetOf<String>()
            playableCountryIds.addAll(userData.availableCountries)
            playableCountryIds.addAll(userData.conqueredCountries)

            // 3. Añadimos los vecinos de los países conquistados
            userData.conqueredCountries.forEach { conqueredId ->
                val conqueredCountry = allCountries.find { it.countryId == conqueredId }
                conqueredCountry?.neighbors?.let { neighbors ->
                    playableCountryIds.addAll(neighbors)
                }
            }

            // 4. Determinamos el estado del país actual.
            val status = when {
                userData.dominatedCountries.contains(countryId) -> CountryStatus.DOMINATED
                userData.conqueredCountries.contains(countryId) -> CountryStatus.CONQUERED
                playableCountryIds.contains(countryId) -> CountryStatus.AVAILABLE
                else -> CountryStatus.LOCKED
            }
            // --- FIN DE LA LÓGICA DE ESTADO MEJORADA ---

            // El resto de la lógica (filtrar categorías, etc.) no cambia.
            val filteredCategories = country.availableCategories.mapNotNull { (catId, isAvailable) ->
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