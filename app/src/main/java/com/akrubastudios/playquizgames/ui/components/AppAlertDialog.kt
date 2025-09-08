package com.akrubastudios.playquizgames.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Función helper para obtener el color del botón
@Composable
fun getButtonTextColor(): Color {
    return if (isSystemInDarkTheme()) Color.Yellow else Color.Black
}

// VERSIÓN SIMPLE
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String
) {
    val buttonColor = getButtonTextColor()

    AppAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(confirmButtonText, color = buttonColor)
            }
        }
    )
}

// VERSIÓN COMPLEJA
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    val backgroundColor: Color
    val contentColor = Color.White

    if (isSystemInDarkTheme()) {
        backgroundColor = Color(0xFF075E54)
    } else {
        backgroundColor = Color.Red
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        titleContentColor = contentColor,
        textContentColor = contentColor,
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}