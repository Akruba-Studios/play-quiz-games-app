package com.akrubastudios.playquizgames.ui.screens.freemode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.domain.UserLevelCompletion
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FreeModeScreen(
    viewModel: FreeModeViewModel = hiltViewModel(),
    // Pasamos una función lambda para manejar la navegación hacia el juego.
    onNavigateToGame: (levelId: String, countryId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Modo Libre",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Repite tus niveles de 3 estrellas para mejorar tu puntaje y ganar más XP.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.masteredLevels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no has conseguido 3 estrellas en ningún nivel.",
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.masteredLevels) { level ->
                    MasteredLevelItem(
                        level = level,
                        onPlayClick = {
                            // Usamos un countryId genérico o vacío, ya que en este modo
                            // no afecta la recompensa de PC.
                            onNavigateToGame(level.levelId, "freemode")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MasteredLevelItem(
    level: UserLevelCompletion,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = level.levelName["es"] ?: "Nivel Desconocido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Lógica para la barra de progreso de puntaje
            val progress = if (level.maxScore > 0) {
                level.highScore.toFloat() / level.maxScore.toFloat()
            } else {
                0f
            }
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "ScoreProgress")

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Récord: ${formatNumber(level.highScore)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Máximo: ${formatNumber(level.maxScore)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPlayClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Jugar")
            }
        }
    }
}

/**
 * Función de utilidad para formatear números largos.
 */
private fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
}