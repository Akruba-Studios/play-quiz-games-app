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
import com.akrubastudios.playquizgames.ui.screens.login.LoginScreen
import com.akrubastudios.playquizgames.ui.screens.map.MapScreen
import com.akrubastudios.playquizgames.ui.screens.ranking.RankingScreen
import com.akrubastudios.playquizgames.ui.screens.onboarding.ContinentSelectionScreen
import com.akrubastudios.playquizgames.ui.screens.onboarding.CountrySelectionScreen
import com.akrubastudios.playquizgames.core.AdManager
import android.app.Activity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.ui.screens.boss.BossScreen
import com.akrubastudios.playquizgames.ui.screens.country.CountryViewModel
import com.akrubastudios.playquizgames.ui.screens.freemode.FreeModeScreen
import com.akrubastudios.playquizgames.ui.screens.game.GameViewModel
import com.akrubastudios.playquizgames.ui.screens.level_selection.LevelSelectionScreen
import com.akrubastudios.playquizgames.ui.screens.profile.ProfileScreen
import com.akrubastudios.playquizgames.ui.screens.profile.ProfileViewModel
import com.akrubastudios.playquizgames.ui.screens.profile.library.FunFactLibraryScreen
import com.akrubastudios.playquizgames.ui.screens.settings.SettingsScreen
import com.akrubastudios.playquizgames.ui.screens.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth


