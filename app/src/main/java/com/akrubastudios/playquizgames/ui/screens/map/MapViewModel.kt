package com.akrubastudios.playquizgames.ui.screens.map

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.AdManager
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicManager
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject

data class MapState( // Control: 3-MVM
    val countries: List<Country> = emptyList(),
    val conqueredCountryIds: List<String> = emptyList(),
    val dominatedCountryIds: List<String> = emptyList(),
    val availableCountryIds: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val playerLevelInfo: PlayerLevelManager.LevelInfo? = null,
    val expeditionAvailable: Boolean = false,
    val showExpeditionDialog: Boolean = false,
    val availableExpeditions: List<Pair<String, String>> = emptyList(),
    val unassignedPcBoosts: Int = 0,
    val showWelcomeDialog: Boolean = false,
    val firstCountryName: String = "",
    val showFreeModeUnlockedDialog: Boolean = false,
    val showDominationRewardsSheet: Boolean = false,
    val hasProfileNotification: Boolean = false,
    val gems: Int = 0,
    val isRewardedAdLoading: Boolean = true, // Inicia en true porque cargamos el anuncio al inicio
    val isRewardCooldownActive: Boolean = false,
    val rewardCooldownSeconds: Int = 0,
    val showRewardDialog: Boolean = false,
    val isRewardFeatureUnlocked: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameDataRepository: GameDataRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val application: Application,
    private val languageManager: LanguageManager,
    private val settingsRepository: SettingsRepository,
    val musicManager: MusicManager,
    val imageLoader: ImageLoader, // <-- AÑADE ESTA LÍNEA
    @ApplicationContext private val context: Context
) : ViewModel(), DefaultLifecycleObserver {

    val currentUser = authRepository.currentUser

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        musicManager.play(MusicTrack.MAP)
    }

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
        // Lanzamos una corutina separada que se ejecuta UNA SOLA VEZ.
        viewModelScope.launch {
            // 1. Obtenemos la lista de países (que es estática) una vez.
            val countryList = gameDataRepository.getCountryList()
            // 2. Ejecutamos la precarga.
            prefetchCountryBackgrounds(countryList)
        }
        // Precargamos un anuncio bonificado al iniciar la pantalla del mapa.
        AdManager.loadRewardedAd(application)
    }

    private fun processUserData() {
        viewModelScope.launch {

            // Carga la lista de países una sola vez, ya que es estática.
            val countryList = gameDataRepository.getCountryList()

            gameDataRepository.userStateFlow.collect { userData ->

                val dismissedLevel = settingsRepository.dismissedExpeditionLevelFlow.first()

                if (_uiState.value.playerLevelInfo == null) {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    delay(0) // ESTE DELAY SE QUEDA solo para probar el cicular progrees al iniciar mapscreen
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

                    // Lógica para determinar si la función de recompensa está desbloqueada
                    val isRewardUnlocked = userData.conqueredCountries.isNotEmpty()
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
                        unassignedPcBoosts = userData.unassignedPcBoosts,
                        showWelcomeDialog = showWelcome,
                        firstCountryName = welcomeCountryName,
                        showFreeModeUnlockedDialog = showFreeModeDialog,
                        showDominationRewardsSheet = showDominationSheet,
                        hasProfileNotification = hasNotification,
                        gems = userData.gems,
                        isRewardFeatureUnlocked = isRewardUnlocked
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
     * Pre-carga una única imagen de forma controlada usando Listeners y coroutines.
     * Devuelve true si la carga fue exitosa (desde red o disco).
     */
    private suspend fun precacheSingleImage(imageUrl: String): Boolean {
        if (imageUrl.isBlank()) return false

        return suspendCancellableCoroutine { continuation ->
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.DISABLED) // No necesitamos el bitmap en RAM ahora mismo
                .listener(
                    onSuccess = { _, result ->
                        Log.d("MapViewModel_Precache", "✅ Imagen precargada desde ${result.dataSource}: ${imageUrl.takeLast(20)}")
                        if (continuation.isActive) continuation.resume(true)
                    },
                    onError = { _, result ->
                        Log.e("MapViewModel_Precache", "❌ Error al precargar: ${result.throwable}")
                        if (continuation.isActive) continuation.resume(false)
                    }
                )
                .build()

            imageLoader.enqueue(request)

            continuation.invokeOnCancellation {
                // Si la corutina se cancela, no hacemos nada extra,
                // Coil manejará la cancelación de la petición.
            }
        }
    }

    /**
     * Pre-carga múltiples imágenes de fondo en paralelo.
     */
    private suspend fun prefetchCountryBackgrounds(countries: List<Country>) = coroutineScope {
        val imageUrls = countries
            .filter { it.backgroundImageUrl.isNotBlank() }
            .map { it.backgroundImageUrl }

        if (imageUrls.isEmpty()) return@coroutineScope

        Log.d("MapViewModel_Precache", "🚀 Iniciando precarga de ${imageUrls.size} fondos de país...")

        val jobs = imageUrls.map { url ->
            async(Dispatchers.IO) {
                precacheSingleImage(url)
            }
        }
        jobs.awaitAll()
        Log.d("MapViewModel_Precache", "✅ Precarga de fondos de país completada.")
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
    // --- INICIO DE LAS NUEVAS FUNCIONES PARA ANUNCIOS BONIFICADOS ---

    /**
     * Se llama desde la UI para mostrar el diálogo de confirmación.
     */
    fun onShowRewardDialog() {
        _uiState.update { it.copy(showRewardDialog = true) }
    }

    /**
     * Se llama desde la UI para cerrar el diálogo de confirmación.
     */
    fun onDismissRewardDialog() {
        _uiState.update { it.copy(showRewardDialog = false) }
    }

    /**
     * Se llama cuando el usuario confirma que quiere ver el anuncio.
     */
    fun onClaimRewardConfirmed(activity: Activity) {
        // Ocultamos el diálogo y mostramos un estado de carga en el botón
        _uiState.update { it.copy(showRewardDialog = false, isRewardedAdLoading = true) }

        // Mostramos el anuncio
        AdManager.showRewardedAd(activity, musicManager) {
            // Este bloque se ejecuta si el usuario ve el video completo.
            viewModelScope.launch {
                Log.d("MapViewModel", "Recompensa ganada. Creando petición en Firestore.")
                createGemRewardRequest() // Creamos la petición segura en Firestore
                startCooldownTimer()   // Iniciamos el contador de 60 segundos
            }
        }

        // Volvemos a cargar el siguiente anuncio para que esté listo.
        // Damos un pequeño delay para no interferir con el anuncio que se está mostrando.
        viewModelScope.launch {
            delay(1000L) // Espera 1 segundo
            AdManager.loadRewardedAd(application)
            // Cuando el nuevo anuncio termine de cargar (o falle), ocultamos el loader.
            // La UI reaccionará al estado de `rewardedAd` en `AdManager`.
            _uiState.update { it.copy(isRewardedAdLoading = false) }
        }
    }

    /**
     * Crea un documento en Firestore para que el trigger del backend lo procese.
     */
    private fun createGemRewardRequest() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Log.e("MapViewModel", "Usuario nulo, no se puede crear la petición de gemas.")
            return
        }

        // El token de verificación se obtendría del SDK de AdMob y se enviaría aquí.
        // Para nuestro flujo actual con un trigger en modo DEV, un placeholder es suficiente.
        val rewardRequest = hashMapOf(
            "userId" to uid,
            "timestamp" to FieldValue.serverTimestamp(),
            "verificationToken" to "placeholder_for_dev"
        )

        db.collection("gem_reward_requests").add(rewardRequest)
            .addOnSuccessListener {
                Log.d("MapViewModel", "✅ Petición de recompensa de gemas creada con éxito.")
            }
            .addOnFailureListener { e ->
                Log.e("MapViewModel", "❌ Error al crear la petición de recompensa de gemas.", e)
            }
    }

    /**
     * Inicia un temporizador de 60 segundos durante el cual no se puede pedir otra recompensa.
     */
    private fun startCooldownTimer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRewardCooldownActive = true) }
            for (i in 60 downTo 1) {
                _uiState.update { it.copy(rewardCooldownSeconds = i) }
                delay(1000L)
            }
            _uiState.update { it.copy(isRewardCooldownActive = false, rewardCooldownSeconds = 0) }
        }
    }
}

