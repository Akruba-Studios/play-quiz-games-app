package com.akrubastudios.playquizgames.ui.screens.profile.library // <-- PACKAGE CORREGIDO

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estas data classes ahora viven en el paquete correcto
data class FunFactItem(
    val text: String
)
data class LibraryLevelItem(
    val levelName: String,
    val funFacts: List<FunFactItem>
)
data class LibraryCategoryItem(
    val categoryName: String,
    val levels: List<LibraryLevelItem>
)
data class FunFactLibraryState(
    val isLoading: Boolean = true,
    val categories: List<LibraryCategoryItem> = emptyList()
)

@HiltViewModel
class FunFactLibraryViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val quizRepository: QuizRepository,
    private val languageManager: LanguageManager // <-- IMPORTADO Y USADO
) : ViewModel() {

    private val _uiState = MutableStateFlow(FunFactLibraryState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMasteredFunFacts()
    }

    private fun loadMasteredFunFacts() {
        viewModelScope.launch {
            // Combinamos los datos del usuario Y el idioma.
            combine(
                gameDataRepository.userStateFlow,
                languageManager.languageStateFlow
            ) { user, langCode ->
                Pair(user, langCode)
            }.collect { (user, langCode) ->
                _uiState.value = FunFactLibraryState(isLoading = true)

                if (user == null || user.masteredLevelIds.isEmpty()) {
                    _uiState.value = FunFactLibraryState(isLoading = false, categories = emptyList())
                    return@collect
                }

                val allCategories = gameDataRepository.getCategoryList()

                val masteredLevelsContent = user.masteredLevelIds.mapNotNull { levelId ->
                    quizRepository.getLevel(levelId)
                }

                val groupedByCategory = masteredLevelsContent.groupBy { level ->
                    level.levelId.split('_').getOrNull(1) ?: "unknown"
                }

                val libraryCategories = groupedByCategory.mapNotNull { (categoryId, levels) ->
                    val categoryInfo = allCategories.find { it.categoryId == categoryId }
                    // Usa langCode para el nombre de la categoría
                    val categoryName = categoryInfo?.name?.get(langCode) ?: categoryId

                    val libraryLevels = levels.map { levelPackage ->
                        LibraryLevelItem(
                            // Usa langCode para el nombre del nivel
                            levelName = levelPackage.levelName[langCode] ?: levelPackage.levelId,
                            funFacts = levelPackage.questions.map { question ->
                                // Usa langCode para el texto del fun fact
                                val funFactText = if (langCode == "es") question.fun_fact_es else question.fun_fact_en
                                FunFactItem(text = funFactText)
                            }
                        )
                    }.sortedBy { it.levelName }

                    LibraryCategoryItem(
                        categoryName = categoryName,
                        levels = libraryLevels
                    )
                }.sortedBy { it.categoryName }

                _uiState.value = FunFactLibraryState(isLoading = false, categories = libraryCategories)
            }
        }
    }
}