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

// CONTROL: 7-OE
@Composable // Brillo Especular Animado : Efecto de circulos brillantes moviendose en el oceano
fun OceanSpecularEffect(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
    // Sistema de partículas especulares (reflejos de luz solar bajo el agua)
    val specularParticles = remember {
        List(35) { index ->
            SpecularParticle(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(), // Se muestran en toda la pantalla
                size = Random.nextFloat() * 60f + 30f, // Tamaño 30-90px
                speed = Random.nextFloat() * 0.15f + 0.08f, // Velocidad variable lenta
                intensity = Random.nextFloat() * 0.4f + 0.3f, // Intensidad 0.3-0.7
                phase = Random.nextFloat() * 6.28f, // Fase aleatoria
                wobbleSpeed = Random.nextFloat() * 0.5f + 0.3f, // Velocidad oscilación
                wobbleAmp = Random.nextFloat() * 0.03f + 0.01f, // Amplitud oscilación
                depth = Random.nextFloat() // Profundidad (perspectiva)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "specular")

    // Tiempo global para animaciones
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        specularParticles.forEach { particle ->
            // Movimiento horizontal (deriva lenta)
            val xProgress = (particle.startX + time * particle.speed) % 1f
            val xPos = xProgress * canvasWidth

            // Oscilación vertical (simula movimiento de olas)
            val wobble = kotlin.math.sin(time * 6.28f * particle.wobbleSpeed + particle.phase) * particle.wobbleAmp
            val yPos = (particle.startY + wobble) * canvasHeight

            // Variación de intensidad (parpadeo sutil por refracción)
            val flicker = kotlin.math.sin(time * 6.28f * 2f + particle.phase) * 0.15f + 0.85f
            val finalAlpha = particle.intensity * flicker * fadeAlpha

            // Tamaño con perspectiva (partículas más profundas son más pequeñas)
            val perspectiveSize = particle.size * (0.6f + particle.depth * 0.4f)

            // Gradiente radial multi-stop para realismo
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = finalAlpha * 0.9f),           // Centro muy brillante
                    Color(0xFFFFFFF0).copy(alpha = finalAlpha * 0.6f),     // Blanco cálido
                    Color(0xFFC8E6FF).copy(alpha = finalAlpha * 0.3f),     // Azul claro
                    Color(0xFF96C8FF).copy(alpha = finalAlpha * 0.1f),     // Azul más oscuro
                    Color.Transparent                                        // Borde difuso
                ),
                center = Offset(xPos, yPos),
                radius = perspectiveSize
            )

            // Dibujar partícula especular principal
            drawCircle(
                brush = gradient,
                radius = perspectiveSize,
                center = Offset(xPos, yPos)
            )

            // Highlight central adicional (núcleo brillante)
            if (finalAlpha > 0.5f) {
                val coreGradient = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = finalAlpha * 0.7f),
                        Color.White.copy(alpha = finalAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(xPos, yPos),
                    radius = perspectiveSize * 0.3f
                )

                drawCircle(
                    brush = coreGradient,
                    radius = perspectiveSize * 0.3f,
                    center = Offset(xPos, yPos)
                )
            }

            // Reflejo elongado sutil (efecto de agua en movimiento)
            if (particle.depth > 0.6f && finalAlpha > 0.4f) {
                val stretchGradient = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = finalAlpha * 0.2f),
                        Color.Transparent
                    ),
                    start = Offset(xPos, yPos),
                    end = Offset(xPos + perspectiveSize * 0.8f, yPos - perspectiveSize * 0.3f)
                )

                drawRect(
                    brush = stretchGradient,
                    topLeft = Offset(xPos, yPos - 5f),
                    size = androidx.compose.ui.geometry.Size(perspectiveSize * 0.8f, 10f)
                )
            }
        }

        // Capa de brillo ambiental superior (luz difusa del sol)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFC8E6FF).copy(alpha = 0.08f * fadeAlpha),
                    Color.Transparent
                ),
                startY = 0f,
                endY = canvasHeight * 0.5f
            )
        )
    }
}

// Clase de datos para partículas especulares
private data class SpecularParticle(
    val id: Int,
    val startX: Float,        // Posición inicial X (0-1)
    val startY: Float,        // Posición inicial Y (0-1)
    val size: Float,          // Tamaño base de la partícula
    val speed: Float,         // Velocidad de desplazamiento horizontal
    val intensity: Float,     // Intensidad base del brillo
    val phase: Float,         // Fase inicial para variación
    val wobbleSpeed: Float,   // Velocidad de oscilación vertical
    val wobbleAmp: Float,     // Amplitud de oscilación vertical
    val depth: Float          // Profundidad (0=lejano, 1=cercano)
)

