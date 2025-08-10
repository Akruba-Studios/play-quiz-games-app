package com.akrubastudios.playquizgames.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onPlayClick: () -> Unit // Una función para notificar que se quiere jugar
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Play Quiz Games", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(64.dp))
        Button(
            onClick = onPlayClick,
            modifier = Modifier.width(200.dp).height(50.dp)
        ) {
            Text(text = "Jugar")
        }
    }
}