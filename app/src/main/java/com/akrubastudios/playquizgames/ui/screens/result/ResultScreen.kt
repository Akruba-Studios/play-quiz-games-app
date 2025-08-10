package com.akrubastudios.playquizgames.ui.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "¡Juego Terminado!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Puntaje Final", style = MaterialTheme.typography.titleMedium)
        Text(text = "$score", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Acertaste $correctAnswers de $totalQuestions preguntas.")
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onPlayAgain) {
            Text(text = "Jugar de Nuevo")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackToMenu) {
            Text(text = "Volver al Menú")
        }
    }
}