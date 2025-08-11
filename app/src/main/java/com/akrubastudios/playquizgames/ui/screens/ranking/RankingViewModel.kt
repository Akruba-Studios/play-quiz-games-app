package com.akrubastudios.playquizgames.ui.screens.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.domain.RankedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RankingState(
    val rankingList: List<RankedUser> = emptyList(),
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
            val ranking = gameDataRepository.getRanking()
            _uiState.value = RankingState(rankingList = ranking, isLoading = false)
        }
    }
}