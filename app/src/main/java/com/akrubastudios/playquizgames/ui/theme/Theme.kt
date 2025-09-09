package com.akrubastudios.playquizgames.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Paleta para el Tema CLARO
private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = PureWhite,
    primaryContainer = SkyBlue.copy(alpha = 0.2f),
    onPrimaryContainer = DeepNavy,
    secondary = DarkGoldAccent,
    onSecondary = DeepNavy,
    secondaryContainer = CyanAccent.copy(alpha = 0.2f),
    onSecondaryContainer = DeepNavy,
    tertiary = GoldAccent,
    onTertiary = PureWhite,
    tertiaryContainer = GoldAccent.copy(alpha = 0.8f), // Fondo Avión
    onTertiaryContainer = DeepNavy,
    background = LightGray,
    onBackground = DeepNavy,
    // ROL PARA DIÁLOGOS Y SUPERFICIES BASE: SÓLIDO Y OPACO
        // Surface: Barra Begginner y Hard en FreeMode y LevelSelectionScreen
        //  - También: Barra principal Settings y botones sound y music; y Barra Principal Knowledge
        //  - También: Variaciones de surface en Niveles disponibles y apagados en LevelSeleccionScreen
    surface = SkyBlue.copy(alpha = 0.10f),
    onSurface = DeepNavy,
    surfaceVariant = LightGray,
    onSurfaceVariant = DeepNavy,
    // ROL PARA NUESTRAS TARJETAS PERSONALIZADAS: EL SKYBLUE SUTIL
        // SurfaceContainer: Tarjetas en Freemode, Ranking y ProfileScreen;
        //  - También Fondo Cuadro Titulo Mapscreen y BootomAppBar MapScreen;
        //  - También Fondo en Diamante, Fondo LetterBank, QuestionImage, Card Score todos en GameScreen;
        //  - También la card grande de funfactlibraryscreen
    surfaceContainer = SkyBlue.copy(alpha = 0.08f),
    surfaceContainerHigh = SkyBlue.copy(alpha = 0.12f), // SkyBlue un poco más notorio
    error = Color(0xFFB00020),
    onError = PureWhite,
    outline = SkyBlue.copy(alpha = 0.5f)
)

// Paleta para el Tema OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = DeepNavy,
    primaryContainer = SkyBlue.copy(alpha = 0.2f),
    onPrimaryContainer = PureWhite,
    secondary = CyanAccent,
    onSecondary = DeepNavy,
    secondaryContainer = CyanAccent.copy(alpha = 0.2f),
    onSecondaryContainer = DeepNavy,
    tertiary = GoldAccent,
    onTertiary = DeepNavy,
    tertiaryContainer = GoldAccent.copy(alpha = 0.8f),
    onTertiaryContainer = DeepNavy,
    background = DeepNavy,
    onBackground = PureWhite,
    surface = Color(35, 65, 80), // DeepNavy más claro para Cards principales
    onSurface = PureWhite,
    surfaceVariant = Color(45, 75, 90), // Aún más claro para elementos diferentes
    onSurfaceVariant = Color(200, 220, 230), // Gris claro
    surfaceContainer = Color(28, 55, 65), // Entre DeepNavy y surface
    surfaceContainerHigh = Color(40, 70, 85), // Entre surface y surfaceVariant
    error = Color(0xFFCF6679),
    onError = DeepNavy,
    outline = SkyBlue.copy(alpha = 0.5f)
)

@Composable
fun PlayQuizGamesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}