@Composable // Gradient Overlay Animado: Un efecto muy bueno de oscurecimiento como un eclipse. Muy Bueno!
fun OceanGradientOverlay(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean_gradient")

    // Tiempo global para animaciones
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Pulsación lenta de intensidad general
    val globalIntensity by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    // Zonas de profundidad (manchas oscuras que simulan variación del fondo marino)
    val depthZones = remember {
        List(8) { index ->
            DepthZone(
                id = index,
                centerX = Random.nextFloat(),
                centerY = Random.nextFloat(),
                size = Random.nextFloat() * 0.4f + 0.3f, // 0.3 a 0.7
                intensity = Random.nextFloat() * 0.25f + 0.15f, // 0.15 a 0.4
                speed = Random.nextFloat() * 0.03f + 0.01f,
                phase = Random.nextFloat() * 6.28f,
                driftX = (Random.nextFloat() - 0.5f) * 0.1f,
                driftY = (Random.nextFloat() - 0.5f) * 0.05f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // CAPA 1: Gradiente base de profundidad (de arriba hacia abajo)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A5C8A).copy(alpha = 0.1f * fadeAlpha), // Azul claro arriba
                    Color(0xFF0D3B5C).copy(alpha = 0.25f * fadeAlpha), // Azul medio
                    Color(0xFF0A2840).copy(alpha = 0.35f * fadeAlpha)  // Azul oscuro abajo
                ),
                startY = 0f,
                endY = canvasHeight
            )
        )

        // CAPA 2: Zonas de profundidad variable (manchas oscuras móviles)
        depthZones.forEach { zone ->
            // Movimiento lento y deriva
            val xOffset = kotlin.math.sin(time * 6.28f * zone.speed + zone.phase) * zone.driftX
            val yOffset = kotlin.math.cos(time * 6.28f * zone.speed * 0.7f + zone.phase) * zone.driftY

            val xPos = (zone.centerX + xOffset) * canvasWidth
            val yPos = (zone.centerY + yOffset) * canvasHeight

            // Pulsación individual sutil
            val pulse = kotlin.math.sin(time * 6.28f * zone.speed * 2f + zone.phase) * 0.15f + 0.85f
            val zoneAlpha = zone.intensity * pulse * globalIntensity * fadeAlpha

            val zoneRadius = zone.size * kotlin.math.max(canvasWidth, canvasHeight)

            // Gradiente radial con múltiples stops para bordes orgánicos
            val zoneGradient = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0A1E2E).copy(alpha = zoneAlpha * 0.8f),     // Centro oscuro
                    Color(0xFF0D2A3F).copy(alpha = zoneAlpha * 0.5f),     // Medio
                    Color(0xFF10334D).copy(alpha = zoneAlpha * 0.25f),    // Transición
                    Color.Transparent                                       // Borde difuso
                ),
                center = Offset(xPos, yPos),
                radius = zoneRadius
            )

            drawCircle(
                brush = zoneGradient,
                radius = zoneRadius,
                center = Offset(xPos, yPos)
            )
        }

        // CAPA 3: Bandas de corrientes submarinas (franjas horizontales sutiles)
        val currentOffset1 = (time * 0.05f) % 1f
        val currentOffset2 = (time * 0.08f + 0.5f) % 1f

        // Corriente 1 (tercio superior)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF1A4D6E).copy(alpha = 0.12f * fadeAlpha),
                    Color(0xFF1A4D6E).copy(alpha = 0.18f * fadeAlpha),
                    Color.Transparent
                ),
                startX = (currentOffset1 - 0.3f) * canvasWidth,
                endX = (currentOffset1 + 0.3f) * canvasWidth
            ),
            topLeft = Offset(0f, canvasHeight * 0.25f),
            size = androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight * 0.15f)
        )

        // Corriente 2 (tercio inferior)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF0D3548).copy(alpha = 0.15f * fadeAlpha),
                    Color(0xFF0D3548).copy(alpha = 0.22f * fadeAlpha),
                    Color.Transparent
                ),
                startX = (currentOffset2 - 0.4f) * canvasWidth,
                endX = (currentOffset2 + 0.4f) * canvasWidth
            ),
            topLeft = Offset(0f, canvasHeight * 0.6f),
            size = androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight * 0.2f)
        )

        // CAPA 4: Cáusticas sutiles (patrones de luz refractada)
        val causticOffset = (time * 0.15f) % 1f

        for (i in 0..5) {
            val causticX = ((causticOffset + i * 0.2f) % 1f) * canvasWidth
            val causticY = (kotlin.math.sin(time * 6.28f * 0.3f + i * 1.2f) * 0.15f + 0.4f) * canvasHeight
            val causticAlpha = (kotlin.math.sin(time * 6.28f * 0.5f + i * 0.8f) * 0.5f + 0.5f) * 0.08f * fadeAlpha

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB3E5FC).copy(alpha = causticAlpha),
                        Color(0xFF81D4FA).copy(alpha = causticAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(causticX, causticY),
                    radius = 120f
                ),
                radius = 120f,
                center = Offset(causticX, causticY)
            )
        }

        // CAPA 5: Vignette tradicional (oscurecimiento de bordes)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF000814).copy(alpha = 0.35f * fadeAlpha)
                ),
                center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                radius = kotlin.math.max(canvasWidth, canvasHeight) * 0.9f
            )
        )

        // CAPA 6: Gradiente sutil de temperatura de color (azul frío en profundidad)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF0A2540).copy(alpha = 0.15f * fadeAlpha)
                ),
                startY = canvasHeight * 0.6f,
                endY = canvasHeight
            )
        )
    }
}

