package com.akrubastudios.playquizgames.ui.screens.country

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.Country
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    val isScreenLoading: Boolean = true, // Para la carga inicial
    val isApplyingBoost: Boolean = false,
    val canApplyBoost: Boolean = false
)

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val quizRepository: com.akrubastudios.playquizgames.data.repository.QuizRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CountryState())
    val uiState = _uiState.asStateFlow()

    private val countryId: String = savedStateHandle.get<String>("countryId")!!

    // --- INICIO DE LA MODIFICACIÓN ---

    init {
        // Lanza la corrutina principal que escuchará los cambios y actualizará la UI.
        processCountryData()
    }

    private fun processCountryData() {
        viewModelScope.launch {
            // Ponemos el estado de carga al iniciar.
            _uiState.value = _uiState.value.copy(isScreenLoading = true)

            // 1. Cargamos TODOS los datos estáticos PRIMERO y los guardamos.
            val country = gameDataRepository.getCountry(countryId)
            val allCategories = gameDataRepository.getCategoryList()
            val allCountries = gameDataRepository.getCountryList()
            val bossQuiz = country?.bossLevelId?.let {
                if (it.isNotBlank()) quizRepository.getLevel(it) else null
            }

            // Si los datos estáticos cruciales fallan, salimos del estado de carga y mostramos error.
            if (country == null) {
                _uiState.value = _uiState.value.copy(isScreenLoading = false)
                return@launch
            }

            // 2. AHORA, nos suscribimos al flujo de datos dinámicos del usuario.
            gameDataRepository.userStateFlow.collect { userData ->
                if (userData != null) {
                    // Solo cuando tenemos tanto los datos estáticos como los del usuario,
                    // procedemos a calcular el estado final.

                    val initialProgress = gameDataRepository.getUserProgressForCountry(countryId)

                    val playableCountryIds = mutableSetOf<String>()
                    playableCountryIds.addAll(userData.availableCountries)
                    playableCountryIds.addAll(userData.conqueredCountries)
                    (userData.conqueredCountries + userData.dominatedCountries).toSet().forEach { influentialId ->
                        val influentialCountry = allCountries.find { it.countryId == influentialId }
                        influentialCountry?.neighbors?.forEach { neighborId ->
                            playableCountryIds.add(neighborId)
                        }
                    }

                    val status = when {
                        userData.dominatedCountries.contains(countryId) -> CountryStatus.DOMINATED
                        userData.conqueredCountries.contains(countryId) -> CountryStatus.CONQUERED
                        playableCountryIds.contains(countryId) -> CountryStatus.AVAILABLE
                        else -> CountryStatus.LOCKED
                    }

                    val filteredCategories = country.availableCategories.mapNotNull { (catId, isAvailable) ->
                        if (isAvailable || status == CountryStatus.DOMINATED) {
                            allCategories.find { it.categoryId == catId }
                        } else {
                            null
                        }
                    }

                    val canApplyBoost = (userData.unassignedPcBoosts > 0) && (status == CountryStatus.AVAILABLE)
                    val currentProgress = gameDataRepository.getUserProgressForCountry(countryId)

                    // 3. Actualizamos el estado final y APAGAMOS la carga.
                    _uiState.value = _uiState.value.copy(
                        country = country,
                        countryStatus = status,
                        availableCategories = filteredCategories,
                        studyTopics = bossQuiz?.studyTopics ?: emptyList(),
                        currentPc = currentProgress?.currentPc ?: initialProgress?.currentPc ?: 0,
                        pcRequired = country.pcRequired.takeIf { it > 0 } ?: 1,
                        isScreenLoading = false,
                        canApplyBoost = canApplyBoost
                    )
                }
                // Si 'userData' es nulo, 'isScreenLoading' permanece en true,
                // lo que es correcto hasta que tengamos un usuario válido.
            }
        }
    }

    /**
     * La función 'applyPcBoost' ahora es mucho más simple.
     * Ya no necesita el delay() ni la recarga forzada.
     */
    fun applyPcBoost() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("CountryViewModel", "No se puede aplicar boost, usuario nulo.")
            return
        }

        // Activamos el indicador del botón
        _uiState.value = _uiState.value.copy(isApplyingBoost = true)

        viewModelScope.launch {
            try {
                val boostRequest = hashMapOf(
                    "userId" to uid,
                    "countryId" to countryId,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("pc_boost_requests").add(boostRequest).await()
                Log.d("CountryViewModel", "✅ Petición de boost enviada.")
                // No hacemos nada más. La escucha en `processCountryData`
                // detectará automáticamente el cambio en `unassignedPcBoosts`
                // y refrescará toda la UI, apagando el `isApplyingBoost` y
                // actualizando el botón.
            } catch (e: Exception) {
                Log.e("CountryViewModel", "❌ Error al enviar la petición de boost.", e)
            } finally {
                // 2. PASE LO QUE PASE, apagamos el indicador al final.
                _uiState.value = _uiState.value.copy(isApplyingBoost = false)
            }
        }
    }
    // --- FIN DE LA MODIFICACIÓN ---
}