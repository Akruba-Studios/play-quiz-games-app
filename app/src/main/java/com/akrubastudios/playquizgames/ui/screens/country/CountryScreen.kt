package com.akrubastudios.playquizgames.ui.screens.country

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CountryScreen(
    countryName: String, // Recibirá el nombre del país para mostrarlo
    onPlayClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = countryName, style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(32.dp))
        // Aquí iría la barra de progreso de conquista
        Text(text = "Progreso de Conquista: 0 / 50000 PC")
        Spacer(modifier = Modifier.height(64.dp))
        Button(onClick = onPlayClick, modifier = Modifier.fillMaxWidth()) {
            Text("Jugar Nivel de Logos") // Por ahora, un solo nivel
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al Mapa")
        }
    }
}