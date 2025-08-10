package com.akrubastudios.playquizgames.ui.screens.game

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel // Importante añadir esta línea

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel() // Hilt provee el ViewModel aquí
) {
    // Mantenemos el texto simple por ahora.
    Text(text = "¡Pantalla de Juego Conectada!")
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    // La previsualización no necesita el ViewModel por ahora
    GameScreen()
}