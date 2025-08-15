package com.akrubastudios.playquizgames.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrubastudios.playquizgames.ui.theme.PlayQuizGamesTheme

@Composable
fun ContinentSelectionScreen(
    // Esta es una "función de callback". La pantalla notificará al NavGraph
    // qué continente se ha seleccionado, pasándole el continentId como un String.
    onContinentSelected: (continentId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp), // Un padding más generoso para esta pantalla
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Elige tu Continente de Origen",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tu primera expedición comenzará aquí.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))

        // Botón para Sudamérica
        Button(
            onClick = { onContinentSelected("south_america") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Sudamérica")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Europa
        Button(
            onClick = { onContinentSelected("europe") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Europa")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para Norteamérica
        Button(
            onClick = { onContinentSelected("north_america") },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Norteamérica")
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