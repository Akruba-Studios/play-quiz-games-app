package com.akrubastudios.playquizgames.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Un Composable que dibuja una imagen de fondo que ocupa toda la pantalla.
 * El contenido principal se dibuja encima.
 *
 * @param backgroundUrl La URL de la imagen a cargar.
 * @param content El contenido de la pantalla que se mostrará sobre el fondo.
 */
@Composable
fun ScreenBackground(
    backgroundUrl: String,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // CAPA 1: La imagen de fondo. La hacemos un poco más visible (ej. 40% opaca)
        if (backgroundUrl.isNotBlank()) {
            AsyncImage(
                model = backgroundUrl,
                contentDescription = "Background image",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.4f), // valor mas grande, mas claro la imagen
                contentScale = ContentScale.Crop
            )
        }

        // CAPA 2: El "Velo". Una capa del color de fondo principal de la app,
        // pero semitransparente. Esto suaviza la imagen de abajo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)) // valor mas grande, mas opaco el velo
        )

        // CAPA 3: El contenido principal de la pantalla
        content()
    }
}