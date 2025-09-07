package com.akrubastudios.playquizgames.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// 1. Definimos el esquema de colores para el tema OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,          // El azul del logo será el color principal
    secondary = CyanAccent,     // El cian brillante como color secundario
    tertiary = GoldAccent,      // El dorado para acentos especiales
    background = DeepNavy,      // El azul marino oscuro como fondo
    surface = DeepNavy,         // Las "superficies" como tarjetas tendrán el mismo fondo
    onPrimary = DeepNavy,       // El texto sobre un botón primario será oscuro para contraste
    onSecondary = DeepNavy,     // El texto sobre un botón secundario será oscuro
    onTertiary = DeepNavy,      // El texto sobre un botón terciario será oscuro
    onBackground = PureWhite,   // El texto general sobre el fondo oscuro será blanco
    onSurface = PureWhite       // El texto en las tarjetas será blanco
)

// 2. Definimos el esquema de colores para el tema CLARO
private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,          // Mantenemos el azul del logo como color principal
    secondary = CyanAccent,
    tertiary = GoldAccent,
    background = LightGray,     // Usamos el gris claro para el fondo para que no sea un blanco puro
    surface = PureWhite,        // Las tarjetas serán blancas para destacar sobre el fondo gris
    onPrimary = PureWhite,      // El texto sobre un botón primario será blanco
    onSecondary = DeepNavy,
    onTertiary = DeepNavy,
    onBackground = DeepNavy,    // El texto general sobre el fondo claro será el azul marino oscuro
    onSurface = DeepNavy        // El texto en las tarjetas será el azul marino oscuro
)

@Composable
fun PlayQuizGamesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Mantenemos en false para usar siempre nuestro tema
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
        typography = Typography, // La tipografía que definimos en Type.kt se aplica aquí
        content = content
    )
}