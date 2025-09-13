package com.akrubastudios.playquizgames.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val GemIconDarkGold: ImageVector = ImageVector.Builder(
    defaultWidth = 72.dp,
    defaultHeight = 72.dp,
    viewportWidth = 72f,
    viewportHeight = 72f
).apply {
    // Todas las rutas del SVG convertidas con tonos dorados
    path(fill = SolidColor(Color(200, 140, 0))) { // Dorado base
        moveTo(26.1f, 49.8f)
        lineToRelative(9.6f, 21.8f)
        curveTo(35.7f, 71.7f, 35.6f, 71.7f, 35.6f, 71.6f)
        lineTo(16.9f, 49.8f)
        close()
    }

    path(fill = SolidColor(Color(200, 140, 0))) { // Dorado base
        moveTo(17.3f, 29.7f)
        horizontalLineTo(0.1f)
        curveTo(0.1f, 30.4f, 1.2f, 31.3f, 1.2f, 31.3f)
        lineToRelative(15.7f, 18.5f)
        lineToRelative(6.3f, -6.7f)
        close()
    }

    path(fill = SolidColor(Color(220, 160, 20))) { // Dorado claro
        moveTo(71.5f, 30.5f)
        curveTo(71f, 31f, 70.2f, 31f, 69.7f, 30.5f)
        curveTo(69.5f, 30.3f, 69.3f, 29.9f, 69.3f, 29.6f)
        horizontalLineTo(54.6f)
        lineToRelative(-9.2f, -10f)
        lineTo(55f, 9.5f)
        lineToRelative(16.3f, 19.1f)
        reflectiveCurveToRelative(0f, 0.1f, 0.1f, 0.1f)
        horizontalLineToRelative(0.1f)
        curveToRelative(0.5f, 0.6f, 0.5f, 1.4f, 0f, 1.8f)
        moveToRelative(-35.6f, -0.8f)
        lineToRelative(9.4f, -10f)
        lineToRelative(-9.4f, -10.2f)
        lineToRelative(-9.4f, 10.2f)
        close()
    }

    path(fill = SolidColor(Color(180, 120, 0))) { // Dorado medio
        moveTo(36.3f, 71.7f)
        curveTo(36.2f, 71.8f, 36.1f, 71.7f, 36.2f, 71.6f)
        lineToRelative(9.6f, -21.8f)
        horizontalLineTo(55f)
        close()
    }

    path(fill = SolidColor(Color(180, 120, 0))) { // Dorado medio
        moveTo(48.7f, 43.1f)
        lineToRelative(6.3f, 6.7f)
        lineToRelative(16.4f, -19.2f)
        reflectiveCurveToRelative(0.4f, -0.6f, 0.4f, -0.9f)
        horizontalLineTo(54.6f)
        close()
    }

    path(fill = SolidColor(Color(240, 180, 40))) { // Dorado brillante
        moveTo(26.5f, 19.7f)
        lineToRelative(-9.2f, 10f)
        horizontalLineToRelative(18.6f)
        close()
    }

    path(fill = SolidColor(Color(200, 160, 60))) { // Dorado suave
        moveTo(45.4f, 19.7f)
        lineToRelative(-9.5f, 10f)
        horizontalLineToRelative(18.7f)
        close()
    }

    path(fill = SolidColor(Color(160, 100, 0))) { // Dorado oscuro
        moveTo(26.5f, 19.7f)
        lineToRelative(-9.2f, 10f)
        horizontalLineTo(0.1f)
        reflectiveCurveToRelative(0.1f, -0.6f, 0.5f, -1f)
        lineToRelative(15.8f, -18.5f)
        curveToRelative(0.1f, -0.1f, 0.2f, -0.2f, 0.3f, -0.2f)
        curveToRelative(0.2f, -0.3f, 0.5f, -0.4f, 0.9f, -0.4f)
        curveToRelative(0.7f, 0f, 1.2f, 0.6f, 1.2f, 1.2f)
        curveToRelative(0f, 0.2f, -0.1f, 0.4f, -0.2f, 0.6f)
        close()
        moveTo(35.9f, 29.7f)
        lineToRelative(12.8f, 13.5f)
        lineToRelative(5.9f, -13.5f)
        close()
        moveTo(17.3f, 29.7f)
        lineToRelative(5.9f, 13.5f)
        lineTo(36f, 29.7f)
        close()
        moveTo(26.1f, 49.8f)
        lineToRelative(9.7f, 22f)
        quadToRelative(0.15f, 0.15f, 0.3f, 0f)
        lineToRelative(9.7f, -22f)
        close()
        moveTo(48.7f, 43.1f)
        lineToRelative(-2.9f, 6.7f)
        horizontalLineTo(55f)
        close()
    }

    path(fill = SolidColor(Color(180, 120, 0))) { // Dorado medio
        moveTo(45.4f, 19.7f)
        lineTo(35.9f, 9.5f)
        horizontalLineTo(55f)
        close()
        moveTo(35.9f, 9.5f)
        horizontalLineTo(18f)
        curveToRelative(-0.4f, 0f, -1.2f, 0f, -0.9f, 0.3f)
        lineToRelative(9.4f, 9.9f)
        close()
    }

    path(fill = SolidColor(Color(140, 80, 0))) { // Dorado muy oscuro
        moveTo(23.2f, 43.1f)
        lineToRelative(-6.3f, 6.7f)
        horizontalLineToRelative(9.2f)
        close()
    }

    path(fill = SolidColor(Color(200, 140, 0))) { // Dorado base
        moveTo(23.2f, 43.1f)
        lineToRelative(2.9f, 6.7f)
        horizontalLineToRelative(19.7f)
        lineToRelative(2.9f, -6.7f)
        lineToRelative(-12.8f, -13.4f)
        close()
    }

    path(fill = SolidColor(Color.White)) { // Mantiene el brillo blanco
        moveTo(52.7f, 29.9f)
        curveToRelative(2.7f, 0.7f, 4.8f, 2.8f, 5.5f, 5.5f)
        curveToRelative(0.1f, 0.3f, 0.4f, 0.3f, 0.5f, 0f)
        curveToRelative(0.7f, -2.7f, 2.8f, -4.8f, 5.5f, -5.5f)
        curveToRelative(0.3f, -0.1f, 0.3f, -0.4f, 0f, -0.5f)
        curveToRelative(-2.7f, -0.7f, -4.8f, -2.8f, -5.5f, -5.5f)
        curveToRelative(-0.1f, -0.3f, -0.4f, -0.3f, -0.5f, 0f)
        curveToRelative(-0.7f, 2.7f, -2.8f, 4.8f, -5.5f, 5.5f)
        curveToRelative(-0.3f, 0.1f, -0.3f, 0.4f, 0f, 0.5f)
        close()
    }

    path(fill = SolidColor(Color(240, 200, 80))) { // Dorado muy suave (para la estrella)
        moveTo(42.6f, 3.6f)
        curveToRelative(1.4f, 0.4f, 2.5f, 1.5f, 2.9f, 2.9f)
        curveToRelative(0f, 0.1f, 0.2f, 0.1f, 0.2f, 0f)
        curveToRelative(0.4f, -1.4f, 1.5f, -2.5f, 2.9f, -2.9f)
        curveToRelative(0.1f, 0f, 0.1f, -0.2f, 0f, -0.2f)
        curveToRelative(-1.4f, -0.4f, -2.5f, -1.5f, -2.9f, -2.9f)
        curveToRelative(0f, -0.1f, -0.2f, -0.1f, -0.2f, 0f)
        curveToRelative(-0.4f, 1.4f, -1.5f, 2.5f, -2.9f, 2.9f)
        curveToRelative(-0.1f, 0f, -0.1f, 0.2f, 0f, 0.2f)
        close()
    }
}.build()