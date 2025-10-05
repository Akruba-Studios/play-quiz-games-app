package com.akrubastudios.playquizgames.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle

/**
 * Un Composable que dibuja un Text con un borde o contorno para mejorar la legibilidad.
 */
@Composable
fun TextWithBorder(
    text: String,
    style: TextStyle,
    borderColor: Color,
    borderWidth: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        // CAPA 1: El borde (texto dibujado solo con contorno)
        Text(
            text = text,
            style = style.copy(
                drawStyle = Stroke(
                    width = borderWidth,
                    join = StrokeJoin.Round
                )
            ),
            color = borderColor
        )
        // CAPA 2: El relleno (texto normal encima)
        Text(
            text = text,
            style = style,
        )
    }
}