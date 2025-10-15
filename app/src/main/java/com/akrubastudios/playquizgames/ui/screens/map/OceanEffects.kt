package com.akrubastudios.playquizgames.ui.screens.map

import android.R.attr.scaleX
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phishing
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Tsunami
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

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
@Composable
fun StormEffect(
    modifier: Modifier = Modifier,
    onThunderSound: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "storm")

    // Ciclo de relámpagos cada 10 segundos
    val lightningCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightningCycle"
    )

    // Determinar si hay relámpago en este ciclo (30% probabilidad)
    val shouldFlash = remember(lightningCycle) {
        if (lightningCycle < 0.05f) Random.nextFloat() < 0.3f else false
    }

    val flashProgress = if (shouldFlash && lightningCycle < 0.05f) {
        lightningCycle / 0.05f // 0 a 1 en los primeros 500ms
    } else 0f

    val flashIntensity = when {
        flashProgress < 0.3f -> 0f // Esperando
        flashProgress < 0.5f -> (flashProgress - 0.3f) / 0.2f * -0.25f // Oscurece
        flashProgress < 0.6f -> 0.95f // FLASH máximo
        flashProgress < 1f -> 0.95f - ((flashProgress - 0.6f) / 0.4f * 0.95f) // Desvanece
        else -> 0f
    }

    // Disparar sonido cuando el flash está en máximo
    LaunchedEffect(flashProgress) {
        if (flashProgress in 0.5f..0.61f && shouldFlash) {
            onThunderSound()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Overlay oscuro constante (ambiente de tormenta)
        drawRect(color = Color.Black.copy(alpha = 0.35f))

        // Flash del relámpago
        if (flashIntensity > 0.5f) {
            drawRect(color = Color.White.copy(alpha = flashIntensity))
        } else if (flashIntensity < 0f) {
            drawRect(color = Color.Black.copy(alpha = -flashIntensity))
        }
    }
}

