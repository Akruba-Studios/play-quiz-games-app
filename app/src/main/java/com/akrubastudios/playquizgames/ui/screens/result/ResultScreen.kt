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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ResultScreen(
    title: String,
    score: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    starsEarned: Int,
    pcGained: Int,
    isFromBossFight: Boolean,
    showPlayAgainButton: Boolean,
    playAgainText: String,
    backButtonText: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(R.string.result_final_score), style = MaterialTheme.typography.titleMedium)
        Text(text = "$score XP", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (!isFromBossFight && pcGained > 0) {
            Text(
                text = stringResource(R.string.result_conquest_points),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "+$pcGained PC",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary // Un color destacado
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = stringResource(R.string.result_questions_summary, correctAnswers, totalQuestions)
        )
        // Mostramos las estrellas SOLO si NO es una batalla de jefe.
        if (!isFromBossFight) {
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                // Iteramos de 1 a 3 para crear cada una de las 3 estrellas.
                (1..3).forEach { starIndex ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.cd_star_number, starIndex),
                        modifier = Modifier.size(48.dp),
                        // Lógica de color: si el índice de la estrella (1, 2, o 3)
                        // es menor o igual a las estrellas que ganó el jugador (starsEarned),
                        // la pintamos de dorado. Si no, la pintamos de gris.
                        tint = if (starIndex <= starsEarned) Color(0xFFFFD700) else Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        // El botón ahora se muestra condicionalmente
        if (showPlayAgainButton) {
            Button(onClick = onPlayAgain) {
                Text(text = playAgainText)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(onClick = onBackToMenu) {
            Text(text = backButtonText)
        }
    }
    if (uiState.showXpTutorial) {
        AlertDialog(
            // onDismissRequest se llama si el usuario toca fuera del diálogo
            onDismissRequest = { viewModel.xpTutorialShown() },
            title = { Text(text = stringResource(R.string.xp_tutorial_title)) },
            text = { Text(text = stringResource(R.string.xp_tutorial_message)) },
            confirmButton = {
                // El botón llama a la misma función de limpieza
                TextButton(onClick = { viewModel.xpTutorialShown() }) {
                    Text(stringResource(R.string.dialog_button_ok_levelup))
                }
            }
        )
    }
}