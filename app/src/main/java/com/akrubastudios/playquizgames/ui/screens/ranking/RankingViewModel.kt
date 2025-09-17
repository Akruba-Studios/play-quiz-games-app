package com.akrubastudios.playquizgames.ui.screens.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.MusicManager
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
data class CurrentUserRankData(
    val rank: Int,
    val totalXp: Long,
    val xpToNext: Long? // null si es el #1
)
data class RankingState(
    val rankingList: List<RankedUserUiItem> = emptyList(),
    val currentUserRank: CurrentUserRankData? = null, // Puede ser null si falla
    val isLoading: Boolean = true
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    val musicManager: MusicManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchRanking()
    }

    private fun fetchRanking() {
        viewModelScope.launch {
            // 3. PEDIMOS AMBAS COSAS A LA VEZ
            val top20Result = gameDataRepository.getRanking() // Pide el Top 20
            val userRankResult = gameDataRepository.getUserRank() // Pide el rango del usuario

            // Procesamos el Top 20
            val rankingForUi = top20Result.map { rankedUser ->
                val levelInfo = PlayerLevelManager.calculateLevelInfo(rankedUser.totalXp)
                RankedUserUiItem(
                    rank = rankedUser.rank,
                    displayName = rankedUser.displayName,
                    totalXp = rankedUser.totalXp,
                    photoUrl = rankedUser.photoUrl,
                    level = levelInfo.level
                )
            }

            // Procesamos el resultado del rango del usuario
            var currentUserData: CurrentUserRankData? = null
            userRankResult.onSuccess { response ->
                currentUserData = CurrentUserRankData(
                    rank = response.rank,
                    totalXp = response.totalXp,
                    xpToNext = response.nextPlayerXp?.let { it - response.totalXp }
                )
            }.onFailure {
                // Si falla, lo dejamos en null, la UI sabrá qué hacer
            }

            // 4. ACTUALIZAMOS EL ESTADO CON TODA LA INFORMACIÓN
            _uiState.value = RankingState(
                rankingList = rankingForUi,
                currentUserRank = currentUserData,
                isLoading = false
            )
        }
    }
}