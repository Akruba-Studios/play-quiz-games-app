package com.akrubastudios.playquizgames.ui.screens.result

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.core.AppConstants
import com.akrubastudios.playquizgames.core.SoundEffect
import com.akrubastudios.playquizgames.core.SoundManager
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.DialogButtonText
import com.akrubastudios.playquizgames.ui.components.DialogText
import com.akrubastudios.playquizgames.ui.components.DialogTitle
import com.akrubastudios.playquizgames.ui.components.GemsBalanceIndicator
import com.akrubastudios.playquizgames.ui.components.ScreenBackground
import kotlinx.coroutines.delay
import androidx.compose.ui.text.TextStyle
import com.akrubastudios.playquizgames.ui.components.TextWithBorder

@Composable // Control 4-RS
fun ResultScreen(
    title: String,
    score: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    starsEarned: Int,
    previousBestStars: Int,
    pcGained: Int,
    gemsGained: Int,
    isFromBossFight: Boolean,
    showPlayAgainButton: Boolean,
    playAgainText: String,
    backButtonText: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
    onBackToLevels: () -> Unit,
    onChallengeBossNow: (countryId: String, bossLevelId: String) -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenBackground(
        backgroundUrl = AppConstants.RESULTS_BACKGROUND_URL,
        imageLoader = viewModel.imageLoader,
        imageAlpha = 0.6f,  // 1.0f - 100% opaca, la imagen se verá con toda su fuerza
        scrimAlpha = 0.75f   // 0.7 - 70% opaco en el velo
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextWithBorder( // Esta tecnica deberia centrar siempre siempre los textwithborders
                        text = title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            textAlign = TextAlign.Center
                        ),
                        borderColor = Color.White,
                        borderWidth = 4f
                        // El modifier.fillMaxWidth() se quita de aquí
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextWithBorder(
                        text = stringResource(R.string.result_final_score),
                        style = MaterialTheme.typography.titleMedium,
                        borderColor = Color.White,
                        borderWidth = 3f // Un borde un poco más sutil para un texto más pequeño
                    )
                }
                Text(text = "$score XP", style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(16.dp))
                if (!isFromBossFight && pcGained > 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextWithBorder(
                            text = stringResource(R.string.result_conquest_points),
                            style = MaterialTheme.typography.titleMedium,
                            borderColor = Color.White,
                            borderWidth = 3f
                        )
                    }
                    Text(
                        text = "+$pcGained PC",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary, // Un color destacado
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (gemsGained > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextWithBorder(
                            text = stringResource(R.string.result_gems_won),
                            style = MaterialTheme.typography.titleMedium,
                            borderColor = Color.White,
                            borderWidth = 3f
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Simplemente llamamos a nuestro componente reutilizable
                    GemsBalanceIndicator(
                        gems = gemsGained,
                        prefix = "+", // <-- PASAMOS EL PREFIJO AQUÍ
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f), // 0.8f - 80% de Opacidad
                            shape = RoundedCornerShape(12.dp) // Usa la misma forma que la Card interna
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextWithBorder(
                        text = stringResource(
                            R.string.result_questions_summary,
                            correctAnswers,
                            totalQuestions
                        ),
                        style = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        borderColor = Color.White,
                        borderWidth = 3f
                    )
                }
                // Mostramos las estrellas SOLO si NO es una batalla de jefe.
                if (!isFromBossFight) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedStars(
                        starsEarned = starsEarned,
                        previousBestStars = previousBestStars,
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
    // Este efecto se ejecutará cuando el estado cambie para mostrar el diálogo.
    val pendingCountryId = uiState.pendingBossChallengeCountryId
    val bossLevelId = uiState.bossLevelId

    if (pendingCountryId != null && bossLevelId != null) {
        val countryName = uiState.pendingBossChallengeCountryName ?: "este país"
        AppAlertDialog(
            onDismissRequest = { viewModel.clearPendingBossChallenge() },
            title = { DialogTitle(text = stringResource(R.string.conquest_dialog_title)) },
            text = { DialogText(text = stringResource(R.string.conquest_dialog_text, countryName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Primero, limpiamos la bandera
                        viewModel.clearPendingBossChallenge()
                        // Luego, ejecutamos la navegación
                        onChallengeBossNow(pendingCountryId, bossLevelId)
                    }
                ) {
                    DialogButtonText(text = stringResource(R.string.conquest_dialog_button_challenge))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearPendingBossChallenge() }) {
                    DialogButtonText(text = stringResource(R.string.expedition_dialog_button_later))
                }
            }
        )
    }
}
@Composable
private fun AnimatedStars(
    starsEarned: Int,
    previousBestStars: Int,
    soundManager: SoundManager
) {
    // Un estado para controlar la visibilidad y escala de cada estrella
    val starScales = listOf(
        remember { mutableStateOf(if (previousBestStars >= 1) 1f else 0f) },
        remember { mutableStateOf(if (previousBestStars >= 2) 1f else 0f) },
        remember { mutableStateOf(if (previousBestStars >= 3) 1f else 0f) }
    )

    // Animar la escala de cada estrella individualmente
    val animatedScales = starScales.map {
        animateFloatAsState(
            targetValue = it.value,
            animationSpec = tween(durationMillis = 400),
            label = "StarScaleAnimation"
        ).value
    }

    // Este efecto se ejecuta una sola vez para orquestar la secuencia
    LaunchedEffect(key1 = starsEarned, key2 = previousBestStars) {
        // Solo animamos si el nuevo resultado es MEJOR que el anterior
        if (starsEarned > previousBestStars) {
            // Iteramos desde la primera estrella NUEVA hasta la última ganada
            for (i in previousBestStars until starsEarned) {
                delay(300L)
                soundManager.playSound(SoundEffect.STAR_APPEAR)
                // Activamos la animación de la estrella correspondiente
                starScales[i].value = 1f
            }
        }
    }

    Row {
        (0..2).forEach { index ->
            val starIsEarned = index < starsEarned
            val previousStarWasEarned = index < previousBestStars
            val starColor = if (starIsEarned || previousStarWasEarned) Color(0xFFFFD700) else Color.Gray

            // Usamos un Box para superponer los dos iconos
            Box(
                modifier = Modifier
                    .size(48.dp) // El tamaño se aplica al contenedor
                    .scale(animatedScales[index]),
                contentAlignment = Alignment.Center
            ) {
                // CAPA 1: El borde (un icono más grande, blanco)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null, // El borde no necesita descripción
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White
                )
                // CAPA 2: El relleno (un icono ligeramente más pequeño, con su color)
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(R.string.cd_star_number, index + 1),
                    modifier = Modifier.fillMaxSize(0.85f), // 85% del tamaño crea el borde
                    tint = starColor
                )
            }
        }
    }
}