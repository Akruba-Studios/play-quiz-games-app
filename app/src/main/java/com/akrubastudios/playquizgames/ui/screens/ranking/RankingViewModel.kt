package com.akrubastudios.playquizgames.ui.screens.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.akrubastudios.playquizgames.domain.RankedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RankedUserUiItem(
    val rank: Int,
    val displayName: String,
    val totalXp: Long,
    val photoUrl: String?,
    val level: Int // <-- El nuevo dato
)
data class RankingState(
    val rankingList: List<RankedUserUiItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchRanking()
    }

    private fun fetchRanking() {
        viewModelScope.launch {
            // 3. Obtenemos los datos del repositorio como antes
            val rankingData = gameDataRepository.getRanking()

            // 4. TRANSFORMAMOS los datos del dominio a datos para la UI
            val rankingForUi = rankingData.map { rankedUser ->
                // Calculamos el nivel para cada usuario
                val levelInfo = PlayerLevelManager.calculateLevelInfo(rankedUser.totalXp)
                RankedUserUiItem(
                    rank = rankedUser.rank,
                    displayName = rankedUser.displayName,
                    totalXp = rankedUser.totalXp,
                    photoUrl = rankedUser.photoUrl,
                    level = levelInfo.level
                )
            }

            // 5. Actualizamos el estado con la nueva lista transformada
            _uiState.value = RankingState(rankingList = rankingForUi, isLoading = false)
        }
    }
}