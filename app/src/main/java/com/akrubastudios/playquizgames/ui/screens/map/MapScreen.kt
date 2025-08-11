package com.akrubastudios.playquizgames.ui.screens.map

import android.R.attr.enabled
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.domain.Country

@Composable
fun MapScreen(
    navController: NavController,
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
            modifier = Modifier.fillMaxSize()
            .clickable(enabled = !uiState.isLoading) {
            // Navega a la pantalla de Brasil al tocar el mapa.
            // Lógica simple por ahora.
            val brazilId = "br"
            navController.navigate(
                Routes.COUNTRY_SCREEN.replace("{countryId}", brazilId)
            )
        }
        ) {
            val imagePath = "file:///android_asset/world_globe.svg"
            Image(
                painter = rememberAsyncImagePainter(model = imagePath, imageLoader = imageLoader),
                contentDescription = "Mapa del Mundo",
                modifier = Modifier.fillMaxSize()
            )

            // Iteramos sobre la lista de países que cargó el ViewModel
            uiState.countries.forEach { country ->
                // Lógica simple para posicionar el botón de Brasil (mejorará en el futuro)
                if (country.countryId == "br") {
                    CountryButton(
                        country = country,
                        isConquered = uiState.conqueredCountryIds.contains(country.countryId),
                        onClick = {
                            navController.navigate(
                                Routes.COUNTRY_SCREEN.replace("{countryId}", country.countryId)
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.Center) // Centramos en el mapa
                            .offset(x = (-50).dp, y = 100.dp) // Ajustamos la posición para Sudamérica
                    )
                }
            }
        }
    }
}

@Composable
fun CountryButton(
    country: Country,
    isConquered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isConquered) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primary // Dorado si está conquistado
        )
    ) {
        Text(text = country.name["es"] ?: "")
    }
}