// Clase de datos para zonas de profundidad
private data class DepthZone(
    val id: Int,
    val centerX: Float,       // Posición X (0-1)
    val centerY: Float,       // Posición Y (0-1)
    val size: Float,          // Tamaño relativo
    val intensity: Float,     // Intensidad del oscurecimiento
    val speed: Float,         // Velocidad de movimiento
    val phase: Float,         // Fase inicial
    val driftX: Float,        // Deriva horizontal
    val driftY: Float         // Deriva vertical
)

@Composable // Vignette Dinámico: Bordes oscuros que pulsan, haciendo el centro más prominente.
fun OceanVignette(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "vignette")

    val intensity by infiniteTransition.animateFloat(
        initialValue = 0.15f,  // CAMBIO: era 0.45f (mucho mas suave)
        targetValue = 0.45f,   // CAMBIO: era 0.65f (se reduce la la fuerza)
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing), // CAMBIO: era 8000 (rápido)
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
                radius = size.minDimension * 1f  // CAMBIO: era 0.85f (se veia mucho)
            )
        )
    }
}

@Composable // Burbujas Flotantes
fun OceanBubblesEffect(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")

    // Tiempo global para animaciones
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Pulsación de corrientes submarinas
    val currentIntensity by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "current"
    )

    // Sistema de burbujas multi-capa
    val bubbles = remember {
        // CAPA 1: Micro-burbujas de fondo (20 unidades)
        val microBubbles = List(20) { index ->
            Bubble(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 2f, // 2-5px (muy pequeñas)
                speed = Random.nextFloat() * 0.15f + 0.10f, // Lentas
                wobbleAmp = Random.nextFloat() * 15f + 10f,
                wobbleFreq = Random.nextFloat() * 2f + 1f,
                phase = Random.nextFloat() * 6.28f,
                depth = 0.3f, // Fondo
                opacity = Random.nextFloat() * 0.2f + 0.15f, // Muy tenues
                turbulenceStrength = Random.nextFloat() * 0.02f + 0.01f,
                hasGlow = false
            )
        }

        // CAPA 2: Burbujas medianas (15 unidades)
        val mediumBubbles = List(15) { index ->
            Bubble(
                id = index + 20,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextFloat() * 20f + 15f, // 15-35px
                speed = Random.nextFloat() * 0.25f + 0.20f, // Velocidad media
                wobbleAmp = Random.nextFloat() * 35f + 25f,
                wobbleFreq = Random.nextFloat() * 1.5f + 0.8f,
                phase = Random.nextFloat() * 6.28f,
                depth = 0.6f, // Medio
                opacity = Random.nextFloat() * 0.25f + 0.35f, // Visibles
                turbulenceStrength = Random.nextFloat() * 0.03f + 0.02f,
                hasGlow = Random.nextBoolean()
            )
        }

        // CAPA 3: Burbujas grandes de primer plano (10 unidades)
        val largeBubbles = List(10) { index ->
            Bubble(
                id = index + 35,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextFloat() * 35f + 40f, // 40-75px (grandes)
                speed = Random.nextFloat() * 0.35f + 0.30f, // Rápidas
                wobbleAmp = Random.nextFloat() * 50f + 40f,
                wobbleFreq = Random.nextFloat() * 1.2f + 0.6f,
                phase = Random.nextFloat() * 6.28f,
                depth = 0.9f, // Primer plano
                opacity = Random.nextFloat() * 0.3f + 0.50f, // Muy visibles
                turbulenceStrength = Random.nextFloat() * 0.04f + 0.03f,
                hasGlow = true // Todas tienen brillo
            )
        }

        microBubbles + mediumBubbles + largeBubbles
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        bubbles.forEach { bubble ->
            // Movimiento vertical ascendente (de abajo hacia arriba)
            val yProgress = (bubble.startY + time * bubble.speed * currentIntensity) % 1f
            val yPos = canvasHeight * (1f - yProgress) // Invertido: 1=abajo, 0=arriba

            // Oscilación lateral base (zigzag)
            val wobbleBase = kotlin.math.sin(yProgress * 6.28f * bubble.wobbleFreq + bubble.phase) * bubble.wobbleAmp

            // Turbulencias aleatorias adicionales (simulan corrientes)
            val turbulence1 = kotlin.math.sin(time * 6.28f * 0.5f + bubble.phase) * bubble.turbulenceStrength * canvasWidth
            val turbulence2 = kotlin.math.cos(time * 6.28f * 0.3f + bubble.phase * 1.5f) * bubble.turbulenceStrength * canvasWidth

            val xPos = (bubble.startX * canvasWidth) + wobbleBase + turbulence1 + turbulence2

            // Fade out al llegar arriba (últimos 15% del recorrido)
            val fadeOut = if (yProgress > 0.85f) {
                (1f - yProgress) / 0.15f
            } else {
                1f
            }

            // Alpha final con profundidad y fade
            val depthAlpha = bubble.opacity * bubble.depth
            val finalAlpha = depthAlpha * fadeOut * fadeAlpha

            // Tamaño con perspectiva (burbujas más grandes cerca)
            val perspectiveSize = bubble.size * bubble.depth

            // Variación de tamaño por oscilación (burbujas "respiran")
            val breathe = kotlin.math.sin(time * 6.28f * 2f + bubble.phase) * 0.08f + 1f
            val animatedSize = perspectiveSize * breathe

            if (finalAlpha > 0.05f) {
                // BURBUJA BASE (círculo translúcido)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = finalAlpha * 0.4f), // Centro semi-opaco
                            Color.White.copy(alpha = finalAlpha * 0.6f), // Medio más opaco
                            Color(0xFFE3F2FD).copy(alpha = finalAlpha * 0.3f), // Borde azulado
                            Color.Transparent
                        ),
                        center = Offset(xPos, yPos),
                        radius = animatedSize
                    ),
                    radius = animatedSize,
                    center = Offset(xPos, yPos)
                )

                // BORDE DE LA BURBUJA (contorno sutil)
                drawCircle(
                    color = Color.White.copy(alpha = finalAlpha * 0.5f),
                    radius = animatedSize,
                    center = Offset(xPos, yPos),
                    style = Stroke(width = (0.5f + bubble.depth * 1f))
                )

                // REFLEJO ESPECULAR (highlight en la parte superior)
                val highlightOffset = animatedSize * 0.35f
                val highlightX = xPos - highlightOffset * 0.5f
                val highlightY = yPos - highlightOffset * 0.7f
                val highlightSize = animatedSize * 0.4f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = finalAlpha * 0.8f),
                            Color.White.copy(alpha = finalAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(highlightX, highlightY),
                        radius = highlightSize
                    ),
                    radius = highlightSize,
                    center = Offset(highlightX, highlightY)
                )

                // GLOW EXTERIOR (solo burbujas grandes con hasGlow)
                if (bubble.hasGlow && bubble.depth > 0.7f) {
                    val glowIntensity = kotlin.math.sin(time * 6.28f * 1.5f + bubble.phase) * 0.3f + 0.7f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = finalAlpha * 0.3f * glowIntensity),
                                Color(0xFFB3E5FC).copy(alpha = finalAlpha * 0.15f * glowIntensity),
                                Color.Transparent
                            ),
                            center = Offset(xPos, yPos),
                            radius = animatedSize * 1.5f
                        ),
                        radius = animatedSize * 1.5f,
                        center = Offset(xPos, yPos)
                    )
                }

                // REFLEJO SECUNDARIO (pequeño punto de luz opuesto)
                if (bubble.size > 20f) {
                    val secondaryX = xPos + highlightOffset * 0.8f
                    val secondaryY = yPos + highlightOffset * 0.6f
                    val secondarySize = animatedSize * 0.15f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = finalAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(secondaryX, secondaryY),
                            radius = secondarySize
                        ),
                        radius = secondarySize,
                        center = Offset(secondaryX, secondaryY)
                    )
                }
            }
        }

        // ESTELA DE MICRO-BURBUJAS EN CORRIENTES (efecto ambiental)
        val trailCount = 8
        for (i in 0 until trailCount) {
            val trailProgress = (time * 0.3f + i * 0.125f) % 1f
            val trailX = (kotlin.math.sin(trailProgress * 6.28f * 2f) * 0.3f + 0.5f) * canvasWidth
            val trailY = canvasHeight * (1f - trailProgress)
            val trailAlpha = (1f - trailProgress) * 0.15f * fadeAlpha

            if (trailAlpha > 0.02f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = trailAlpha),
                            Color.Transparent
                        ),
                        center = Offset(trailX, trailY),
                        radius = 3f
                    ),
                    radius = 3f,
                    center = Offset(trailX, trailY)
                )
            }
        }
    }
}

