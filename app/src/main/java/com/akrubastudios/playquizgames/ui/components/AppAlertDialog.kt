package com.akrubastudios.playquizgames.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String
) {
    // 1. Definimos los colores personalizados.
    val backgroundColor: Color
    val contentColor = Color.White // El color del texto siempre es blanco.

    // 2. Usamos isSystemInDarkTheme() para elegir el color de fondo.
    if (isSystemInDarkTheme()) {
        // MODO NOCHE: Fondo Verde Oscuro (un verde similar al de WhatsApp oscuro).
        backgroundColor = Color(0xFF075E54)
    } else {
        // MODO DÍA: Fondo Rojo.
        backgroundColor = Color.Red
    }

    // 3. Aplicamos los colores directamente al AlertDialog.
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        titleContentColor = contentColor,
        textContentColor = contentColor,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(confirmButtonText, color = contentColor)
            }
        }
    )
}