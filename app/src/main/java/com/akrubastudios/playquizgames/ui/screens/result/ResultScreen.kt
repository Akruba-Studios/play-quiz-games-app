package com.akrubastudios.playquizgames.ui.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    starsEarned: Int,
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

        // --- AÑADE ESTE COMPONENTE PARA MOSTRAR LAS ESTRELLAS ---
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            (1..3).forEach { starIndex ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrella $starIndex",
                    modifier = Modifier.size(48.dp),
                    tint = if (starIndex <= starsEarned) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
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