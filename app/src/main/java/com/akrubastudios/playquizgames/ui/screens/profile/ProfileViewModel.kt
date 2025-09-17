package com.akrubastudios.playquizgames.ui.screens.profile

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.akrubastudios.playquizgames.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * Estado de la UI para la pantalla de Perfil.
 */
// Representa una sola meta con su progreso.
data class GoalProgress(
    val description: String,
    val current: Int,
    val target: Int
)

// Representa el hito completo que se mostrará en la tarjeta.
data class Milestone(
    val title: String,
    val goals: List<GoalProgress>
)
data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val levelInfo: PlayerLevelManager.LevelInfo? = null,
    val nextMilestone: Milestone? = null,
    val triggerMilestoneAnimation: Boolean = false,
    val gems: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val authRepository: AuthRepository,
    private val application: Application,
    private val languageManager: LanguageManager,
    private val db: FirebaseFirestore,
    val musicManager: MusicManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    private val _signOutEvent = Channel<Unit>()
    val signOutEvent = _signOutEvent.receiveAsFlow()

    // --- INICIO DE LA MODIFICACIÓN ---

    init {
        // Nos suscribimos al flujo de datos del usuario.
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            // Combinamos el flujo de datos del usuario con el flujo del idioma.
            // El bloque se re-ejecutará si CUALQUIERA de los dos cambia.
            combine(
                gameDataRepository.userStateFlow,
                languageManager.languageStateFlow
            ) { currentUser, langCode ->
                // Usamos un Pair para manejar ambos valores. langCode actúa como disparador.
                Pair(currentUser, langCode)
            }.collect { (currentUser, langCode) ->

                // Si el usuario llega al perfil y tiene notificaciones pendientes, las limpiamos.
                if (currentUser != null && currentUser.pendingProfileNotifications.isNotEmpty()) {
                    clearProfileNotifications()
                }

                var shouldTriggerAnimation = false // <-- Variable local
                if (currentUser != null && currentUser.pendingProfileNotifications.isNotEmpty()) {
                    clearProfileNotifications()
                    shouldTriggerAnimation = true // <-- La activamos
                }

                //isLoading se sigue controlando igual.
                _uiState.value = _uiState.value.copy(isLoading = (currentUser == null))

                if (currentUser != null) {
                    val levelInfo = PlayerLevelManager.calculateLevelInfo(currentUser.totalXp)

                    // La llamada a determineNextMilestone ahora ocurre dentro del collect,
                    // por lo que se vuelve a ejecutar con el idioma actualizado.
                    val nextMilestone = determineNextMilestone(currentUser, levelInfo)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = currentUser,
                        levelInfo = levelInfo,
                        nextMilestone = nextMilestone,
                        triggerMilestoneAnimation = shouldTriggerAnimation,
                        gems = currentUser.gems
                    )
                }
            }
        }
    }

    private fun clearProfileNotifications() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Obtenemos una referencia a la base de datos (necesitarás inyectar 'db: FirebaseFirestore')
                db.collection("users").document(uid)
                    .update("pendingProfileNotifications", emptyList<String>())
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al limpiar notificaciones de perfil", e)
            }
        }
    }

    private fun getLocalizedResources(): android.content.res.Resources {
        val appLanguage = languageManager.languageStateFlow.value
        val locale = Locale(appLanguage)
        val config = Configuration(application.resources.configuration)
        config.setLocale(locale)
        return application.createConfigurationContext(config).resources
    }

    private fun determineNextMilestone(user: User, levelInfo: PlayerLevelManager.LevelInfo): Milestone? {
        val conqueredCount = user.conqueredCountries.size

        // --- Hito 1: Primera Expedición ---
        // Condiciones para mostrar este hito:
        // 1. El jugador aún no ha cumplido AMBAS metas (nivel 5 Y 3 conquistas).
        // 2. Opcional: Podríamos añadir una comprobación de continentes si fuera necesario,
        //    pero basarse en el número de conquistas es más simple y robusto.
        val localizedResources = getLocalizedResources()
        val targetLevel1 = 7
        val targetConquests1 = 3
        if (levelInfo.level < targetLevel1 || conqueredCount < targetConquests1) {
            return Milestone(
                title = localizedResources.getString(R.string.milestone_title_expedition_1),
                goals = listOf(
                    GoalProgress(
                        description = localizedResources.getString(R.string.milestone_goal_level, targetLevel1),
                        current = levelInfo.level,
                        target = targetLevel1
                    ),
                    GoalProgress(
                        description = localizedResources.getString(R.string.milestone_goal_conquests, targetConquests1),
                        current = conqueredCount,
                        target = targetConquests1
                    )
                )
            )
        }

        // --- Hito 2: Segunda Expedición ---
        // Se muestra solo si ya se cumplió el anterior.
        val targetLevel2 = 10
        val targetConquests2 = 7
        if (levelInfo.level < targetLevel2 || conqueredCount < targetConquests2) {
            return Milestone(
                title = localizedResources.getString(R.string.milestone_title_expedition_2),
                goals = listOf(
                    GoalProgress(
                        description = localizedResources.getString(R.string.milestone_goal_level, targetLevel2),
                        current = levelInfo.level,
                        target = targetLevel2
                    ),
                    GoalProgress(
                        description = localizedResources.getString(R.string.milestone_goal_conquests, targetConquests2),
                        current = conqueredCount,
                        target = targetConquests2
                    )
                )
            )
        }

        // Si todos los hitos definidos se han cumplido, no devolvemos nada.
        return null
    }

    fun resetMilestoneAnimation() {
        _uiState.update { it.copy(triggerMilestoneAnimation = false) }
    }

    fun signOut() {
        Log.d("SignOut_Debug", "[PASO 1] ProfileViewModel.signOut() llamado.")
        viewModelScope.launch {
            authRepository.signOut()
            _signOutEvent.send(Unit)
            Log.d("SignOut_Debug", "[PASO 4] Evento de navegación enviado.")
        }
    }
}