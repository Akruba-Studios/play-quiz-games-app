package com.akrubastudios.playquizgames.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GemsIndicator(
    gems: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    forceTheme: androidx.compose.material3.ColorScheme? = null,
    maxScaleFactor: Float = 1.15f
) {
    val hasGems = gems > 0
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val infiniteTransition = rememberInfiniteTransition(label = "gemsAnimation")

    // Tamaños adaptativos basados en screenWidth
    val gemsFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 10.sp    // Zona crítica
            screenWidth < 370.dp -> 12.sp    // Zona transición
            else -> 16.sp                    // Zona normal
        }
    }

    val gemsPadding = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> PaddingValues(horizontal = 6.dp, vertical = 2.dp)    // Zona crítica
            screenWidth < 370.dp -> PaddingValues(horizontal = 8.dp, vertical = 3.dp)    // Zona transición
            else -> PaddingValues(horizontal = 10.dp, vertical = 4.dp)                   // Zona normal
        }
    }

    val iconSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 18.dp    // Zona crítica
            screenWidth < 370.dp -> 20.dp    // Zona transición
            else -> 22.dp                    // Zona normal
        }
    }

    // Animación de pulso (escala)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasGems) maxScaleFactor else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Animación de brillo (alpha)
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = if (hasGems) 0.6f else 0.5f,
        targetValue = if (hasGems) 1f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    // Animación de cambio de color
    val colorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorPhase"
    )

    val isRedPhase = colorPhase > 0.75f

    val cardContent = @Composable {
        Card(
            modifier = modifier.clickable(
                enabled = hasGems,
                onClick = onClick
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(gemsPadding)
                    .scale(pulseScale)
                    .alpha(shimmerAlpha),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    imageVector = if (isRedPhase) GemIconDarkGold else GemIcon,
                    contentDescription = "Gems",
                    modifier = Modifier.size(iconSize)
                )
                Text(
                    text = formatGems(gems),
                    fontSize = gemsFontSize,
                    lineHeight = (gemsFontSize.value * 0.9f).sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
    if (forceTheme != null) {
        MaterialTheme(colorScheme = forceTheme) {
            cardContent()
        }
    } else {
        cardContent()
    }
}

private fun formatGems(gems: Int): String {
    return when {
        gems < 1000 -> gems.toString()
        gems < 10000 -> {
            val decimal = (gems % 1000) / 100
            if (decimal == 0) {
                "${gems / 1000}.0K"
            } else {
                "${gems / 1000}.${decimal}K"
            }
        }
        gems < 100000 -> "${gems / 1000}K"
        gems < 1000000 -> "${gems / 1000}K"
        else -> {
            val millions = gems / 1000000
            val decimal = (gems % 1000000) / 100000
            if (millions < 10 && decimal > 0) {
                "$millions.${decimal}M"
            } else {
                "${millions}M"
            }
        }
    }
}