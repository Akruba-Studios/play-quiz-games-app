package com.akrubastudios.playquizgames.ui.screens.country

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CountryScreen(
    viewModel: CountryViewModel = hiltViewModel(),
    onPlayClick: (categoryId: String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = uiState.countryName, style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(32.dp))

            // Barra de progreso real
            Text(text = "Progreso de Conquista: ${uiState.currentPc} / ${uiState.pcRequired} PC")
            LinearProgressIndicator(
                progress = { (uiState.currentPc.toFloat() / uiState.pcRequired.toFloat()) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))
            uiState.availableLevels.forEach { (category, nextLevelId) ->
                Button(
                    onClick = {
                        onPlayClick(category.categoryId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nextLevelId != null // El botón se deshabilita si no hay más niveles
                ) {
                    val buttonText = if (nextLevelId != null) {
                        category.name["es"] ?: "Categoría"
                    } else {
                        "${category.name["es"]} (¡Completado!)"
                    }
                    Text(buttonText)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al Mapa")
            }
        }
    }
}