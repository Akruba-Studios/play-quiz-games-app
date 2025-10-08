package com.akrubastudios.playquizgames.ui.screens.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun OceanSpecularEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "specular")

    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX1"
    )

    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 800f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX2"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(offsetX1, size.height * 0.3f),
                radius = 250f
            ),
            center = Offset(offsetX1, size.height * 0.3f),
            radius = 250f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.8f),
                    Color.White.copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                center = Offset(offsetX2, size.height * 0.6f),
                radius = 180f
            ),
            center = Offset(offsetX2, size.height * 0.6f),
            radius = 180f
        )
    }
}