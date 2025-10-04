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
 * Un Composable que dibuja una imagen de fondo configurable. Control 1-SB
 *
 * @param backgroundUrl La URL de la imagen a cargar.
 * @param imageAlpha La opacidad de la imagen de fondo (de 0.0f a 1.0f).
 * @param scrimAlpha La opacidad del "velo" de color que se superpone a la imagen (de 0.0f a 1.0f).
 *                   Un valor de 0.0f significa que no hay velo.
 * @param content El contenido de la pantalla que se mostrará sobre el fondo.
 */
@Composable
fun ScreenBackground(
    backgroundUrl: String,
    imageAlpha: Float = 0.7f, // Valor + grande, Imagen mas clara
    scrimAlpha: Float = 0.7f, // Valor * grande, Velo mas opaco
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // CAPA 1: La imagen de fondo, ahora con opacidad configurable
        if (backgroundUrl.isNotBlank()) {
            AsyncImage(
                model = backgroundUrl,
                contentDescription = "Background image",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(imageAlpha), // <-- USA EL PARÁMETRO
                contentScale = ContentScale.Crop
            )
        }

        // CAPA 2: El "Velo", ahora con opacidad configurable
        if (scrimAlpha > 0.0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = scrimAlpha)) // <-- USA EL PARÁMETRO
            )
        }

        // CAPA 3: El contenido principal de la pantalla
        content()
    }
}