object Routes {
    // La ruta a la pantalla de resultados ahora define los parámetros que espera
    const val SPLASH_SCREEN = "splash"
    const val RESULT_SCREEN = "result/{score}/{totalQuestions}/{correctAnswers}/{starsEarned}/{levelId}/{countryId}/{difficulty}/{isFromBossFight}/{victory}/{pcGained}"
    const val GAME_SCREEN = "game/{countryId}/{levelId}/{difficulty}"
    const val MAP_SCREEN = "map" // Renombramos MENU_SCREEN a MAP_SCREEN
    const val LOGIN_SCREEN = "login"
    const val COUNTRY_SCREEN = "country/{countryId}"
    const val RANKING_SCREEN = "ranking"
    const val LEVEL_SELECTION_SCREEN = "level_selection/{countryId}/{categoryId}/{continentId}"
    const val CONTINENT_SELECTION_SCREEN = "continent_selection"
    const val COUNTRY_SELECTION_SCREEN = "country_selection/{continentId}"
    const val FREE_MODE_SCREEN = "free_mode"
    const val BOSS_SCREEN = "boss/{countryId}/{levelId}"
    const val PROFILE_SCREEN = "profile"
    const val SETTINGS_SCREEN = "settings"
    const val FUN_FACT_LIBRARY_SCREEN = "fun_fact_library"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_SCREEN
    ) {
        composable(Routes.SPLASH_SCREEN) {
            SplashScreen(navController = navController)
        }

        composable(Routes.RANKING_SCREEN) {
            RankingScreen()
        }

        composable(Routes.FREE_MODE_SCREEN) {
            FreeModeScreen(
                onNavigateToGame = { levelId, countryId, difficulty ->
                    // Navega a la pantalla del juego con los datos del nivel seleccionado.
                    val route = Routes.GAME_SCREEN
                        .replace("{countryId}", countryId) // Usará "freemode"
                        .replace("{levelId}", levelId)
                        .replace("{difficulty}", difficulty)
                    navController.navigate(route)
                }
            )
        }
        composable(Routes.PROFILE_SCREEN) {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onSignOut = {
                    // Cuando el ViewModel nos notifica que la sesión se cerró,
                    // navegamos a la pantalla de Login.
                    navController.navigate(Routes.LOGIN_SCREEN) {
                        // Limpiamos TODA la pila de navegación hasta la raíz.
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS_SCREEN)
                },
                navController = navController
            )
        }
        composable(Routes.SETTINGS_SCREEN) {
            SettingsScreen(
                onBackClick = {
                    // Simplemente vuelve a la pantalla anterior en la pila (ProfileScreen).
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.FUN_FACT_LIBRARY_SCREEN) {
            // Necesitamos importar FunFactLibraryScreen
            FunFactLibraryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Routes.COUNTRY_SCREEN,
            arguments = listOf(navArgument("countryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: CountryViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            CountryScreen(
                onPlayCategoryClick = { categoryId ->
                    val countryId = backStackEntry.arguments?.getString("countryId") ?: ""
                    val continentId = uiState.country?.continentId ?: ""
                    val route = Routes.LEVEL_SELECTION_SCREEN
                        .replace("{countryId}", countryId)
                        .replace("{categoryId}", categoryId)
                        .replace("{continentId}", continentId)
                    navController.navigate(route)
                },
                onChallengeBossClick = { bossLevelId ->
                    val countryId = backStackEntry.arguments?.getString("countryId") ?: ""
                    // Construimos la ruta a la BossScreen.
                    val route = Routes.BOSS_SCREEN
                        .replace("{countryId}", countryId)
                        .replace("{levelId}", bossLevelId)
                    navController.navigate(route)
                },
                onApplyBoostClick = { viewModel.applyPcBoost() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.BOSS_SCREEN,
            arguments = listOf(
                navArgument("countryId") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.StringType }
            )
        ) {
            // El BossViewModel se inyecta automáticamente gracias a Hilt.
            BossScreen(navController = navController)
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
                // La lambda ahora provee levelId y difficulty
                onLevelClick = { levelId, difficulty ->
                    val countryId = backStackEntry.arguments?.getString("countryId") ?: ""
                    // Construimos la nueva ruta completa
                    val route = Routes.GAME_SCREEN
                        .replace("{countryId}", countryId)
                        .replace("{levelId}", levelId)
                        .replace("{difficulty}", difficulty) // <-- NUEVA LÍNEA
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
                navArgument("levelId") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
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
                navArgument("countryId") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("isFromBossFight") { type = NavType.BoolType },
                navArgument("victory") { type = NavType.BoolType },
                navArgument("pcGained") { type = NavType.IntType }
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
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "principiante"
            val isFromBossFight = backStackEntry.arguments?.getBoolean("isFromBossFight") ?: false
            val victory = backStackEntry.arguments?.getBoolean("victory") ?: false
            val pcGained = backStackEntry.arguments?.getInt("pcGained") ?: 0

            val title = if (isFromBossFight) {
                if (victory) stringResource(R.string.result_title_boss_victory) else stringResource(R.string.result_title_boss_defeat)
            } else {
                stringResource(R.string.result_title_game_over)
            }
            val showPlayAgainButton: Boolean
            val playAgainText: String
            val backButtonText: String

            if (isFromBossFight) {
                if (victory) {
                    showPlayAgainButton = false // No se puede rejugar una victoria
                    playAgainText = ""
                    backButtonText = stringResource(R.string.result_button_celebrate)
                } else {
                    showPlayAgainButton = true
                    playAgainText = stringResource(R.string.result_button_retry_challenge)
                    backButtonText = stringResource(R.string.result_button_retreat)
                }
            } else {
                showPlayAgainButton = true
                playAgainText = stringResource(R.string.result_button_play_again)
                backButtonText = stringResource(R.string.result_button_back_to_menu)
            }

            // Mostramos la ResultScreen con los datos extraídos
            ResultScreen(
                title = title,
                score = score,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                starsEarned = starsEarned,
                pcGained = pcGained,
                isFromBossFight = isFromBossFight,
                showPlayAgainButton = showPlayAgainButton,
                playAgainText = playAgainText,
                backButtonText = backButtonText,
                onPlayAgain = {
                    // --- LÓGICA DE NAVEGACIÓN CONDICIONAL ---
                    val destinationRoute = if (isFromBossFight) {
                        Routes.BOSS_SCREEN
                    } else {
                        Routes.GAME_SCREEN
                    }
                    val route = destinationRoute
                        .replace("{countryId}", countryId)
                        .replace("{levelId}", levelId)
                        .replace("{difficulty}", difficulty)
                    navController.navigate(route) {
                        popUpTo(Routes.MAP_SCREEN)
                    }
                },
                onBackToMenu = {
                    // --- AÑADE ESTA LÓGICA DE TEST DE ANUNCIO---
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