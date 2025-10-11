package com.akrubastudios.playquizgames.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.LocalImageLoader
import com.akrubastudios.playquizgames.core.AppConstants
import com.akrubastudios.playquizgames.ui.components.ScreenBackground
import com.akrubastudios.playquizgames.ui.components.TextWithBorder
import com.akrubastudios.playquizgames.ui.theme.PlayQuizGamesTheme

@Composable
fun ContinentSelectionScreen(
    // Esta es una "función de callback". La pantalla notificará al NavGraph
    // qué continente se ha seleccionado, pasándole el continentId como un String.
    onContinentSelected: (continentId: String) -> Unit
) {
    // Intercepta el botón "Atrás" del sistema y no hace nada.
    BackHandler(enabled = true) { /* No hacer nada */ }

    ScreenBackground(
        backgroundUrl = AppConstants.ONBOARDING_BACKGROUND_URL,
        imageLoader = LocalImageLoader.current,
        imageAlpha = 0.6f,  // 1.0f - 100% opaca, la imagen se verá con toda su fuerza
        scrimAlpha = 0.75f   // 0.7 - 70% opaco en el velo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), // Un padding más generoso para esta pantalla
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título centrado
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextWithBorder(
                    text = stringResource(R.string.continent_selection_title),
                    style = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                    borderColor = Color.White,
                    borderWidth = 4f
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Subtítulo centrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TextWithBorder(
                    text = stringResource(R.string.continent_selection_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    borderColor = Color.White,
                    borderWidth = 3f
                )
            }
            Spacer(modifier = Modifier.height(48.dp))

            // Botón para Sudamérica
            Button(
                onClick = { onContinentSelected("south_america") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(stringResource(R.string.continent_south_america))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Botón para Europa
            Button(
                onClick = { onContinentSelected("europe") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(stringResource(R.string.continent_europe))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Botón para Norteamérica
            Button(
                onClick = { onContinentSelected("north_america") },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(stringResource(R.string.continent_north_america))
            }
        }
    }
}

// Una previsualización para ver cómo se ve la pantalla sin ejecutar la app
@Preview(showBackground = true)
@Composable
fun ContinentSelectionScreenPreview() {
    PlayQuizGamesTheme {
        ContinentSelectionScreen(onContinentSelected = {})
    }
}