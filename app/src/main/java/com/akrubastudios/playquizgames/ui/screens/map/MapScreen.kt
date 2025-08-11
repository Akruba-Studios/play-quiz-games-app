package com.akrubastudios.playquizgames.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.domain.Country

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold nos da la estructura de la pantalla principal
    Scaffold(
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.RANKING_SCREEN) },
                    icon = { Icon(Icons.Filled.Leaderboard, contentDescription = "Ranking") },
                    label = { Text("Ranking") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navegar a Perfil en el futuro */ },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding -> // El contenido principal debe usar este padding

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Usa el padding proporcionado por el Scaffold
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mapa del Conocimiento",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                MapContent(
                    uiState = uiState,
                    onCountryClick = { countryId ->
                        navController.navigate(
                            Routes.COUNTRY_SCREEN.replace("{countryId}", countryId)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MapContent(
    uiState: MapState,
    onCountryClick: (String) -> Unit
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .build()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(model = "file:///android_asset/world_globe.svg", imageLoader = imageLoader),
            contentDescription = "Mapa del Mundo",
            modifier = Modifier.fillMaxSize()
        )

        uiState.countries.forEach { country ->
            if (country.countryId == "br") {
                CountryButton(
                    country = country,
                    isConquered = uiState.conqueredCountryIds.contains(country.countryId),
                    onClick = { onCountryClick(country.countryId) },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-50).dp, y = 100.dp)
                )
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