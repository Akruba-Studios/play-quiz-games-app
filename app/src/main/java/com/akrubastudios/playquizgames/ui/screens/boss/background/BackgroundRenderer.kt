package com.akrubastudios.playquizgames.ui.screens.boss.background

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.akrubastudios.playquizgames.domain.models.ParsedVisualTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Componente principal que renderiza el fondo dinámico completo: Control 1-BR
 */
@Composable
fun DynamicBossBackground(
    visualTheme: ParsedVisualTheme,
    currentPhase: Int,
    shakeEffect: Boolean
) {
    // Colores de la fase actual
    val phaseColors = remember(currentPhase) {
        when(currentPhase) {
            1 -> visualTheme.archetype.colorPalette.phase1
            2 -> visualTheme.archetype.colorPalette.phase2
            3 -> visualTheme.archetype.colorPalette.phase3
            else -> visualTheme.archetype.colorPalette.phase1
        }
    }

    // Animación suave entre fases
    val backgroundTopColor by animateColorAsState(
        targetValue = phaseColors.backgroundTop,
        animationSpec = tween(durationMillis = 1500),
        label = "backgroundTop"
    )

    val backgroundBottomColor by animateColorAsState(
        targetValue = phaseColors.backgroundBottom,
        animationSpec = tween(durationMillis = 1500),
        label = "backgroundBottom"
    )

    // Efecto de shake
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeEffect) 20f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "shake"
    )

    // Pulso para fases 2 y 3
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundPulse")
    val pulseIntensity by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = when(currentPhase) {
            1 -> 1.0f      // Sin pulso
            2 -> 0.55f     // Oscurece 45%
            3 -> 0.35f     // Oscurece 65% - casi negro
            else -> 1.0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (currentPhase == 3) 400 else 800,
                easing = LinearEasing  // Sin suavizado, más brusco
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = shakeOffset.dp, y = shakeOffset.dp)
    ) {
        // Capa 1: Gradiente base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundTopColor.copy(
                                red = (backgroundTopColor.red * pulseIntensity).coerceIn(0f, 1f),
                                green = (backgroundTopColor.green * pulseIntensity).coerceIn(0f, 1f),
                                blue = (backgroundTopColor.blue * pulseIntensity).coerceIn(0f, 1f)
                            ),
                            backgroundBottomColor.copy(
                                red = (backgroundBottomColor.red * pulseIntensity).coerceIn(0f, 1f),
                                green = (backgroundBottomColor.green * pulseIntensity).coerceIn(0f, 1f),
                                blue = (backgroundBottomColor.blue * pulseIntensity).coerceIn(0f, 1f)
                            )
                        )
                    )
                )
        )

        // Capa 2-5: Canvas con patrones, partículas y vignette
        BackgroundCanvas(
            visualTheme = visualTheme,
            phaseColors = phaseColors,
            currentPhase = currentPhase,
            accentColor = visualTheme.accentColor
        )
    }
}

/**
 * Canvas que dibuja patrones, partículas y vignette
 */
@Composable
private fun BackgroundCanvas(
    visualTheme: ParsedVisualTheme,
    phaseColors: PhaseColors,
    currentPhase: Int,
    accentColor: Color
) {
    // Estado del sistema de partículas
    var particleSystem by remember { mutableStateOf<ParticleSystemManager?>(null) }
    var lastFrameTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Inicializar sistema de partículas si no existe
        if (particleSystem == null) {
            particleSystem = ParticleSystemManager(
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                style = visualTheme.archetype.particleStyle,
                colors = phaseColors.particleColors,
                phase = currentPhase
            )
        }

        // Capa 2: Patrón cultural
        PatternGenerator.drawPattern(
            drawScope = this,
            patternType = visualTheme.archetype.patternType,
            color = phaseColors.patternColor,
            countryId = visualTheme.countryId,
            phase = currentPhase
        )

        // Capa 3: Partículas ambientales
        particleSystem?.let { system ->
            system.getParticles().forEach { particle ->
                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = particle.position
                )
            }
        }

        // Capa 4: Vignette dinámico
        drawVignette(
            vignetteColor = phaseColors.vignetteColor,
            intensity = when(currentPhase) {
                1 -> 0.3f
                2 -> 0.5f
                3 -> 0.65f
                else -> 0.3f
            }
        )
    }

    // Loop de actualización de partículas
    LaunchedEffect(currentPhase, phaseColors) {
        particleSystem?.adjustForPhase(currentPhase, phaseColors.particleColors)

        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                val currentTime = frameTimeMillis
                val deltaTime = (currentTime - lastFrameTime) / 1000f
                lastFrameTime = currentTime

                particleSystem?.update(deltaTime)
            }
        }
    }
}

/**
 * Dibuja un vignette radial desde los bordes
 */
private fun DrawScope.drawVignette(vignetteColor: Color, intensity: Float) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val maxRadius = maxOf(size.width, size.height) * 0.6f  // Más pequeño = más oscuro

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                vignetteColor.copy(alpha = intensity * 0.8f),  // Era 0.5f
                vignetteColor.copy(alpha = (intensity * 1.2f).coerceIn(0f, 1f))  // Era 1.0f
            ),
            center = Offset(centerX, centerY),
            radius = maxRadius
        ),
        center = Offset(centerX, centerY),
        radius = maxRadius
    )
}