package com.akrubastudios.playquizgames.ui.screens.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable // Brillo Especular Animado : Efecto de circulos brillantes moviendose en el oceano
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
@Composable // Gradient Overlay Animado: Un gradiente azul oscuro que pulsa desde las esquinas.
fun OceanGradientOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Gradiente desde esquinas hacia el centro
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF1B4F72).copy(alpha = alpha)
                ),
                center = size.center,
                radius = size.maxDimension * 0.8f
            )
        )
    }
}
@Composable // Vignette Dinámico: Bordes oscuros que pulsan, haciendo el centro más prominente.
fun OceanVignette(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "vignette")

    val intensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = intensity)
                ),
                center = size.center,
                radius = size.minDimension * 1.2f
            )
        )
    }
}
@Composable // Partículas flotantes que simulan burbujas/espuma
fun OceanBubblesEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

    // Generamos 15 burbujas con diferentes offsets y velocidades
    val bubbles = remember {
        List(15) { index ->
            Triple(
                (index * 73) % 100 / 100f, // posX (0-1)
                (index * 47) % 100 / 100f, // velocidad relativa
                (index * 31) % 50 + 20f    // tamaño (20-70)
            )
        }
    }

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        bubbles.forEach { (posX, speedFactor, bubbleSize) ->
            // Movimiento vertical cíclico
            val yProgress = (time + speedFactor) % 1f
            val yPos = size.height * (1f - yProgress) // De abajo hacia arriba

            // Oscilación horizontal suave
            val xOffset = kotlin.math.sin(yProgress * 6.28f * 2) * 30f
            val xPos = size.width * posX + xOffset

            // Alpha que desaparece al llegar arriba
            val alpha = if (yProgress > 0.85f) {
                (1f - yProgress) / 0.15f
            } else {
                0.3f
            }

            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.4f),
                radius = bubbleSize / 2f,
                center = Offset(xPos, yPos)
            )
        }
    }
}
@Composable // Rayos de luz que atraviesan el agua (God Rays)
fun OceanGodRaysEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "godRays")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val rayColor = Color.White.copy(alpha = alpha)

        // Rayo 1 - Diagonal principal
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    rayColor,
                    rayColor,
                    Color.Transparent
                ),
                startX = offsetX - 200f,
                endX = offsetX + 200f
            ),
            topLeft = Offset(offsetX - 200f, -100f),
            size = androidx.compose.ui.geometry.Size(400f, size.height + 200f)
        )

        // Rayo 2 - Más delgado y rápido
        val offsetX2 = offsetX * 0.7f + 300f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    rayColor.copy(alpha = alpha * 0.6f),
                    Color.Transparent
                ),
                startX = offsetX2 - 100f,
                endX = offsetX2 + 100f
            ),
            topLeft = Offset(offsetX2 - 100f, -100f),
            size = androidx.compose.ui.geometry.Size(200f, size.height + 200f)
        )

        // Rayo 3 - Muy sutil, lento
        val offsetX3 = offsetX * 1.3f - 500f
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    rayColor.copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                startX = offsetX3 - 150f,
                endX = offsetX3 + 150f
            ),
            topLeft = Offset(offsetX3 - 150f, -100f),
            size = androidx.compose.ui.geometry.Size(300f, size.height + 200f)
        )
    }
}
@Composable // Capas de niebla/neblina que flotan
fun OceanMistEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mist")

    // Capa de niebla 1 - Movimiento lento
    val offsetX1 by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX1"
    )

    // Capa de niebla 2 - Movimiento más rápido, dirección opuesta
    val offsetX2 by infiniteTransition.animateFloat(
        initialValue = 1000f,
        targetValue = -400f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offsetX2"
    )

    // Pulsación de opacidad - MÁS VISIBLE
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Capa 1 - Niebla superior (más pequeña y visible)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(offsetX1, size.height * 0.3f),
                radius = 350f
            ),
            topLeft = Offset(offsetX1 - 400f, size.height * 0.2f),
            size = androidx.compose.ui.geometry.Size(800f, 300f)
        )

        // Capa 2 - Niebla inferior (forma alargada horizontal)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.8f),
                    Color.White.copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                center = Offset(offsetX2, size.height * 0.65f),
                radius = 400f
            ),
            topLeft = Offset(offsetX2 - 500f, size.height * 0.55f),
            size = androidx.compose.ui.geometry.Size(1000f, 250f)
        )

        // Capa 3 - Niebla pequeña central
        val offsetX3 = offsetX1 * 0.6f + size.width * 0.3f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.6f),
                    Color.Transparent
                ),
                center = Offset(offsetX3, size.height * 0.45f),
                radius = 250f
            ),
            topLeft = Offset(offsetX3 - 300f, size.height * 0.35f),
            size = androidx.compose.ui.geometry.Size(600f, 200f)
        )
    }
}