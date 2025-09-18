package com.akrubastudios.playquizgames.ui.screens.result

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.core.SoundEffect
import com.akrubastudios.playquizgames.core.SoundManager
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.GemsBalanceIndicator
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    title: String,
    score: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    starsEarned: Int,
    pcGained: Int,
    gemsGained: Int,
    isFromBossFight: Boolean,
    showPlayAgainButton: Boolean,
    playAgainText: String,
    backButtonText: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    onBackToLevels: () -> Unit,
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
        if (gemsGained > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.result_gems_won),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Simplemente llamamos a nuestro componente reutilizable
            GemsBalanceIndicator(
                gems = gemsGained,
                prefix = "+" // <-- PASAMOS EL PREFIJO AQUÍ
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.result_questions_summary, correctAnswers, totalQuestions)
        )
        // Mostramos las estrellas SOLO si NO es una batalla de jefe.
        if (!isFromBossFight) {
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedStars(
                starsEarned = starsEarned,
                soundManager = viewModel.soundManager
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
        // El botón ahora se muestra condicionalmente
        if (showPlayAgainButton) {
            Button(onClick = onPlayAgain) {
                Text(text = playAgainText)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (!isFromBossFight) {
            Button(onClick = onBackToLevels) {
                Text(text = stringResource(R.string.result_button_back_to_levels))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(onClick = onBackToMenu) {
            Text(text = backButtonText)
        }
    }
    if (uiState.showXpTutorial) {
        AppAlertDialog(
            onDismissRequest = { viewModel.xpTutorialShown() },
            title = stringResource(R.string.xp_tutorial_title),
            text = stringResource(R.string.xp_tutorial_message),
            confirmButtonText = stringResource(R.string.dialog_button_ok_levelup)
        )
    }
}
@Composable
private fun AnimatedStars(
    starsEarned: Int,
    soundManager: SoundManager
) {
    // Un estado para controlar la visibilidad y escala de cada estrella
    val starStates = listOf(
        remember { mutableStateOf(0f) }, // Escala de la estrella 1
        remember { mutableStateOf(0f) }, // Escala de la estrella 2
        remember { mutableStateOf(0f) }  // Escala de la estrella 3
    )

    // Animar la escala de cada estrella individualmente
    val animatedScales = starStates.map {
        animateFloatAsState(
            targetValue = it.value,
            // Animación de resorte (spring) para un efecto de "rebote"
            animationSpec = tween(durationMillis = 400),
            label = "StarScaleAnimation"
        ).value
    }

    // Este efecto se ejecuta una sola vez para orquestar la secuencia
    LaunchedEffect(key1 = starsEarned) {
        for (i in 0 until starsEarned) {
            // Esperamos un poco antes de mostrar la siguiente estrella
            delay(300L)
            // Disparamos el sonido
            soundManager.playSound(SoundEffect.STAR_APPEAR)
            // Activamos la animación de la estrella actual
            starStates[i].value = 1f
        }
    }

    Row {
        (0..2).forEach { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = stringResource(R.string.cd_star_number, index + 1),
                modifier = Modifier
                    .size(48.dp)
                    .scale(animatedScales[index]), // Aplicamos la escala animada
                // El color se basa en si la animación de esta estrella se ha activado
                tint = if (starStates[index].value > 0f) Color(0xFFFFD700) else Color.Gray
            )
        }
    }
}