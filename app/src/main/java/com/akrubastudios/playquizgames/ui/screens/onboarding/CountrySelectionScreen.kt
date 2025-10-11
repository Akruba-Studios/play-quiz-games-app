package com.akrubastudios.playquizgames.ui.screens.onboarding

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.AppConstants
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.ui.components.ScreenBackground

@Composable
fun CountrySelectionScreen(
    viewModel: CountrySelectionViewModel = hiltViewModel(),
    onNavigateToMap: () -> Unit
) {
    // Intercepta el botón "Atrás" del sistema y no hace nada.
    BackHandler(enabled = true) { /* No hacer nada */ }

    val uiState by viewModel.uiState.collectAsState()

    Log.d("Onboarding_Debug", "Recomponiendo CountrySelectionScreen. isLoading: ${uiState.isLoading}")

    LaunchedEffect(Unit) {
        Log.d("Onboarding_Debug", "LaunchedEffect iniciado. Escuchando eventos de navegación...")
        viewModel.navigationEvent.collect {
            Log.d("Onboarding_Debug", "✅ Evento de navegación RECIBIDO. Llamando a onNavigateToMap...")
            // Cada vez que recibimos un evento, navegamos.
            onNavigateToMap()
        }
    }

    ScreenBackground(
        backgroundUrl = AppConstants.ONBOARDING_BACKGROUND_URL,
        imageLoader = viewModel.imageLoader,
        imageAlpha = 0.6f,  // 1.0f - 100% opaca, la imagen se verá con toda su fuerza
        scrimAlpha = 0.75f   // 0.7 - 70% opaco en el velo
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.country_selection_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(), // <-- AÑADE ESTA LÍNEA
                textAlign = TextAlign.Center        // <-- AÑADE ESTA LÍNEA
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(uiState.countries) { country ->
                        Button(
                            onClick = {
                                Log.d(
                                    "Onboarding_Debug",
                                    "Botón presionado para: ${country.countryId}"
                                )
                                viewModel.onCountrySelected(country.countryId)
                            },
                            modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 4.dp)
                        ) {
                            Text(text = country.name)
                        }
                    }
                }
            }
        }
    }
}