package com.akrubastudios.playquizgames.ui.screens.game

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Esta es la función principal que dibujará nuestra pantalla.
// La anotación @Composable le dice a Kotlin que esta función describe una pieza de UI.
@Composable
fun GameScreen() {
    // Por ahora, solo mostraremos un texto simple para verificar que funciona.
    Text(text = "¡Pantalla de Juego!")
}

// Esta función nos permite previsualizar nuestro diseño directamente en Android Studio.
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen()
}