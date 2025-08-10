package com.akrubastudios.playquizgames

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.akrubastudios.playquizgames.ui.screens.game.GameScreen
import com.akrubastudios.playquizgames.ui.screens.result.ResultScreen
import com.akrubastudios.playquizgames.ui.screens.menu.MenuScreen

object Routes {
    // La ruta a la pantalla de resultados ahora define los parámetros que espera
    const val RESULT_SCREEN = "result/{score}/{totalQuestions}/{correctAnswers}"
    const val GAME_SCREEN = "game"
    const val MENU_SCREEN = "menu" // Añadimos la ruta del menú para el futuro
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.MENU_SCREEN // <-- CAMBIO 1: Empezamos en el menú
    ) {
        composable(Routes.MENU_SCREEN) {
            MenuScreen(
                onPlayClick = { navController.navigate(Routes.GAME_SCREEN) }
            )
        }

        composable(Routes.GAME_SCREEN) {
            // Le pasamos el navController a la GameScreen
            GameScreen(navController = navController)
        }

        // Definimos la pantalla de resultados y cómo extraer sus argumentos
        composable(
            route = Routes.RESULT_SCREEN,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("correctAnswers") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            // Extraemos los valores de los argumentos
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 0
            val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0

            // Mostramos la ResultScreen con los datos extraídos
            ResultScreen(
                score = score,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                onPlayAgain = {
                    // Navega al juego y limpia la pila para que no se pueda volver al resultado anterior
                    navController.navigate(Routes.GAME_SCREEN) {
                        popUpTo(Routes.MENU_SCREEN) // <-- CAMBIO 2: Limpia hasta el menú
                    }
                },
                onBackToMenu = {
                    // --- AÑADE ESTA LÓGICA ---
                    navController.navigate(Routes.MENU_SCREEN) {
                        popUpTo(Routes.MENU_SCREEN) { inclusive = true }
                    }
                    // -------------------------
                }
            )
        }
    }
}