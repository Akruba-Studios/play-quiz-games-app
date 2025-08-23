// en ui/screens/splash/SplashScreen.kt
package com.akrubastudios.playquizgames.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.Routes

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState.destination) {
        uiState.destination?.let { dest ->
            val route = when (dest) {
                Destination.LOGIN -> Routes.LOGIN_SCREEN
                Destination.ONBOARDING -> Routes.CONTINENT_SELECTION_SCREEN
                Destination.MAP -> Routes.MAP_SCREEN
            }
            // Navegamos al destino final y limpiamos la pila para que
            // el usuario no pueda volver a la Splash Screen.
            navController.navigate(route) {
                popUpTo(Routes.SPLASH_SCREEN) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}