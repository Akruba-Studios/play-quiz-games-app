package com.akrubastudios.playquizgames

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.akrubastudios.playquizgames.ui.screens.country.CountryScreen
import com.akrubastudios.playquizgames.ui.screens.game.GameScreen
import com.akrubastudios.playquizgames.ui.screens.result.ResultScreen
import com.akrubastudios.playquizgames.ui.screens.menu.MenuScreen
import com.akrubastudios.playquizgames.ui.screens.login.LoginScreen
import com.akrubastudios.playquizgames.ui.screens.map.MapScreen
import com.akrubastudios.playquizgames.ui.screens.ranking.RankingScreen
import com.akrubastudios.playquizgames.ui.screens.onboarding.ContinentSelectionScreen
import com.akrubastudios.playquizgames.ui.screens.onboarding.CountrySelectionScreen
import androidx.compose.ui.platform.LocalContext
import com.akrubastudios.playquizgames.core.AdManager
import android.app.Activity
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.akrubastudios.playquizgames.ui.screens.country.CountryViewModel
import com.akrubastudios.playquizgames.ui.screens.game.GameViewModel
import com.akrubastudios.playquizgames.ui.screens.level_selection.LevelSelectionScreen


object Routes {
    // La ruta a la pantalla de resultados ahora define los parámetros que espera
    const val RESULT_SCREEN = "result/{score}/{totalQuestions}/{correctAnswers}/{starsEarned}/{levelId}/{countryId}"
    const val GAME_SCREEN = "game/{countryId}/{levelId}"
    const val MAP_SCREEN = "map" // Renombramos MENU_SCREEN a MAP_SCREEN
    const val LOGIN_SCREEN = "login"
    const val COUNTRY_SCREEN = "country/{countryId}"
    const val RANKING_SCREEN = "ranking"
    const val LEVEL_SELECTION_SCREEN = "level_selection/{countryId}/{categoryId}/{continentId}"
    const val CONTINENT_SELECTION_SCREEN = "continent_selection"
    const val COUNTRY_SELECTION_SCREEN = "country_selection/{continentId}"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN_SCREEN
    ) {

        composable(Routes.RANKING_SCREEN) {
            RankingScreen()
        }

        composable(
            route = Routes.COUNTRY_SCREEN,
            arguments = listOf(navArgument("countryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val countryId = backStackEntry.arguments?.getString("countryId") ?: ""
            val viewModel: CountryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            CountryScreen(
                viewModel = viewModel,
                onPlayClick = { categoryId ->
                    val continentId = uiState.country?.continentId ?: ""
                    val route = Routes.LEVEL_SELECTION_SCREEN
                        .replace("{countryId}", countryId)
                        .replace("{categoryId}", categoryId)
                        .replace("{continentId}", continentId)
                    navController.navigate(route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.LEVEL_SELECTION_SCREEN,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType },
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("continentId") { type = NavType.StringType }
            )
        ) { backStackEntry -> // <-- Usamos backStackEntry
            LevelSelectionScreen(
                onLevelClick = { levelId ->
                    val countryId = backStackEntry.arguments?.getString("countryId") ?: ""
                    val route = Routes.GAME_SCREEN
                        .replace("{countryId}", countryId)
                        .replace("{levelId}", levelId)
                    navController.navigate(route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onSignInComplete = { isNewUser ->
                    if (isNewUser) {
                        // Si es nuevo, va a la pantalla de selección de continente
                        navController.navigate(Routes.CONTINENT_SELECTION_SCREEN) {
                            popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                        }
                    } else {
                        // Si ya existía, va directo al mapa
                        navController.navigate(Routes.MAP_SCREEN) {
                            popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.CONTINENT_SELECTION_SCREEN) {
            ContinentSelectionScreen(
                onContinentSelected = { continentId ->
                    // Navega a la nueva pantalla de selección de país
                    val route = Routes.COUNTRY_SELECTION_SCREEN.replace("{continentId}", continentId)
                    navController.navigate(route)
                }
            )
        }

        // --- AÑADE EL NUEVO COMPOSABLE ---
        composable(
            route = Routes.COUNTRY_SELECTION_SCREEN,
            arguments = listOf(navArgument("continentId") { type = NavType.StringType })
        ) {
            CountrySelectionScreen(
                onNavigateToMap = {
                    navController.navigate(Routes.MAP_SCREEN) {
                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAP_SCREEN) {
            MapScreen(navController = navController) // Por ahora no hace nada más
        }

        composable(
            route = Routes.GAME_SCREEN,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.StringType }
            )
        ) { backStackEntry -> // <-- PASO 1: Nombramos el backStackEntry

            // --- PASO 2: Le pasamos el backStackEntry a hiltViewModel ---
            // Esto le permite a Hilt leer los argumentos de la ruta
            // y pasarlos al SavedStateHandle del ViewModel.
            val viewModel: GameViewModel = hiltViewModel(backStackEntry)

            GameScreen(viewModel = viewModel, navController = navController)
        }

        // Definimos la pantalla de resultados y cómo extraer sus argumentos
        composable(
            route = Routes.RESULT_SCREEN,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("correctAnswers") { type = NavType.IntType },
                navArgument("starsEarned") { type = NavType.IntType },
                navArgument("levelId") { type = NavType.StringType },
                navArgument("countryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extraemos los valores de los argumentos
            val view = LocalView.current
            val activity = view.context as Activity
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0
            val starsEarned = backStackEntry.arguments?.getInt("starsEarned") ?: 0
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "logos_1"
            val countryId = backStackEntry.arguments?.getString("countryId") ?: ""

            // Mostramos la ResultScreen con los datos extraídos
            ResultScreen(
                score = score,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                starsEarned = starsEarned,
                onPlayAgain = {
                    // --- AHORA SÍ TENEMOS TODOS LOS DATOS ---
                    val route = Routes.GAME_SCREEN
                        .replace("{countryId}", countryId)
                        .replace("{levelId}", levelId)
                    navController.navigate(route) {
                        popUpTo(Routes.MAP_SCREEN)
                    }
                },
                onBackToMenu = {
                    // --- AÑADE ESTA LÓGICA ---
                    AdManager.showInterstitialAd(activity)

                    navController.navigate(Routes.MAP_SCREEN) {
                        popUpTo(Routes.MAP_SCREEN) { inclusive = true }
                    }
                    // -------------------------
                }
            )
        }
    }
}