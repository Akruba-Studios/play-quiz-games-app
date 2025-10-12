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
import com.akrubastudios.playquizgames.domain.models.ParsedVisualTheme
import com.akrubastudios.playquizgames.ui.screens.boss.background.PatternGenerator
import com.akrubastudios.playquizgames.ui.theme.SkyBlue

@Composable
fun GameScreenBackground(
    visualTheme: ParsedVisualTheme?,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Fondo base del tema (LightGray o DeepNavy)
    ) {
        // Solo dibujamos el patrón si tenemos un tema visual definido
        visualTheme?.let { theme ->
            Log.d("GameBackground", "✅ VisualTheme recibido: countryId=${theme.countryId}, pattern=${theme.archetype.patternType}")
            // Animación infinita para el pulso del patrón
            val infiniteTransition = rememberInfiniteTransition(label = "patternPulse")
            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = 0.25f, // Opacidad mínima (muy sutil)
                targetValue = 0.45f, // Opacidad máxima
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 5000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "patternAlpha"
            )

            // Determinamos el color del patrón según el tema del dispositivo
            val patternColor = if (isSystemInDarkTheme()) {
                Color.White.copy(alpha = animatedAlpha * 1.5f) // Blanco en tema oscuro
            } else {
                Color.Black.copy(alpha = 0.15f) // Negro en tema claro
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Llamamos al generador de patrones que ya existe
                PatternGenerator.drawPattern(
                    drawScope = this,
                    patternType = theme.archetype.patternType,
                    color = patternColor,
                    countryId = theme.countryId,
                    phase = 1 // Usamos siempre la fase 1 para una menor densidad
                )
            }
        }

        // FUERA del let:
        if (visualTheme == null) {
            Log.d("GameBackground", "❌ VisualTheme es NULL")
        }

        // El contenido de la pantalla se dibuja encima del fondo y el patrón
        content()
    }
}