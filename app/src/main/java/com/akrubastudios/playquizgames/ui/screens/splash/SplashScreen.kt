// en ui/screens/splash/SplashScreen.kt
package com.akrubastudios.playquizgames.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.Routes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrubastudios.playquizgames.ui.theme.DeepNavy
import com.akrubastudios.playquizgames.ui.theme.LightGray
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState.destination) {
        uiState.destination?.let { dest ->
            delay(1500L) // 1.5 segundos
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightGray // <-- FORZAMOS EL COLOR AQUÍ
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_splash),
                    contentDescription = "Logo de Play Quiz Games",
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                // Espacio entre el logo y el texto
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.splash_edition_founders),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy

                )
            }
        }
    }
}