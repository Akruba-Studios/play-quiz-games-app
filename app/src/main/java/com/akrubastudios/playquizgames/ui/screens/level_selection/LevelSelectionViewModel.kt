package com.akrubastudios.playquizgames.ui.screens.level_selection

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

data class LevelSelectionState(
    val levels: List<LevelStatus> = emptyList(),
    val categoryName: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class LevelSelectionViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectionState())
    val uiState = _uiState.asStateFlow()

    private val categoryId: String = savedStateHandle.get<String>("categoryId")!!

    init {
        loadLevels()
    }

    private fun loadLevels() {
        viewModelScope.launch {
            // --- INICIO DE LA CORRECCIÓN ---

            // 1. Obtenemos TODOS los niveles
            val allLevels = gameDataRepository.getAllLevels()

            // 2. Filtramos para quedarnos solo con los de la categoría actual
            val allLevelsInCategory = allLevels.filter { level ->
                level.levelId.startsWith(categoryId)
            }.sortedBy { it.levelId } // Ordenamos por ID para asegurar la secuencia

            // --- FIN DE LA CORRECCIÓN ---

            val userCompletions = gameDataRepository.getAllLevelCompletionData()
            val category = gameDataRepository.getCategory(categoryId)

            val completionsMap = userCompletions.associateBy { it.levelId }

            // --- CORRECCIÓN DE LA LÓGICA DE DESBLOQUEO ---
            // La lógica anterior dependía de 'levelNumber', que ya no tenemos.
            // La adaptamos para que funcione con la lista ordenada.
            val levelStatuses = allLevelsInCategory.mapIndexed { index, level ->
                val completion = completionsMap[level.levelId]
                val starsEarned = completion?.starsEarned ?: 0

                val isLocked = if (index == 0) {
                    false
                } else {
                    // Miramos las estrellas del nivel anterior en la lista 'allLevelsInCategory'
                    val previousLevelId = allLevelsInCategory[index - 1].levelId
                    val previousLevelStars = completionsMap[previousLevelId]?.starsEarned ?: 0
                    previousLevelStars < 2
                }

                LevelStatus(
                    levelId = level.levelId,
                    levelName = level.levelName["es"] ?: "Nivel",
                    starsEarned = starsEarned,
                    isLocked = isLocked
                )
            }
            // ------------------------------------------

            _uiState.value = LevelSelectionState(
                levels = levelStatuses,
                categoryName = category?.name?.get("es") ?: "Niveles",
                isLoading = false
            )
        }
    }
}