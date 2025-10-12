// En el nuevo archivo: app/src/main/java/com/akrubastudios/playquizgames/ui/screens/game/GameScreenBackground.kt

package com.akrubastudios.playquizgames.ui.screens.game

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.akrubastudios.playquizgames.domain.models.ParsedVisualTheme
import com.akrubastudios.playquizgames.ui.screens.boss.background.PatternGenerator
import com.akrubastudios.playquizgames.ui.theme.SkyBlue

@Composable
fun GameScreenBackground(
    visualTheme: ParsedVisualTheme?,
    questionNumber: Int = 1,
    totalQuestions: Int = 10,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        visualTheme?.let { theme ->
            val isDark = isSystemInDarkTheme()

            // Progreso de 0f (inicio) a 1f (final)
            val progress = if (totalQuestions > 0) {
                (questionNumber - 1f) / totalQuestions.toFloat()
            } else 0f

            // Animaciones
            val infiniteTransition = rememberInfiniteTransition(label = "patternEffects")

            // Rotación lenta (1 vuelta cada 80 segundos)
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 80000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "patternRotation"
            )

            // Pulso de alpha
            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = if (isDark) 0.12f else 0.20f,
                targetValue = if (isDark) 0.22f else 0.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 5000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "patternAlpha"
            )

            // Interpolación de colores según progreso
            val patternColor = if (isDark) {
                when {
                    progress < 0.5f -> {
                        val localProgress = progress / 0.5f
                        androidx.compose.ui.graphics.lerp(
                            Color(0xFF3B82F6), // Azul medio
                            Color(0xFF06B6D4), // Cyan medio
                            localProgress
                        )
                    }
                    else -> {
                        val localProgress = (progress - 0.5f) / 0.5f
                        androidx.compose.ui.graphics.lerp(
                            Color(0xFF06B6D4), // Cyan medio
                            Color(0xFFF59E0B), // Amarillo medio
                            localProgress
                        )
                    }
                }.copy(alpha = animatedAlpha)
            } else {
                when {
                    progress < 0.5f -> {
                        val localProgress = progress / 0.5f
                        androidx.compose.ui.graphics.lerp(
                            Color(0xFF1E40AF), // Azul oscuro
                            Color(0xFF7C3AED), // Púrpura oscuro
                            localProgress
                        )
                    }
                    else -> {
                        val localProgress = (progress - 0.5f) / 0.5f
                        androidx.compose.ui.graphics.lerp(
                            Color(0xFF7C3AED), // Púrpura oscuro
                            Color(0xFF047857), // Verde oscuro
                            localProgress
                        )
                    }
                }.copy(alpha = animatedAlpha)
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 2f // ← Escala 100% más grande
                        scaleY = 2f // ← Escala 100% más grande
                        rotationZ = rotation
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                    }
            ) {
                PatternGenerator.drawPattern(
                    drawScope = this,
                    patternType = theme.archetype.patternType,
                    color = patternColor,
                    countryId = theme.countryId,
                    phase = 1
                )
            }
        }

        content()
    }
}