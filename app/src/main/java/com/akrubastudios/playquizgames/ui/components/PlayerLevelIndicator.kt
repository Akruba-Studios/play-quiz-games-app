package com.akrubastudios.playquizgames.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.akrubastudios.playquizgames.ui.theme.PlayQuizGamesTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.akrubastudios.playquizgames.R
import java.text.NumberFormat
import java.util.Locale

/**
 * Un componente de UI que muestra el nivel del jugador y su progreso de XP.
 *
 * @param levelInfo El objeto que contiene toda la información del nivel del jugador.
 * @param modifier Un Modifier para personalizar el layout del componente.
 */
@Composable
fun PlayerLevelIndicator(
    levelInfo: PlayerLevelManager.LevelInfo,
    boostCount: Int,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val componentPadding = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> PaddingValues(vertical = 6.dp, horizontal = 12.dp)
            screenWidth < 370.dp -> PaddingValues(vertical = 7.dp, horizontal = 14.dp)
            else -> PaddingValues(vertical = 8.dp, horizontal = 16.dp)
        }
    }
    val boostSpacing = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 8.dp
            screenWidth < 370.dp -> 10.dp
            else -> 12.dp
        }
    }
    val progressBarHeight = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 6.dp
            screenWidth < 370.dp -> 7.dp
            else -> 8.dp
        }
    }
    val titleFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 19.sp
            screenWidth < 370.dp -> 21.sp
            else -> 24.sp
        }
    }
    val percentageFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 14.sp
            screenWidth < 370.dp -> 16.sp
            else -> 18.sp
        }
    }
    val iconSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 19.dp
            screenWidth < 370.dp -> 21.dp
            else -> 24.dp
        }
    }
    val boostFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 14.sp
            screenWidth < 370.dp -> 16.sp
            else -> 17.sp
        }
    }
    // --- LÓGICA DE CÁLCULO (sin cambios) ---
    val xpRange = (levelInfo.nextLevelThresholdXp - levelInfo.currentLevelThresholdXp).toFloat()
    val xpProgressInLevel = (levelInfo.playerTotalXp - levelInfo.currentLevelThresholdXp).toFloat()

    val progress = if (xpRange > 0) {
        xpProgressInLevel / xpRange
    } else { 0f }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "PlayerXPProgressAnimation"
    )

    // --- ESTRUCTURA DE LA UI (versión final) ---
    Column(modifier = modifier.padding(componentPadding)) {

        // Fila SUPERIOR: para el Nivel y el Porcentaje.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Agrupamos el Nivel y el Boost en una Row anidada
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.player_level, levelInfo.level),
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold
                )
                // Mostramos el indicador de boost solo si hay al menos uno
                if (boostCount > 0) {
                    Spacer(modifier = Modifier.width(boostSpacing))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Boost de Conquista",
                        tint = MaterialTheme.colorScheme.secondary, // Color dorado para la estrella
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "x $boostCount",
                        fontSize = boostFontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = percentageFontSize,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barra de progreso CENTRAL.
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(progressBarHeight),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Fila INFERIOR: para los valores de XP Totales.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${formatNumber(levelInfo.playerTotalXp)} XP", // XP Total del jugador.
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatNumber(levelInfo.nextLevelThresholdXp)} XP", // Meta del siguiente nivel.
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Función de utilidad para formatear números largos con separadores de miles.
 */
private fun formatNumber(number: Long): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
}

/**
 * Previsualización para el componente en Android Studio.
 * Permite ver el diseño sin tener que ejecutar toda la aplicación.
 */
@Preview(showBackground = true)
@Composable
fun PlayerLevelIndicatorPreview() {
    PlayQuizGamesTheme {
        Surface {
            // Datos para un usuario con 23,200 XP totales.
            val previewLevelInfo = PlayerLevelManager.LevelInfo(
                level = 2,
                playerTotalXp = 23200,
                currentLevelThresholdXp = 10000,
                nextLevelThresholdXp = 25000
            )
            PlayerLevelIndicator(
                levelInfo = previewLevelInfo,
                boostCount = 1 // <-- AÑADIR UN VALOR DE EJEMPLO
            )
        }
    }
}