@Composable
fun RainEffect(modifier: Modifier = Modifier) {
    // Sistema de impactos múltiples con diferentes características
    val impacts = remember { mutableStateListOf<RainImpactAdvanced>() }
    val random = remember { Random.Default }

    // Generador continuo de impactos
    LaunchedEffect(Unit) {
        while (true) {
            // Cada 20-40ms genera un nuevo impacto (lluvia intensa)
            delay(random.nextLong(20, 40))

            // Características aleatorias del impacto
            val isLargeDrop = random.nextFloat() < 0.15f // 15% gotas grandes
            val maxRadius = if (isLargeDrop) {
                random.nextFloat() * 40f + 50f // Gotas grandes: 50-90px
            } else {
                random.nextFloat() * 25f + 20f // Gotas normales: 20-45px
            }

            impacts.add(
                RainImpactAdvanced(
                    x = random.nextFloat(),
                    y = random.nextFloat(),
                    age = 0f,
                    maxRadius = maxRadius,
                    ripples = if (isLargeDrop) 3 else 2, // Gotas grandes hacen más ondas
                    speed = if (isLargeDrop) 600f else 450f,
                    alpha = if (isLargeDrop) 0.95f else 0.85f
                )
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Actualizar y dibujar todos los impactos
        val iterator = impacts.iterator()
        while (iterator.hasNext()) {
            val impact = iterator.next()

            // Remover impactos viejos
            if (impact.age > impact.maxRadius * 3.5f) {
                iterator.remove()
                continue
            }

            val xPos = width * impact.x
            val yPos = height * impact.y

            // Progreso de la animación (0 a 1)
            val progress = impact.age / (impact.maxRadius * 2f)

            // Dibujar múltiples ondas concéntricas
            for (rippleIndex in 0 until impact.ripples) {
                // Cada onda empieza con un delay
                val rippleDelay = rippleIndex * 0.25f
                val rippleProgress = (progress - rippleDelay).coerceAtLeast(0f)

                if (rippleProgress > 0f) {
                    val radius = rippleProgress * impact.maxRadius

                    // Alpha que se desvanece: fuerte al inicio, desaparece al final
                    val fadeOut = 1f - rippleProgress
                    val alpha = (impact.alpha * fadeOut * fadeOut).coerceIn(0f, 1f)

                    if (alpha > 0.05f) {
                        // Onda exterior (más gruesa, más visible)
                        drawCircle(
                            color = Color.White.copy(alpha = alpha * 0.8f),
                            radius = radius,
                            center = Offset(xPos, yPos),
                            style = Stroke(
                                width = (8f - rippleProgress * 4f).coerceAtLeast(2f)
                            )
                        )

                        // Onda interior sutil (efecto de profundidad)
                        if (rippleProgress > 0.1f) {
                            drawCircle(
                                color = Color(0xFFB3E5FC).copy(alpha = alpha * 0.4f), // Azul claro
                                radius = radius * 0.7f,
                                center = Offset(xPos, yPos),
                                style = Stroke(width = 1.5f)
                            )
                        }

                        // Splash inicial (solo en los primeros frames)
                        if (rippleProgress < 0.2f && rippleIndex == 0) {
                            val splashAlpha = (1f - rippleProgress / 0.2f) * impact.alpha

                            // Círculo sólido central del impacto
                            drawCircle(
                                color = Color.White.copy(alpha = splashAlpha * 0.6f),
                                radius = radius * 0.3f,
                                center = Offset(xPos, yPos)
                            )

                            // Pequeñas salpicaduras radiales
                            val splashCount = if (impact.maxRadius > 20f) 8 else 6
                            for (i in 0 until splashCount) {
                                val angle = (i / splashCount.toFloat()) * 6.28f
                                val splashDist = radius * 1.2f
                                val splashX = xPos + kotlin.math.cos(angle) * splashDist
                                val splashY = yPos + kotlin.math.sin(angle) * splashDist

                                drawCircle(
                                    color = Color.White.copy(alpha = splashAlpha * 0.5f),
                                    radius = 2f + rippleProgress * 3f,
                                    center = Offset(splashX, splashY)
                                )
                            }
                        }
                    }
                }
            }

            // Incrementar edad (velocidad ajustada por tamaño)
            impact.age += 16f / (impact.speed / 450f) // ~60fps normalizado
        }

        // Efecto de neblina/spray ambiental (gotas microscópicas)
        if (impacts.size > 50) { // Solo cuando hay suficiente lluvia
            drawRect(
                color = Color.White.copy(alpha = 0.03f),
                size = size
            )
        }
    }
}

// Clase de datos mejorada para impactos avanzados
private data class RainImpactAdvanced(
    val x: Float,           // Posición X normalizada (0-1)
    val y: Float,           // Posición Y normalizada (0-1)
    var age: Float,         // Edad en ms
    val maxRadius: Float,   // Radio máximo de expansión
    val ripples: Int,       // Cantidad de ondas concéntricas
    val speed: Float,       // Velocidad de expansión
    val alpha: Float        // Opacidad base
)

@Composable
fun OceanFishEffect(modifier: Modifier = Modifier) {
    // Define las 5 especies de peces con sus características
    val fishSpecies = remember {
        listOf(
            FishSpecies(
                icon = JawsIcon, // Pez 1. Tamaño 24
                baseSize = 100.dp,
                speed = 0.8f,
                depthLayer = 0.3f, // Lejano (pequeño, transparente)
                verticalMovement = 0.02f
            ),
            FishSpecies(
                icon = AnglerFishIcon, // Pez 2. Tamaño 512
                baseSize = 28.dp,
                speed = 1.2f,
                depthLayer = 0.5f, // Medio
                verticalMovement = 0.05f
            ),
            FishSpecies(
                icon = FishSharpIcon, // Pez 3. Tamaño 512
                baseSize = 24.dp,
                speed = 1.5f,
                depthLayer = 0.7f, // Cercano (grande, opaco)
                verticalMovement = 0.03f
            ),
            FishSpecies(
                icon = JellyfishIcon, // Pez 4. Tamaño 32
                baseSize = 36.dp,
                speed = 0.6f,
                depthLayer = 0.4f,
                verticalMovement = 0.01f
            ),
            FishSpecies(
                icon = TropicalFishIcon, // Pez 5. Tamaño 512
                baseSize = 30.dp,
                speed = 1.0f,
                depthLayer = 0.6f,
                verticalMovement = 0.04f
            ),
            FishSpecies(
                icon = WhaleIcon, // Pez 6. Tamaño 64 y tiene colores
                baseSize = 30.dp,
                speed = 1.0f,
                depthLayer = 0.6f,
                verticalMovement = 0.04f
            )
        )
    }

    // Generamos población de peces (20 peces en total)
    val fishes = remember {
        List(20) { index ->
            val species = fishSpecies[index % fishSpecies.size]
            Fish(
                species = species,
                startX = (index * 0.13f) % 1f, // Distribuidos en X
                startY = (index * 0.29f) % 1f, // Distribuidos en Y
                id = index
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fish_swim")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        fishes.forEach { fish ->
            val density = LocalDensity.current
            val fishTime = (time + fish.startX) % 1f
            val xProgress = fishTime

            val verticalOscillation = kotlin.math.sin(fishTime * 6.28f * 3) * fish.species.verticalMovement
            val yPos = fish.startY + verticalOscillation

            val previousTime = (time + fish.startX - 0.01f) % 1f
            val isMovingRight = fishTime > previousTime

            val scaledSize = fish.species.baseSize * fish.species.depthLayer
            val alpha = 0.4f + (fish.species.depthLayer * 0.5f)

            val blueTint = 1f - (fish.species.depthLayer * 0.3f)
            val fishColor = Color(
                red = blueTint,
                green = blueTint,
                blue = 1f,
                alpha = alpha
            )

            Icon(
                imageVector = fish.species.icon,
                contentDescription = null,
                modifier = Modifier
                    .offset {
                        val halfSize = (scaledSize.value * density.density / 2).toInt()
                        IntOffset(
                            x = (xProgress * this@BoxWithConstraints.maxWidth.value * density.density).toInt() - halfSize,
                            y = (yPos * this@BoxWithConstraints.maxHeight.value * density.density).toInt() - halfSize
                        )
                    }
                    .size(scaledSize)
                    .graphicsLayer {
                        scaleX = if (fish.species.icon == WhaleIcon) {
                            if (isMovingRight) -1f else 1f  // Invertido para ballena
                        } else {
                            if (isMovingRight) 1f else -1f  // Normal para otros
                        }
                    },
                tint = if (fish.species.icon == WhaleIcon) Color.Unspecified else fishColor
            )
        }
    }
}

// Clase de datos para especies de peces
private data class FishSpecies(
    val icon: ImageVector,      // Icono del pez (FÁCIL DE CAMBIAR)
    val baseSize: Dp,            // Tamaño base
    val speed: Float,            // Multiplicador de velocidad
    val depthLayer: Float,       // 0.0 = muy lejos, 1.0 = muy cerca
    val verticalMovement: Float  // Amplitud del movimiento vertical
)

// Clase de datos para instancias individuales de peces
private data class Fish(
    val species: FishSpecies,
    val startX: Float,  // Posición inicial X (0-1)
    val startY: Float,  // Posición inicial Y (0-1)
    val id: Int
)