// Clase de datos para burbujas mejorada
private data class Bubble(
    val id: Int,
    val startX: Float,              // Posición inicial X (0-1)
    val startY: Float,              // Posición inicial Y (0-1)
    val size: Float,                // Tamaño en px
    val speed: Float,               // Velocidad de ascenso
    val wobbleAmp: Float,           // Amplitud de oscilación lateral
    val wobbleFreq: Float,          // Frecuencia de oscilación
    val phase: Float,               // Fase para variación
    val depth: Float,               // Profundidad (0.3=fondo, 0.9=primer plano)
    val opacity: Float,             // Opacidad base
    val turbulenceStrength: Float,  // Fuerza de turbulencias
    val hasGlow: Boolean            // Si tiene glow exterior
)

@Composable // Rayos de luz que atraviesan el agua (God Rays)
fun OceanGodRaysEffect(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "god_rays")

    // Tiempo global para animaciones
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Pulsación de intensidad general (nubes pasando sobre el sol)
    val globalIntensity by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    // Sistema de rayos
    val godRays = remember {
        List(14) { index ->
            GodRaySimple(
                id = index,
                startX = (index * 0.15f) % 1.3f - 0.15f, // Distribuidos con offset
                topY = -0.1f - Random.nextFloat() * 0.2f, // Empiezan arriba fuera de pantalla
                angle = Random.nextFloat() * 8f + 78f, // Ángulos 78-86 grados (casi vertical)
                width = Random.nextFloat() * 100f + 60f, // Ancho 60-160px
                speed = Random.nextFloat() * 0.04f + 0.02f, // Velocidad 0.02-0.06
                intensity = Random.nextFloat() * 0.3f + 0.2f, // Intensidad 0.2-0.5
                phase = Random.nextFloat() * 6.28f,
                flickerSpeed = Random.nextFloat() * 0.6f + 0.4f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        godRays.forEach { ray ->
            // MOVIMIENTO HORIZONTAL (cruza de izquierda a derecha)
            val xProgress = (ray.startX + time * ray.speed) % 1.3f

            // Si está fuera del rango visible, skip
            if (xProgress < -0.15f || xProgress > 1.15f) return@forEach

            // Posición X actual del rayo en píxeles
            val rayX = xProgress * canvasWidth

            // Oscilación muy sutil del ángulo (agua moviéndose)
            val angleWobble = kotlin.math.sin(time * 6.28f * 0.2f + ray.phase) * 2f
            val currentAngle = ray.angle + angleWobble
            val angleRad = Math.toRadians(currentAngle.toDouble()).toFloat()

            // Parpadeo por turbulencia del agua
            val flicker = kotlin.math.sin(time * 6.28f * ray.flickerSpeed + ray.phase) * 0.25f + 0.75f
            val finalAlpha = ray.intensity * flicker * globalIntensity * fadeAlpha

            // CALCULAR GEOMETRÍA DEL RAYO
            // El rayo es un "pilar" diagonal que atraviesa toda la pantalla verticalmente

            // Punto superior (fuera de pantalla arriba)
            val topX = rayX
            val topY = ray.topY * canvasHeight

            // Punto inferior (calculado por trigonometría)
            val rayHeight = canvasHeight * 1.3f // Un poco más largo para cubrir todo
            val bottomX = topX - kotlin.math.tan(Math.toRadians((90.0 - currentAngle).toDouble())).toFloat() * rayHeight
            val bottomY = topY + rayHeight

            // Ancho del rayo con ligera perspectiva (más ancho abajo)
            val topWidth = ray.width
            val bottomWidth = ray.width * 1.15f

            // Vector perpendicular al rayo para crear los bordes
            val perpAngle = angleRad - Math.PI.toFloat() / 2f
            val perpX = kotlin.math.cos(perpAngle)
            val perpY = kotlin.math.sin(perpAngle)

            // CREAR PATH DEL RAYO (trapecio alargado)
            val rayPath = androidx.compose.ui.graphics.Path().apply {
                // Lado izquierdo superior
                moveTo(topX + perpX * topWidth / 2f, topY + perpY * topWidth / 2f)
                // Lado izquierdo inferior
                lineTo(bottomX + perpX * bottomWidth / 2f, bottomY + perpY * bottomWidth / 2f)
                // Lado derecho inferior
                lineTo(bottomX - perpX * bottomWidth / 2f, bottomY - perpY * bottomWidth / 2f)
                // Lado derecho superior
                lineTo(topX - perpX * topWidth / 2f, topY - perpY * topWidth / 2f)
                close()
            }

            // GRADIENTE A LO LARGO DEL RAYO (de arriba hacia abajo)
            // Más brillante arriba (cerca de la superficie), más tenue abajo
            val rayGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFF5).copy(alpha = finalAlpha * 0.85f), // Blanco cálido arriba
                    Color(0xFFE8F4F8).copy(alpha = finalAlpha * 0.65f), // Blanco azulado
                    Color(0xFFB3E5FC).copy(alpha = finalAlpha * 0.40f), // Azul claro
                    Color(0xFF81D4FA).copy(alpha = finalAlpha * 0.20f), // Azul medio
                    Color(0xFF4FC3F7).copy(alpha = finalAlpha * 0.05f), // Azul oscuro
                    Color.Transparent
                ),
                start = Offset(topX, topY),
                end = Offset(bottomX, bottomY)
            )

            // Dibujar rayo principal
            drawPath(
                path = rayPath,
                brush = rayGradient
            )

            // NÚCLEO BRILLANTE CENTRAL (30% del ancho)
            if (finalAlpha > 0.3f) {
                val coreTopWidth = topWidth * 0.3f
                val coreBottomWidth = bottomWidth * 0.3f

                val corePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(topX + perpX * coreTopWidth / 2f, topY + perpY * coreTopWidth / 2f)
                    lineTo(bottomX + perpX * coreBottomWidth / 2f, bottomY + perpY * coreBottomWidth / 2f)
                    lineTo(bottomX - perpX * coreBottomWidth / 2f, bottomY - perpY * coreBottomWidth / 2f)
                    lineTo(topX - perpX * coreTopWidth / 2f, topY - perpY * coreTopWidth / 2f)
                    close()
                }

                val coreGradient = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = finalAlpha * 0.6f),
                        Color(0xFFFFFFF0).copy(alpha = finalAlpha * 0.4f),
                        Color(0xFFE3F2FD).copy(alpha = finalAlpha * 0.2f),
                        Color.Transparent
                    ),
                    start = Offset(topX, topY),
                    end = Offset(bottomX, bottomY)
                )

                drawPath(
                    path = corePath,
                    brush = coreGradient
                )
            }

            // PARTÍCULAS DE POLVO/PLANCTON flotando en el rayo
            val particleCount = 12
            for (i in 0 until particleCount) {
                // Posición a lo largo del rayo (0 = arriba, 1 = abajo)
                val particleRatio = (i.toFloat() / particleCount + time * 0.15f + ray.phase * 0.1f) % 1f

                // Interpolación de posición
                val pX = topX + (bottomX - topX) * particleRatio
                val pY = topY + (bottomY - topY) * particleRatio

                // Oscilación lateral dentro del rayo
                val lateralWobble = kotlin.math.sin(time * 6.28f * 1.5f + ray.phase + i * 0.5f) * ray.width * 0.25f
                val finalPX = pX + perpX * lateralWobble
                val finalPY = pY + perpY * lateralWobble

                // Parpadeo individual de partícula
                val particleFlicker = kotlin.math.sin(time * 6.28f * 2f + i * 0.8f) * 0.5f + 0.5f
                val particleAlpha = finalAlpha * particleFlicker * 0.5f

                if (particleAlpha > 0.15f) {
                    // Tamaño variable según profundidad
                    val particleSize = 3f + (1f - particleRatio) * 2f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = particleAlpha * 0.9f),
                                Color(0xFFE3F2FD).copy(alpha = particleAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(finalPX, finalPY),
                            radius = particleSize
                        ),
                        radius = particleSize,
                        center = Offset(finalPX, finalPY)
                    )
                }
            }
        }

        // DIFUSIÓN ATMOSFÉRICA superior (brillo general del agua)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE8F4F8).copy(alpha = 0.06f * globalIntensity * fadeAlpha),
                    Color.Transparent
                ),
                startY = 0f,
                endY = canvasHeight * 0.35f
            )
        )
    }
}

