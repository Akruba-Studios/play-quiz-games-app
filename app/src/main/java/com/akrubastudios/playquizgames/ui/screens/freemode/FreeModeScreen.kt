package com.akrubastudios.playquizgames.ui.screens.freemode

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.domain.UserLevelCompletion
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicTrack

@Composable
fun FreeModeScreen(
    viewModel: FreeModeViewModel = hiltViewModel(),
    // Pasamos una función lambda para manejar la navegación hacia el juego.
    onNavigateToGame: (levelId: String, countryId: String, difficulty: String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.free_mode_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            stringResource(R.string.free_mode_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Usamos una lista de IDs para la lógica.
        val difficultyIds = listOf("principiante", "dificil")
        // 2. El índice se calcula correctamente.
        val selectedIndex = difficultyIds.indexOf(selectedDifficulty)

        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3. Iteramos sobre los IDs.
            difficultyIds.forEachIndexed { index, id ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { viewModel.onDifficultyChange(id) },
                    // 4. Obtenemos el texto traducido DENTRO del Tab.
                    text = {
                        val textRes = if (id == "principiante") R.string.difficulty_beginner else R.string.difficulty_hard
                        Text(text = stringResource(textRes))
                    }
                )
            }
        }
        Text(
            text = if (selectedDifficulty == "dificil") stringResource(R.string.difficulty_hard_bonus) else stringResource(R.string.difficulty_beginner_tip),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
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
                    stringResource(R.string.free_mode_no_levels),
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
                        selectedDifficulty = selectedDifficulty,
                        onPlayClick = {
                            // Usamos un countryId genérico o vacío, ya que en este modo
                            // no afecta la recompensa de PC.
                            onNavigateToGame(level.levelId, "freemode", selectedDifficulty)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MasteredLevelItem(
    level: MasteredLevelUiState,
    selectedDifficulty: String,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = level.levelName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Definimos nuestros máximos teóricos.
            val maxScorePrincipiante = level.maxScore
            val maxScoreDificil = (level.maxScore * 1.5).toInt()

            // 2. La barra de progreso SIEMPRE es relativa al máximo de Difícil.
            val progress = if (maxScoreDificil > 0) {
                (level.highScore.toFloat() / maxScoreDificil.toFloat()).coerceAtMost(1f)
            } else {
                0f
            }
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "ScoreProgress")

            // 3. Definimos la condición para mostrar el aviso.
            val threshold = maxScorePrincipiante * 0.8
            val shouldShowHint = level.highScore >= threshold

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
                    text = stringResource(R.string.free_mode_record, formatNumber(level.highScore)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    // Siempre mostramos el máximo real (de Difícil).
                    text = stringResource(R.string.free_mode_max, formatNumber(maxScoreDificil)),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 4. Mostramos el texto de ayuda solo si AMBAS condiciones se cumplen.
            if (shouldShowHint && selectedDifficulty == "principiante") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.free_mode_beginner_max_hint, formatNumber(maxScorePrincipiante)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPlayClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.free_mode_button_play))
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