package com.akrubastudios.playquizgames.ui.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapState(
    val countries: List<Country> = emptyList(),
    val conqueredCountryIds: List<String> = emptyList(),
    val availableCountryIds: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val playerLevelInfo: PlayerLevelManager.LevelInfo? = null,
    val expeditionAvailable: Boolean = false,
    val availableExpeditions: List<Pair<String, String>> = emptyList(),
    val pendingBossChallenge: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameDataRepository: GameDataRepository, // <-- Inyectamos el nuevo repo
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    val currentUser = authRepository.currentUser

    private val _uiState = MutableStateFlow(MapState())
    val uiState: StateFlow<MapState> = _uiState.asStateFlow()

    init {
        listenToUserData()
    }

    private fun listenToUserData() {
        viewModelScope.launch {
            val countryList = gameDataRepository.getCountryList()

            // Ponemos el estado de carga explícitamente al empezar a escuchar
            _uiState.value = MapState(isLoading = true)

            gameDataRepository.getUserDataFlow().collect { userData ->

                // Si userData es null la primera vez, el estado de carga se mantendrá
                // y la UI mostrará el indicador.

                if (userData != null) {
                    // 1. Llamamos a nuestro gestor para calcular toda la información del nivel
                    //    basándonos en el XP total del usuario que acabamos de recibir.
                    val levelInfo = PlayerLevelManager.calculateLevelInfo(userData.totalXp)

                    val conqueredIds = userData.conqueredCountries
                    val availableIdsFromDB = userData.availableCountries

                    // 1. Identificamos los continentes que el jugador ya ha "tocado".
                    val unlockedContinents = (conqueredIds + availableIdsFromDB)
                        .mapNotNull { countryId -> countryList.find { it.countryId == countryId }?.continentId }
                        .toSet()

                    // --- INICIO DE LA LÓGICA DE FILTRADO DE EXPEDICIONES ---
                    val allPossibleExpeditions = mapOf(
                        "europe" to "Explorar Europa",
                        "north_america" to "Explorar Norteamérica",
                        "south_america" to "Explorar Sudamérica"
                        // Añadir más continentes aquí en el futuro
                    )

                    // Filtramos el mapa para quedarnos solo con las expediciones a continentes NO desbloqueados.
                    val filteredExpeditions = allPossibleExpeditions
                        .filter { !unlockedContinents.contains(it.key) }
                        .map { Pair(it.key, it.value) } // Convertimos el mapa filtrado a una lista de pares.

                    // 2. Definimos las reglas para las expediciones.
                    var isExpeditionAvailable = false
                    if (unlockedContinents.size == 1 && conqueredIds.size >= 3 && levelInfo.level >= 5) {
                        // Condición para la PRIMERA expedición (desbloquear el 2º continente).
                        isExpeditionAvailable = true
                    } else if (unlockedContinents.size == 2 && conqueredIds.size >= 6 && levelInfo.level >= 10) {
                        // Condición para la SEGUNDA expedición (desbloquear el 3er continente).
                        isExpeditionAvailable = true
                    }
                    // ... Podemos añadir más reglas "else if" para futuras expediciones.

                    val availableIds = mutableSetOf<String>()
                    availableIdsFromDB.forEach { availableIds.add(it) }
                    conqueredIds.forEach { conqueredId ->
                        val conqueredCountry = countryList.find { it.countryId == conqueredId }
                        conqueredCountry?.neighbors?.forEach { neighborId ->
                            availableIds.add(neighborId)
                        }
                    }

                    _uiState.value = MapState(
                        countries = countryList,
                        conqueredCountryIds = conqueredIds,
                        availableCountryIds = availableIds.toList(),
                        isLoading = false, // <-- Solo ponemos isLoading a false cuando tenemos datos
                        playerLevelInfo = levelInfo,
                        expeditionAvailable = isExpeditionAvailable,
                        availableExpeditions = filteredExpeditions,
                        pendingBossChallenge = userData.pendingBossChallenge
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
}

