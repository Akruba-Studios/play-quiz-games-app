package com.akrubastudios.playquizgames.ui.screens.profile.library // <-- PACKAGE CORREGIDO

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.content.res.Configuration
import coil.ImageLoader
import java.util.Locale
import javax.inject.Inject

data class FunFactItem(
    val answer: String, // La respuesta de la pregunta para dar contexto
    val text: String    // El texto del fun fact
)

data class LibraryLevelItem(
    val levelName: String,
    val funFacts: List<FunFactItem>
)

data class LibraryCategoryItem(
    val categoryName: String,
    val levels: List<LibraryLevelItem>
)

// Nueva data class para el nivel superior de la jerarquía
data class LibraryContinentItem(
    val continentName: String,
    val categories: List<LibraryCategoryItem>
)

data class FunFactLibraryState(
    val isLoading: Boolean = true,
    // La lista principal ahora es de continentes
    val continents: List<LibraryContinentItem> = emptyList()
)

@HiltViewModel
class FunFactLibraryViewModel @Inject constructor(
    private val gameDataRepository: GameDataRepository,
    private val quizRepository: QuizRepository,
    private val languageManager: LanguageManager, // <-- IMPORTADO Y USADO
    private val application: Application,
    val imageLoader: ImageLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(FunFactLibraryState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMasteredFunFacts()
    }

    private fun getLocalizedResources(): android.content.res.Resources {
        val appLanguage = languageManager.languageStateFlow.value
        val locale = Locale(appLanguage)
        val config = Configuration(application.resources.configuration)
        config.setLocale(locale)
        return application.createConfigurationContext(config).resources
    }

    private fun loadMasteredFunFacts() {
        viewModelScope.launch {
            combine(
                gameDataRepository.userStateFlow,
                languageManager.languageStateFlow
            ) { user, langCode ->
                Pair(user, langCode)
            }.collect { (user, langCode) ->
                _uiState.value = FunFactLibraryState(isLoading = true)

                if (user == null || user.masteredLevelIds.isEmpty()) {
                    _uiState.value = FunFactLibraryState(isLoading = false, continents = emptyList())
                    return@collect
                }

                // Cargamos todos los datos estáticos necesarios
                val allCategories = gameDataRepository.getCategoryList()
                val allContinents = mapOf(
                    "south_america" to R.string.continent_south_america,
                    "north_america" to R.string.continent_north_america,
                    "europe" to R.string.continent_europe,
                    "asia" to R.string.continent_asia,
                    "africa" to R.string.continent_africa,
                    "oceania" to R.string.continent_oceania
                )

                // --- INICIO DE LA CORRECCIÓN ---

                // 1. Cargamos TODOS los metadatos de los niveles una sola vez.
                val allLevelsMetadata = gameDataRepository.getAllLevels()

                // 2. Creamos un mapa de consulta para un acceso rápido: LevelID -> TierID
                val levelIdToTierIdMap = allLevelsMetadata.associateBy({ it.levelId }, { it.tierId })

                // 3. Cargamos el contenido de los niveles que el usuario ha dominado.
                val masteredLevelsContent = user.masteredLevelIds.mapNotNull { levelId ->
                    quizRepository.getLevel(levelId)
                }

                // 4. Agrupamos los QuizLevelPackage usando nuestro mapa de consulta.
                val groupedByContinent = masteredLevelsContent.groupBy {
                    // Para cada nivel, buscamos su tierId en nuestro mapa.
                    levelIdToTierIdMap[it.levelId] ?: "unknown"
                }

                // --- FIN DE LA CORRECCIÓN ---

                // El resto de la lógica de construcción de la UI es la misma y ahora funcionará.
                val libraryContinents = groupedByContinent.map { (continentId, levelsInContinent) ->
                    val continentNameRes = allContinents[continentId] ?: R.string.unknown_continent
                    val continentName = getLocalizedResources().getString(continentNameRes)

                    val groupedByCategory = levelsInContinent.groupBy {
                        it.levelId.split('_').getOrNull(1) ?: "unknown"
                    }

                    val libraryCategories = groupedByCategory.mapNotNull { (categoryId, levelsInCategory) ->
                        val categoryInfo = allCategories.find { it.categoryId == categoryId }
                        val categoryName = categoryInfo?.name?.get(langCode) ?: categoryId

                        val libraryLevels = levelsInCategory.map { levelPackage ->
                            LibraryLevelItem(
                                levelName = levelPackage.levelName[langCode] ?: levelPackage.levelId,
                                funFacts = levelPackage.questions.map { question ->
                                    val answerText = if (langCode == "es") question.correctAnswer_es else question.correctAnswer_en
                                    val funFactText = if (langCode == "es") question.fun_fact_es else question.fun_fact_en
                                    FunFactItem(
                                        answer = answerText.uppercase(),
                                        text = funFactText
                                    )
                                }
                            )
                        }.sortedBy { it.levelName }

                        LibraryCategoryItem(
                            categoryName = categoryName,
                            levels = libraryLevels
                        )
                    }.sortedBy { it.categoryName }

                    LibraryContinentItem(
                        continentName = continentName,
                        categories = libraryCategories
                    )
                }.sortedBy { it.continentName }

                _uiState.value = FunFactLibraryState(isLoading = false, continents = libraryContinents)
            }
        }
    }
}