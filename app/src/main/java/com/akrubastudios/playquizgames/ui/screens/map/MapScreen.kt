package com.akrubastudios.playquizgames.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder

@Composable
fun MapScreen(
    // 1. Recibimos el ViewModel que Hilt nos proveerá
    viewModel: MapViewModel = hiltViewModel()
) {
    // 2. Nos suscribimos al estado de la UI del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
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

        // 3. Mostramos un texto de prueba que depende del estado
        if (uiState.isLoading) {
            Text(text = "Cargando países...")
        } else {
            Text(text = "Países cargados: ${uiState.countries.size}")
        }

        // El Box y la Imagen del mapa se quedan igual por ahora
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val imagePath = "file:///android_asset/world_globe.svg"
            Image(
                painter = rememberAsyncImagePainter(model = imagePath, imageLoader = imageLoader),
                contentDescription = "Mapa del Mundo",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}