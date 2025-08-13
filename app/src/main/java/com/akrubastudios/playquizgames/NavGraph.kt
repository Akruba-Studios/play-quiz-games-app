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
import androidx.compose.ui.platform.LocalContext
import com.akrubastudios.playquizgames.core.AdManager
import android.app.Activity
import android.util.Log
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner


object Routes {
    // La ruta a la pantalla de resultados ahora define los parámetros que espera
    const val RESULT_SCREEN = "result/{score}/{totalQuestions}/{correctAnswers}/{starsEarned}/{levelId}"
    const val GAME_SCREEN = "game/{levelId}"
    const val MAP_SCREEN = "map" // Renombramos MENU_SCREEN a MAP_SCREEN
    const val LOGIN_SCREEN = "login"
    const val COUNTRY_SCREEN = "country/{countryId}"
    const val RANKING_SCREEN = "ranking"
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
        ) {
            // Aquí no necesitamos el backStackEntry porque el ViewModel lo maneja

            CountryScreen(
                onPlayClick = { levelId -> // <-- Recibe el levelId dinámico
                    Log.d("NavGraph", "Iniciando nivel: $levelId")
                    navController.navigate(
                        Routes.GAME_SCREEN.replace("{levelId}", levelId) // <-- Usa el levelId dinámico
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }


        composable(Routes.LOGIN_SCREEN) {
            LoginScreen(
                onSignInSuccess = {
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
            route = Routes.GAME_SCREEN, // <-- USA LA NUEVA RUTA
            arguments = listOf(navArgument("levelId") { type = NavType.StringType }) // <-- AÑADE ESTO
        ) {
            GameScreen(navController = navController)
        }

        // Definimos la pantalla de resultados y cómo extraer sus argumentos
        composable(
            route = Routes.RESULT_SCREEN,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("correctAnswers") { type = NavType.IntType },
                navArgument("starsEarned") { type = NavType.IntType },
                navArgument("levelId") { type = NavType.StringType }
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

            // Mostramos la ResultScreen con los datos extraídos
            ResultScreen(
                score = score,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                starsEarned = starsEarned,
                onPlayAgain = {
                    // --- AJUSTA ESTA NAVEGACIÓN ---
                    navController.navigate(Routes.GAME_SCREEN.replace("{levelId}", levelId)) {
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