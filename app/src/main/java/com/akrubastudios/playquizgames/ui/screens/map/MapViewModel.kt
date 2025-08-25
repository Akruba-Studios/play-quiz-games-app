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
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val showExpeditionDialog: Boolean = false,
    val availableExpeditions: List<Pair<String, String>> = emptyList(),
    val pendingBossChallenge: String? = null,
    val unassignedPcBoosts: Int = 0,
    val showWelcomeDialog: Boolean = false,
    val firstCountryName: String = "",
    val showFreeModeUnlockedDialog: Boolean = false,
    val showDominationRewardsSheet: Boolean = false,
    val hasProfileNotification: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val application: Application,
    private val languageManager: LanguageManager,
    private val settingsRepository: SettingsRepository
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
            combine(
                gameDataRepository.userStateFlow,
                settingsRepository.dismissedExpeditionLevelFlow
            ) { userData, dismissedLevel ->
                Pair(userData, dismissedLevel)
            }.collect { (userData, dismissedLevel) ->

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

                    var expeditionTier = 0
                    if (unlockedContinents.size == 1 && conqueredIds.size >= 3 && levelInfo.level >= 7) {
                        expeditionTier = 1
                    } else if (unlockedContinents.size == 2 && conqueredIds.size >= 7 && levelInfo.level >= 10) {
                        expeditionTier = 2
                    }
                    val showAutoDialog = expeditionTier > dismissedLevel && filteredExpeditions.isNotEmpty()

                    val influentialCountryIds = (conqueredIds + dominatedIds).toSet()
                    val availableIds = mutableSetOf<String>()
                    availableIds.addAll(availableIdsFromDB)
                    influentialCountryIds.forEach { influentialId ->
                        val influentialCountry = countryList.find { it.countryId == influentialId }
                        influentialCountry?.neighbors?.forEach { neighborId ->
                            availableIds.add(neighborId)
                        }
                    }

                    // --- INICIO DE LA MODIFICACIÓN: Notificacion de Detección de Hitos ---
                    // Guardamos el estado anterior para comparar.
                    val previousState = _uiState.value

                    // Hito 1: Dos Conquistas
                    // Condición: Ha conquistado 2 o más países Y NUNCA se ha disparado este hito antes.
                    if (userData.conqueredCountries.size >= 2 && !userData.hasTriggeredConquest2Milestone) {
                        // Añadimos la notificación y actualizamos la bandera para que no vuelva a pasar.
                        addProfileNotificationAndUpdateFlag("CONQUEST_2_MILESTONE", "hasTriggeredConquest2Milestone")
                    }

                    // Hito 2: Expansión Intercontinental
                    // Reutilizamos la lógica de 'unlockedContinents' que ya tienes
                    if (unlockedContinents.size > 1 && !userData.hasTriggeredExpansionMilestone) {
                        addProfileNotificationAndUpdateFlag("INTERCONTINENTAL_EXPANSION_MILESTONE", "hasTriggeredExpansionMilestone")
                    }

                    val hasNotification = userData.pendingProfileNotifications.isNotEmpty()
                    // --- FIN DE LA MODIFICACIÓN ---

                    _uiState.value = _uiState.value.copy(
                        countries = countryList,
                        conqueredCountryIds = conqueredIds,
                        dominatedCountryIds = dominatedIds,
                        availableCountryIds = availableIds.toList(),
                        isLoading = false,
                        playerLevelInfo = levelInfo,
                        expeditionAvailable = expeditionTier > 0 && filteredExpeditions.isNotEmpty(),
                        showExpeditionDialog = showAutoDialog,
                        availableExpeditions = filteredExpeditions,
                        pendingBossChallenge = userData.pendingBossChallenge,
                        unassignedPcBoosts = userData.unassignedPcBoosts,
                        showWelcomeDialog = showWelcome,
                        firstCountryName = welcomeCountryName,
                        showFreeModeUnlockedDialog = showFreeModeDialog,
                        showDominationRewardsSheet = showDominationSheet,
                        hasProfileNotification = hasNotification
                    )
                }
            }
        }
    }

    private fun addProfileNotificationAndUpdateFlag(notificationId: String, flagName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid).update(
                    mapOf(
                        "pendingProfileNotifications" to FieldValue.arrayUnion(notificationId),
                        flagName to true
                    )
                )
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error al añadir notificación y bandera: $flagName", e)
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
        // Ocultamos el diálogo en la UI.
        _uiState.value = _uiState.value.copy(showExpeditionDialog = false)

        viewModelScope.launch {
            // Calculamos qué nivel de oferta está activo AHORA.
            val ui = _uiState.value
            val conqueredIds = ui.conqueredCountryIds
            val unlockedContinents = ui.countries
                .filter { conqueredIds.contains(it.countryId) || ui.availableCountryIds.contains(it.countryId) }
                .map { it.continentId }.toSet()

            var currentTier = 0
            if (unlockedContinents.size == 1) currentTier = 1
            else if (unlockedContinents.size == 2) currentTier = 2

            // Guardamos el nivel que acabamos de ignorar.
            if (currentTier > 0) {
                settingsRepository.saveDismissedExpeditionLevel(currentTier)
            }
        }
    }
    fun requestExpeditionDialog() {
        _uiState.value = _uiState.value.copy(showExpeditionDialog = true)
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