// Clase de datos simplificada
private data class GodRaySimple(
    val id: Int,
    val startX: Float,        // Posición inicial X normalizada
    val topY: Float,          // Posición Y superior (negativa = fuera pantalla)
    val angle: Float,         // Ángulo en grados (78-86)
    val width: Float,         // Ancho del rayo en px
    val speed: Float,         // Velocidad de desplazamiento horizontal
    val intensity: Float,     // Intensidad base
    val phase: Float,         // Fase para variación
    val flickerSpeed: Float   // Velocidad de parpadeo
)

@Composable
fun OceanMistEffect(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
    // Sistema de partículas de niebla orgánica
    val mistParticles = remember {
        List(120) { index -> // era 80, ahora 120 para llenar toda la pantalla
            MistParticle(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(), // Concentrada en toda la pantalla
                size = Random.nextFloat() * 200f + 100f, // era 150f + 80f
                speed = Random.nextFloat() * 0.3f + 0.15f, // Velocidad lenta y variable
                depth = Random.nextFloat(), // Profundidad para parallax
                opacity = Random.nextFloat() * 0.20f + 0.12f, // Opacidad muy sutil  -era 0.15f + 0.08f
                drift = Random.nextFloat() * 30f - 15f, // Deriva vertical
                wobbleFreq = Random.nextFloat() * 2f + 1f, // Frecuencia de ondulación
                wobbleAmp = Random.nextFloat() * 40f + 20f // Amplitud de ondulación
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mist")

    // Tiempo global para animaciones
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Pulsación lenta de opacidad general
    val globalAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "globalAlpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        mistParticles.forEach { particle ->
            // Movimiento horizontal (deriva lenta con parallax)
            val timeOffset = (time + particle.startX) % 1f
            val xProgress = timeOffset * particle.speed
            val xPos = ((particle.startX + xProgress) % 1f) * canvasWidth

            // Movimiento vertical (ondulación sutil)
            val wobble = kotlin.math.sin(timeOffset * 6.28f * particle.wobbleFreq) * particle.wobbleAmp
            val yPos = (particle.startY * canvasHeight) + particle.drift + wobble

            // Opacidad con variación temporal
            val timeBasedFade = kotlin.math.sin(timeOffset * 6.28f * 0.5f) * 0.3f + 0.7f
            val finalAlpha = particle.opacity * globalAlpha * timeBasedFade * particle.depth * fadeAlpha

            // Crear gradiente orgánico con múltiples stops para bordes difusos
            val gradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = finalAlpha * 0.8f),
                    Color.White.copy(alpha = finalAlpha * 0.5f),
                    Color.White.copy(alpha = finalAlpha * 0.25f),
                    Color.White.copy(alpha = finalAlpha * 0.1f),
                    Color.Transparent
                ),
                center = Offset(xPos, yPos),
                radius = particle.size * (0.8f + particle.depth * 0.4f)
            )

            // Dibujar partícula de niebla con forma más orgánica
            drawCircle(
                brush = gradient,
                radius = particle.size * (0.8f + particle.depth * 0.4f),
                center = Offset(xPos, yPos)
            )

            // Capa adicional desplazada para efecto volumétrico
            val offsetX = kotlin.math.cos(timeOffset * 6.28f) * 15f
            val offsetY = kotlin.math.sin(timeOffset * 6.28f) * 10f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = finalAlpha * 0.4f),
                        Color.White.copy(alpha = finalAlpha * 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(xPos + offsetX, yPos + offsetY),
                    radius = particle.size * 0.6f
                ),
                radius = particle.size * 0.6f,
                center = Offset(xPos + offsetX, yPos + offsetY)
            )
        }

        // Capa de neblina ambiental global (efecto atmosférico sutil)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.02f * globalAlpha * fadeAlpha),
                    Color.White.copy(alpha = 0.05f * globalAlpha * fadeAlpha),
                    Color.Transparent
                ),
                startY = 0f,
                endY = canvasHeight * 0.4f
            )
        )
    }
}

