package com.akrubastudios.playquizgames.ui.screens.map

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class MapState(
    val countries: List<Country> = emptyList(),
    val conqueredCountryIds: List<String> = emptyList(),
    val dominatedCountryIds: List<String> = emptyList(),
    val availableCountryIds: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val playerLevelInfo: PlayerLevelManager.LevelInfo? = null,
    val expeditionAvailable: Boolean = false,
    val availableExpeditions: List<Pair<String, String>> = emptyList(),
    val pendingBossChallenge: String? = null,
    val unassignedPcBoosts: Int = 0,
    val showWelcomeDialog: Boolean = false,
    val firstCountryName: String = "",
    val showFreeModeUnlockedDialog: Boolean = false,
    val showDominationRewardsSheet: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val application: Application,
    private val languageManager: LanguageManager
) : ViewModel() {

    val currentUser = authRepository.currentUser

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    private fun getLocalizedResources(): android.content.res.Resources {
        val appLanguage = languageManager.languageStateFlow.value
        val locale = Locale(appLanguage)
        val config = Configuration(application.resources.configuration)
        config.setLocale(locale)
        return application.createConfigurationContext(config).resources
    }

    // --- INICIO DE LA MODIFICACIÓN ---

    init {
        // 1. Inicia la escucha global de datos del usuario
        gameDataRepository.startUserDataListener()
        // 2. Lanza la corrutina para procesar los datos
        processUserData()
    }

    private fun processUserData() {
        viewModelScope.launch {
            // Carga la lista de países una sola vez, ya que es estática.
            val countryList = gameDataRepository.getCountryList()

            // Se suscribe al StateFlow compartido del repositorio.
            // 'collect' se ejecutará cada vez que los datos del usuario cambien en Firestore.
            gameDataRepository.userStateFlow.collect { userData ->

                // Ponemos el estado de carga solo si no tenemos datos de usuario aún.
                if (_uiState.value.playerLevelInfo == null) {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }

                if (userData != null) {
                    var showWelcome = false
                    var welcomeCountryName = ""
                    if (!userData.hasSeenWelcomeDialog && userData.availableCountries.isNotEmpty()) {
                        showWelcome = true
                        val firstCountryId = userData.availableCountries.first()
                        val firstCountry = countryList.find { it.countryId == firstCountryId }
                        val lang = Locale.getDefault().language
                        welcomeCountryName = firstCountry?.name?.get(lang) ?: firstCountry?.name?.get("es") ?: ""
                    }

                    val levelInfo = PlayerLevelManager.calculateLevelInfo(userData.totalXp)
                    var showFreeModeDialog = false
                    // La condición: ha llegado al nivel 5 y NUNCA ha visto el diálogo.
                    if (levelInfo.level >= 5 && !userData.hasSeenFreeModeUnlockedDialog) {
                        showFreeModeDialog = true
                    }

                    var showDominationSheet = false
                    // La condición: El usuario NUNCA ha visto el tutorial Y
                    // AHORA tiene al menos un país dominado.
                    if (!userData.hasSeenDominationTutorial && userData.dominatedCountries.isNotEmpty()) {
                        showDominationSheet = true
                    }

                    val conqueredIds = userData.conqueredCountries
                    val availableIdsFromDB = userData.availableCountries
                    val dominatedIds = userData.dominatedCountries

                    // --- La lógica de cálculo de expediciones y vecinos no cambia ---
                    val unlockedContinents = (conqueredIds + availableIdsFromDB)
                        .mapNotNull { countryId -> countryList.find { it.countryId == countryId }?.continentId }
                        .toSet()

                    val allPossibleExpeditions = mapOf(
                        "europe" to getLocalizedResources().getString(R.string.continent_europe),
                        "north_america" to getLocalizedResources().getString(R.string.continent_north_america),
                        "south_america" to getLocalizedResources().getString(R.string.continent_south_america)
                    )

                    val filteredExpeditions = allPossibleExpeditions
                        .filter { !unlockedContinents.contains(it.key) }
                        .map { (continentId, continentName) ->
                            // Construimos el texto "Explorar..."
                            val buttonText = getLocalizedResources().getString(R.string.expedition_dialog_button_explore, continentName)
                            Pair(continentId, buttonText)
                        }

                    var isExpeditionAvailable = false
                    if (unlockedContinents.size == 1 && conqueredIds.size >= 3 && levelInfo.level >= 5) {
                        isExpeditionAvailable = true
                    } else if (unlockedContinents.size == 2 && conqueredIds.size >= 6 && levelInfo.level >= 10) {
                        isExpeditionAvailable = true
                    }

                    val influentialCountryIds = (conqueredIds + dominatedIds).toSet()
                    val availableIds = mutableSetOf<String>()
                    availableIds.addAll(availableIdsFromDB)
                    influentialCountryIds.forEach { influentialId ->
                        val influentialCountry = countryList.find { it.countryId == influentialId }
                        influentialCountry?.neighbors?.forEach { neighborId ->
                            availableIds.add(neighborId)
                        }
                    }

                    _uiState.value = MapState(
                        countries = countryList,
                        conqueredCountryIds = conqueredIds,
                        dominatedCountryIds = dominatedIds,
                        availableCountryIds = availableIds.toList(),
                        isLoading = false,
                        playerLevelInfo = levelInfo,
                        expeditionAvailable = isExpeditionAvailable,
                        availableExpeditions = filteredExpeditions,
                        pendingBossChallenge = userData.pendingBossChallenge,
                        unassignedPcBoosts = userData.unassignedPcBoosts,
                        showWelcomeDialog = showWelcome,
                        firstCountryName = welcomeCountryName,
                        showFreeModeUnlockedDialog = showFreeModeDialog,
                        showDominationRewardsSheet = showDominationSheet
                    )
                }
            }
        }
    }

    /**
     * Limpia la "bandera" de desafío de jefe pendiente en Firestore.
     * Se llama después de que el usuario interactúa con el AlertDialog.
     */
    fun clearPendingBossChallenge() {
        _uiState.value = _uiState.value.copy(pendingBossChallenge = null)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("MapViewModel", "No se puede limpiar el desafío, usuario nulo.")
            return
        }

        viewModelScope.launch {
            try {
                val userRef = db.collection("users").document(uid)
                // Usamos FieldValue.delete() para eliminar completamente el campo del documento.
                userRef.update("pendingBossChallenge", com.google.firebase.firestore.FieldValue.delete())
                Log.d("MapViewModel", "✅ Bandera pendingBossChallenge eliminada.")
            } catch (e: Exception) {
                Log.e("MapViewModel", "❌ Error al limpiar la bandera pendingBossChallenge.", e)
            }
        }
    }

    /**
     * Se llama cuando el usuario selecciona un continente en el diálogo de expedición.
     * TODO: La lógica para desbloquear el contenido se implementará en el siguiente paso.
     */
    fun onExpeditionContinentSelected(continentId: String) {

        // Determinamos qué país "puerta de entrada" desbloquear.
        // Esta lógica puede expandirse en el futuro.
        val countryToUnlock = when (continentId) {
            "europe" -> "es" // España para Europa
            "north_america" -> "mx" // México para Norteamérica
            "south_america" -> "bo" // Bolivia para Sudamérica
            // Añadir más continentes aquí
            else -> null
        }

        if (countryToUnlock == null) {
            android.util.Log.e("MapViewModel", "No se encontró un país de entrada para el continente: $continentId")
            return
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            android.util.Log.e("MapViewModel", "Usuario nulo, no se puede desbloquear el país.")
            return
        }

        // Lanzamos una coroutine para actualizar Firestore en segundo plano.
        viewModelScope.launch {
            try {
                val userRef = db.collection("users").document(uid)
                // Usamos FieldValue.arrayUnion para añadir el nuevo país a la lista
                // de forma segura, evitando duplicados.
                userRef.update("availableCountries", com.google.firebase.firestore.FieldValue.arrayUnion(countryToUnlock))
                android.util.Log.d("MapViewModel", "✅ País '$countryToUnlock' añadido a availableCountries para el usuario $uid.")

                _uiState.value = _uiState.value.copy(expeditionAvailable = false)

            } catch (e: Exception) {
                android.util.Log.e("MapViewModel", "❌ Error al desbloquear el nuevo país.", e)
                // Opcional: Podríamos mostrar un mensaje de error al usuario aquí.
            }
        }
    }

    /**
     * Se llama cuando el usuario decide posponer la expedición.
     * Oculta el diálogo para la sesión actual.
     */
    fun dismissExpeditionDialog() {
        // Ponemos expeditionAvailable a false en el estado de la UI para ocultar el diálogo.
        // La próxima vez que se carguen los datos del usuario, la condición se re-evaluará.
        _uiState.value = _uiState.value.copy(expeditionAvailable = false)
    }
    fun welcomeDialogShown() {
        val uid = auth.currentUser?.uid ?: return

        // Ocultamos el diálogo inmediatamente en la UI para una respuesta rápida.
        _uiState.value = _uiState.value.copy(showWelcomeDialog = false)

        viewModelScope.launch {
            try {
                // Actualizamos la bandera en Firestore para que no se vuelva a mostrar.
                db.collection("users").document(uid)
                    .update("hasSeenWelcomeDialog", true)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error al actualizar hasSeenWelcomeDialog", e)
            }
        }
    }
    fun freeModeTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        // Ocultamos el diálogo en la UI.
        _uiState.value = _uiState.value.copy(showFreeModeUnlockedDialog = false)

        viewModelScope.launch {
            try {
                // Actualizamos la bandera en Firestore.
                db.collection("users").document(uid)
                    .update("hasSeenFreeModeUnlockedDialog", true)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error al actualizar hasSeenFreeModeUnlockedDialog", e)
            }
        }
    }
    fun dominationTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        // Ocultamos el Bottom Sheet en la UI.
        _uiState.value = _uiState.value.copy(showDominationRewardsSheet = false)

        viewModelScope.launch {
            try {
                // Actualizamos la bandera en Firestore.
                db.collection("users").document(uid)
                    .update("hasSeenDominationTutorial", true)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error al actualizar hasSeenDominationTutorial", e)
            }
        }
    }
}

