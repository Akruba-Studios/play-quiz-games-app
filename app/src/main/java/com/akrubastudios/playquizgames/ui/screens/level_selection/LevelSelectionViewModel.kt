package com.akrubastudios.playquizgames.ui.screens.level_selection

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.LevelStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.akrubastudios.playquizgames.core.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class LevelSelectionState(
    val levels: List<LevelStatus> = emptyList(),
    val categoryName: String = "",
    val isLoading: Boolean = true,
    val showLevelUnlockTutorialDialog: Boolean = false
)

@HiltViewModel
class LevelSelectionViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val languageManager: LanguageManager,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectionState())
    val uiState = _uiState.asStateFlow()

    // Estado para guardar la dificultad seleccionada. Por defecto, "principiante".
    private val _selectedDifficulty = MutableStateFlow("principiante")
    val selectedDifficulty = _selectedDifficulty.asStateFlow()

    /**
     * Se llama desde la UI cuando el usuario cambia la selección de dificultad.
     */
    fun onDifficultyChange(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    private val categoryId: String = savedStateHandle.get<String>("categoryId")!!
    private val continentId: String = savedStateHandle.get<String>("continentId")!!

    init {
        loadLevels()
    }

    private fun loadLevels() {
        viewModelScope.launch {
            // 1. Obtenemos TODOS los niveles de la base de datos
            val allLevels = gameDataRepository.getAllLevels()
            val userCompletions = gameDataRepository.getAllLevelCompletionData()
            val category = gameDataRepository.getCategory(categoryId)

            // --- LÓGICA DE FILTRADO Y ORDENAMIENTO ---
            // 2. Filtramos para quedarnos solo con los del continente y categoría correctos
            val levelsForThisScreen = allLevels.filter { level ->
                level.tierId == continentId && level.levelId.startsWith(categoryId)
            }.sortedBy { levelId -> // Ordenamos numéricamente
                levelId.levelId.filter { it.isDigit() }.toIntOrNull() ?: 0
            }

            // (El resto de la lógica de desbloqueo y mapeo se queda igual)
            val completionsMap = userCompletions.associateBy { it.levelId }
            languageManager.languageStateFlow.collect { langCode ->

                val userData = gameDataRepository.getUserData()
                val showTutorial = userData?.hasSeenLevelUnlockTutorial == false

                val levelStatuses = levelsForThisScreen.mapIndexed { index, level ->
                    val starsEarned = completionsMap[level.levelId]?.starsEarned ?: 0

                    val isLocked = if (index == 0) {
                        false
                    } else {
                        val previousLevelId = levelsForThisScreen[index - 1].levelId
                        val previousLevelStars = completionsMap[previousLevelId]?.starsEarned ?: 0
                        previousLevelStars < 2
                    }

                    LevelStatus(
                        levelId = level.levelId,
                        levelName = level.levelName[langCode] ?: level.levelName["es"]
                        ?: level.levelId,
                        starsEarned = starsEarned,
                        isLocked = isLocked
                    )
                }

                _uiState.value = LevelSelectionState(
                    levels = levelStatuses,
                    categoryName = category?.name?.get(langCode) ?: category?.name?.get("es")
                    ?: categoryId,
                    isLoading = false,
                    showLevelUnlockTutorialDialog = showTutorial
                )
            }
        }
    }
    fun levelUnlockTutorialShown() {
        val uid = auth.currentUser?.uid ?: return

        _uiState.value = _uiState.value.copy(showLevelUnlockTutorialDialog = false)

        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .update("hasSeenLevelUnlockTutorial", true)
            } catch (e: Exception) {
                // Manejar el error silenciosamente por ahora
                Log.e("LevelSelectionVM", "Error al actualizar hasSeenLevelUnlockTutorial", e)
            }
        }
    }
}