// Clase de datos para partículas de niebla
private data class MistParticle(
    val id: Int,
    val startX: Float,      // Posición inicial X (0-1)
    val startY: Float,      // Posición inicial Y (0-1)
    val size: Float,        // Tamaño de la partícula
    val speed: Float,       // Velocidad de movimiento horizontal
    val depth: Float,       // Profundidad (0=lejano, 1=cercano)
    val opacity: Float,     // Opacidad base
    val drift: Float,       // Deriva vertical constante
    val wobbleFreq: Float,  // Frecuencia de ondulación
    val wobbleAmp: Float    // Amplitud de ondulación
)

@Composable
fun StormEffect(
    modifier: Modifier = Modifier,
    onThunderSound: () -> Unit,
    fadeAlpha: Float = 1f
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
        drawRect(color = Color.Black.copy(alpha = 0.35f * fadeAlpha))

        // Flash del relámpago
        if (flashIntensity > 0.5f) {
            drawRect(color = Color.White.copy(alpha = flashIntensity * fadeAlpha))
        } else if (flashIntensity < 0f) {
            drawRect(color = Color.Black.copy(alpha = -flashIntensity * fadeAlpha))
        }
    }
}

@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
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
                    val alpha = (impact.alpha * fadeOut * fadeOut * fadeAlpha).coerceIn(0f, 1f)

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
                color = Color.White.copy(alpha = 0.03f * fadeAlpha),
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
fun OceanFishEffect(
    modifier: Modifier = Modifier,
    fadeAlpha: Float = 1f
) {
    // Define las 6 especies de peces con características CORREGIDAS
    val fishSpecies = remember {
        listOf(
            FishSpecies(
                icon = JawsIcon,
                baseSize = 85.dp,        // Tiburón grande pero no pixelado
                speed = 0.5f,            // Lento y amenazante
                depthLayer = 0.90f,      // Muy cercano (imponente)
                verticalMovement = 0.01f // Movimiento sutil
            ),
            FishSpecies(
                icon = AnglerFishIcon,
                baseSize = 45.dp,        // Aumentado de 28
                speed = 0.8f,
                depthLayer = 0.35f,      // Lejano (abisal)
                verticalMovement = 0.03f
            ),
            FishSpecies(
                icon = FishSharpIcon,
                baseSize = 38.dp,        // Aumentado de 24
                speed = 1.5f,            // Rápido y ágil
                depthLayer = 0.65f,
                verticalMovement = 0.04f
            ),
            FishSpecies(
                icon = JellyfishIcon,
                baseSize = 55.dp,        // Aumentado de 36
                speed = 0.3f,            // Muy lenta (flotante)
                depthLayer = 0.50f,
                verticalMovement = 0.06f // Ondulación pronunciada
            ),
            FishSpecies(
                icon = TropicalFishIcon,
                baseSize = 42.dp,        // Aumentado de 30
                speed = 1.2f,
                depthLayer = 0.70f,      // Cercano y visible
                verticalMovement = 0.05f
            ),
            FishSpecies(
                icon = WhaleIcon,
                baseSize = 95.dp,        // TRIPLICADO de 30 - ES UNA BALLENA
                speed = 0.4f,            // Lenta y majestuosa
                depthLayer = 0.95f,      // PRIMER PLANO ABSOLUTO
                verticalMovement = 0.02f // Movimiento suave
            )
        )
    }

    // Población con sistema de rareza: 2 ballenas + 2 tiburones + 21 comunes
    val fishes = remember {
        buildList {
            // RAROS: 2 Ballenas
            repeat(2) { i ->
                add(Fish(
                    species = fishSpecies[5], // WhaleIcon
                    startX = (i * 0.45f + 0.1f) % 1f,
                    startY = (i * 0.35f + 0.2f) % 1f,
                    id = size
                ))
            }

            // RAROS: 2 Tiburones
            repeat(2) { i ->
                add(Fish(
                    species = fishSpecies[0], // JawsIcon
                    startX = (i * 0.55f + 0.25f) % 1f,
                    startY = (i * 0.45f + 0.15f) % 1f,
                    id = size
                ))
            }

            // COMUNES: 21 peces (índices 1-4)
            repeat(21) { i ->
                val commonIndex = 1 + (i % 4) // AnglerFish, FishSharp, Jellyfish, TropicalFish
                add(Fish(
                    species = fishSpecies[commonIndex],
                    startX = (i * 0.11f) % 1f,
                    startY = (i * 0.23f) % 1f,
                    id = size
                ))
            }
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

            // Movimiento vertical con oscilación
            val verticalOscillation = kotlin.math.sin(fishTime * 6.28f * 3) * fish.species.verticalMovement
            val yPos = fish.startY + verticalOscillation

            // Detectar dirección
            val previousTime = (time + fish.startX - 0.01f) % 1f
            val isMovingRight = fishTime > previousTime

            // Tamaño con perspectiva (CORREGIDO)
            val scaledSize = fish.species.baseSize * fish.species.depthLayer

            // SISTEMA DE COLOR CORREGIDO - 3 capas de profundidad
            val fishColor = when {
                // Peces muy lejanos (abisales): Oscuros y tenues
                fish.species.depthLayer < 0.4f -> {
                    Color(
                        red = 0.25f,
                        green = 0.35f,
                        blue = 0.55f,
                        alpha = (0.4f + fish.species.depthLayer * 0.3f) * fadeAlpha // fadeAlpha integrado
                    )
                }
                // Peces medios: Azul moderado
                fish.species.depthLayer < 0.7f -> {
                    Color(
                        red = 0.45f,
                        green = 0.65f,
                        blue = 0.85f,
                        alpha = (0.5f + fish.species.depthLayer * 0.4f) * fadeAlpha // fadeAlpha integrado
                    )
                }
                // Peces cercanos: Brillantes y nítidos
                else -> {
                    Color(
                        red = 0.60f,
                        green = 0.80f,
                        blue = 1.0f,
                        alpha = (0.6f + fish.species.depthLayer * 0.4f) * fadeAlpha // fadeAlpha integrado
                    )
                }
            }

            // Respiración aumentada (más notoria)
            val breathe = kotlin.math.sin(time * 6.28f * 2f + fish.id * 0.5f) * 0.12f + 1f // Aumentado de 0.08 a 0.12
            val animatedSize = scaledSize * breathe

            Icon(
                imageVector = fish.species.icon,
                contentDescription = null,
                modifier = Modifier
                    .offset {
                        val halfSize = (animatedSize.value * density.density / 2).toInt()
                        IntOffset(
                            x = (xProgress * this@BoxWithConstraints.maxWidth.value * density.density).toInt() - halfSize,
                            y = (yPos * this@BoxWithConstraints.maxHeight.value * density.density).toInt() - halfSize
                        )
                    }
                    .size(animatedSize)
                    .graphicsLayer {
                        scaleX = if (fish.species.icon == WhaleIcon) {
                            if (isMovingRight) -1f else 1f  // Invertido para ballena
                        } else {
                            if (isMovingRight) 1f else -1f  // Normal para otros
                        }
                        alpha = if (fish.species.icon == WhaleIcon) {
                            (0.6f + fish.species.depthLayer * 0.4f) * fadeAlpha  // Misma fórmula que otros peces
                        } else {
                            1f  // Los demás usan el alpha del tint
                        }
                    },
                tint = if (fish.species.icon == WhaleIcon) Color.Unspecified else fishColor
            )
        }
    }
}

// Clases de datos (SIN CAMBIOS)
private data class FishSpecies(
    val icon: ImageVector,
    val baseSize: Dp,
    val speed: Float,
    val depthLayer: Float,
    val verticalMovement: Float
)

private data class Fish(
    val species: FishSpecies,
    val startX: Float,
    val startY: Float,
    val id: Int
)