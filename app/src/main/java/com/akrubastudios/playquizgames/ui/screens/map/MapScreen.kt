package com.akrubastudios.playquizgames.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.akrubastudios.playquizgames.R
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import androidx.compose.ui.platform.LocalContext

@Composable
fun MapScreen() {
    // Obtenemos el contexto actual, que Coil necesita
    val context = LocalContext.current

    // Creamos un 'ImageLoader' especial que sabe cómo decodificar SVGs
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mapa del Conocimiento")

        // Construimos la ruta al archivo dentro de la carpeta 'assets'
        val imagePath = "file:///android_asset/world_globe.svg" // <-- REEMPLAZA con el nombre de tu SVG

        // Usamos el 'rememberAsyncImagePainter' con nuestro ImageLoader personalizado
        Image(
            painter = rememberAsyncImagePainter(model = imagePath, imageLoader = imageLoader),
            contentDescription = "Mapa del Mundo",
            modifier = Modifier.fillMaxSize() // Hacemos que ocupe el espacio disponible
        )
    }
}