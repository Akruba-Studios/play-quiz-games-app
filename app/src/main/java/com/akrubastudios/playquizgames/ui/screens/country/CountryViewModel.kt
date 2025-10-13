package com.akrubastudios.playquizgames.ui.screens.country

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Category
import com.akrubastudios.playquizgames.domain.Country
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val countryName: String = "",
    val countryStatus: CountryStatus = CountryStatus.LOCKED,
    val availableCategories: List<CategoryState> = emptyList(),
    val studyTopics: List<String> = emptyList(),
    val currentPc: Long = 0,
    val pcRequired: Long = 1, // Evita división por cero
    val isScreenLoading: Boolean = true, // Para la carga inicial
    val isApplyingBoost: Boolean = false,
    val canApplyBoost: Boolean = false,
    val showConquestTutorialDialog: Boolean = false,
    val isDominated: Boolean = false,
    val guardianName: String = ""
)
// Representará una categoría con su nombre ya localizado.
data class CategoryState(
    val categoryId: String,
    val name: String
)

@HiltViewModel
class CountryViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val quizRepository: com.akrubastudios.playquizgames.data.repository.QuizRepository,
    private val languageManager: LanguageManager,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val savedStateHandle: SavedStateHandle,
    val musicManager: MusicManager,
    val imageLoader: ImageLoader
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(CountryState())
    val uiState = _uiState.asStateFlow()

    private val countryId: String = savedStateHandle.get<String>("countryId")!!

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        musicManager.play(MusicTrack.MAP)
    }

    init {
        gameDataRepository.startCountryProgressListener(countryId)
        // Lanza la corrutina principal que escuchará los cambios y actualizará la UI.
        processCountryData()
    }

    // REEMPLAZAR COMPLETAMENTE LA FUNCIÓN processCountryData
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

            // Cargamos los datos del tema visual que contienen el nombre del guardián.
            val countryVisualTheme = quizRepository.getCountryVisualTheme(countryId)

            // Si los datos estáticos cruciales fallan, salimos del estado de carga y mostramos error.
            if (country == null) {
                _uiState.value = _uiState.value.copy(isScreenLoading = false)
                return@launch
            }

            // 2. NUEVO: Combinamos ambos flujos de datos usando combine()
            combine(
                gameDataRepository.userStateFlow,
                gameDataRepository.userCountryProgressStateFlow,
                languageManager.languageStateFlow
            ) { userData, countryProgress, langCode ->
                Log.d("LanguageDebug", "[PASO 5] CountryViewModel: 'combine' se ha disparado con el idioma -> '$langCode'")
                // Creamos una tupla triple para manejar los tres valores.
                Triple(userData, countryProgress, langCode)
            }.collect { (userData, countryProgress, langCode) -> // <-- DESESTRUCTURAMOS LA TUPLA

                if (userData != null) {

                    val showTutorial = !userData.hasSeenConquestTutorial
                    // Solo cuando tenemos tanto los datos estáticos como los del usuario,
                    // procedemos a calcular el estado final.

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
                        // --- INICIO DE LA MODIFICACIÓN ---
                        // ANTES:
                        // if (isAvailable || status == CountryStatus.DOMINATED) {

                        // DESPUÉS (eliminamos la condición de dominación):
                        if (isAvailable) {
                            // --- FIN DE LA MODIFICACIÓN ---
                            allCategories.find { it.categoryId == catId }
                        } else {
                            null
                        }
                    }

                    val canApplyBoost = (userData.unassignedPcBoosts > 0) && (status == CountryStatus.AVAILABLE)

                    // NUEVO: Usar el progreso del país del listener en tiempo real
                    val currentPc = countryProgress?.currentPc ?: 0

                    val countryNameForUi = country.name[langCode] ?: country.name["es"] ?: country.countryId

                    // Mapeamos la lista de categorías a la nueva clase CategoryState con el nombre ya localizado.
                    val categoriesForUi = filteredCategories.map { category ->
                        CategoryState(
                            categoryId = category.categoryId,
                            name = category.name[langCode] ?: category.name["es"] ?: category.categoryId
                        )
                    }

                    // 3. Actualizamos el estado final y APAGAMOS la carga.
                    _uiState.value = _uiState.value.copy(
                        country = country,
                        countryName = countryNameForUi,
                        countryStatus = status,
                        availableCategories = categoriesForUi,
                        studyTopics = bossQuiz?.studyTopics?.get(langCode) ?: emptyList(),
                        currentPc = currentPc,
                        pcRequired = country.pcRequired.takeIf { it > 0 } ?: 1,
                        isScreenLoading = false,
                        canApplyBoost = canApplyBoost,
                        showConquestTutorialDialog = showTutorial,
                        isDominated = (status == CountryStatus.DOMINATED),
                        guardianName = countryVisualTheme?.guardianData?.name?.get(langCode)
                            ?: countryVisualTheme?.guardianData?.name?.get("es")
                            ?: ""
                    )

                    Log.d("CountryViewModel", "🔄 Estado actualizado - PC: $currentPc, Status: $status")
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

        // --- INICIO DE LA MODIFICACIÓN ---

        // 1. Inicia el efecto visual INMEDIATAMENTE.
        _uiState.value = _uiState.value.copy(isApplyingBoost = true)

        viewModelScope.launch {
            // 2. Lanza la petición a Firebase en una corrutina separada.
            //    Esto se ejecuta en segundo plano y no bloquea el efecto visual.
            launch {
                try {
                    val boostRequest = hashMapOf(
                        "userId" to uid,
                        "countryId" to countryId,
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("pc_boost_requests").add(boostRequest).await()
                    Log.d("CountryViewModel", "✅ Petición de boost enviada.")
                } catch (e: Exception) {
                    Log.e("CountryViewModel", "❌ Error al enviar la petición de boost.", e)
                    // Si falla la petición, detenemos la animación inmediatamente para
                    // que el usuario sepa que algo salió mal.
                    _uiState.value = _uiState.value.copy(isApplyingBoost = false)
                }
            }

            // 3. Mientras la petición se envía en segundo plano, la corrutina principal
            //    espera 2 segundos para el efecto dramático.
            delay(3000L) // 3000 milisegundos = 3 segundos

            // 4. Después de la espera, detenemos el efecto visual.
            //    La actualización de los datos (el nuevo valor de PC) llegará
            //    automáticamente a través del listener del userStateFlow.
            _uiState.value = _uiState.value.copy(isApplyingBoost = false)
        }
        // --- FIN DE LA MODIFICACIÓN ---
    }
    fun conquestTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        // Ocultamos el diálogo en la UI.
        _uiState.value = _uiState.value.copy(showConquestTutorialDialog = false)

        viewModelScope.launch {
            try {
                // Actualizamos la bandera en Firestore.
                db.collection("users").document(uid)
                    .update("hasSeenConquestTutorial", true)
            } catch (e: Exception) {
                Log.e("CountryViewModel", "Error al actualizar hasSeenConquestTutorial", e)
            }
        }
    }
}