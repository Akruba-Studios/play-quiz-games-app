package com.akrubastudios.playquizgames.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrubastudios.playquizgames.ui.theme.CyanAccent
import com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent
import com.akrubastudios.playquizgames.ui.theme.DeepNavy
import com.akrubastudios.playquizgames.ui.theme.LightGray
import com.akrubastudios.playquizgames.ui.theme.SkyBlue

// Función helper para obtener el color del botón
@Composable
fun getButtonTextColor(): Color {
    return if (isSystemInDarkTheme()) DarkGoldAccent else CyanAccent
}

// Función helper para obtener colores de botones internos en el Dialog de Expedition
@Composable
fun getInternalButtonColors(): Pair<Color, Color> { // Pair<backgroundColor, textColor>
    return if (isSystemInDarkTheme()) {
        Pair(DarkGoldAccent, Color.White) // Para modo noche
    } else {
        Pair(LightGray, SkyBlue) // Para modo día
    }
}

// VERSIÓN SIMPLE
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String
) {
    AppAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { DialogTitle(text = title) },
        text = { DialogText(text = text) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                DialogButtonText(text = confirmButtonText)
            }
        }
    )
}

// VERSIÓN COMPLEJA ESTÁNDAR
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
        backgroundColor = DeepNavy
    } else {
        backgroundColor = DarkGoldAccent.copy(alpha = 0.90f)
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

// VERSIÓN ESPECIAL PARA EXPEDICIÓN CON BOTONES PERSONALIZADOS
@Composable
fun AppExpeditionAlertDialog(
    onDismissRequest: () -> Unit,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    expeditionButtons: @Composable (backgroundColor: Color, textColor: Color) -> Unit,
    confirmButton: @Composable () -> Unit
) {
    val backgroundColor: Color
    val contentColor = Color.White
    val (internalButtonBg, internalButtonText) = getInternalButtonColors()

    if (isSystemInDarkTheme()) {
        backgroundColor = DeepNavy
    } else {
        backgroundColor = DarkGoldAccent.copy(alpha = 0.90f)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        titleContentColor = contentColor,
        textContentColor = contentColor,
        title = title,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                text?.invoke()
                Spacer(modifier = Modifier.height(16.dp))
                expeditionButtons(internalButtonBg, internalButtonText)
            }
        },
        confirmButton = confirmButton
    )
}

// === COMPONENTES DE TEXTO REUTILIZABLES ===

@Composable
fun DialogTitle(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DialogText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DialogButtonText(text: String) {
    val buttonColor = getButtonTextColor()
    Text(
        text = text,
        color = buttonColor,
        fontWeight = FontWeight.Bold
    )
}