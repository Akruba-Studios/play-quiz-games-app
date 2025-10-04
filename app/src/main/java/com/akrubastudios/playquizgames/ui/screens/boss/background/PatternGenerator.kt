package com.akrubastudios.playquizgames.ui.screens.boss.background

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * Genera patrones geométricos procedurales basados en el tipo de arquetipo
 */
object PatternGenerator {

    /**
     * Dibuja el patrón cultural en el canvas
     */
    fun drawPattern(
        drawScope: DrawScope,
        patternType: PatternType,
        color: Color,
        countryId: String,
        phase: Int
    ) {
        val seed = countryId.hashCode()
        val density = when(phase) {
            1 -> 1.0f
            2 -> 1.2f
            3 -> 1.5f
            else -> 1.0f
        }

        with(drawScope) {
            when(patternType) {
                PatternType.ZIGZAG -> drawZigzagPattern(color, seed, density)
                PatternType.WAVES -> drawWavesPattern(color, seed, density)
                PatternType.GRID -> drawGridPattern(color, seed, density)
                PatternType.RADIAL -> drawRadialPattern(color, seed, density)
                PatternType.DIAGONAL -> drawDiagonalPattern(color, seed, density)
                PatternType.MOSAIC -> drawMosaicPattern(color, seed, density)
            }
        }
    }

    /**
     * Patrón de zigzag (escalones andinos/mesoamericanos)
     */
    private fun DrawScope.drawZigzagPattern(color: Color, seed: Int, density: Float) {
        val stepSize = 60f / density
        val amplitude = 40f
        val path = Path()

        var y = -amplitude
        while (y < size.height + amplitude) {
            path.reset()
            path.moveTo(0f, y)

            var x = 0f
            var goingUp = (seed + y.toInt()) % 2 == 0

            while (x < size.width) {
                if (goingUp) {
                    path.lineTo(x + stepSize, y - amplitude)
                    goingUp = false
                } else {
                    path.lineTo(x + stepSize, y + amplitude)
                    goingUp = true
                }
                x += stepSize
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2f)
            )

            y += stepSize * 2
        }
    }

    /**
     * Patrón de ondas (costero, atlántico)
     */
    private fun DrawScope.drawWavesPattern(color: Color, seed: Int, density: Float) {
        val wavelength = 80f / density
        val amplitude = 30f
        val path = Path()

        var y = -amplitude * 2
        var waveOffset = 0

        while (y < size.height + amplitude * 2) {
            path.reset()
            path.moveTo(0f, y)

            for (x in 0..size.width.toInt() step 5) {
                val wave = sin((x + waveOffset * 20) / wavelength * Math.PI * 2).toFloat()
                path.lineTo(x.toFloat(), y + wave * amplitude)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2f)
            )

            y += wavelength
            waveOffset = (waveOffset + seed) % 100
        }
    }

    /**
     * Patrón de cuadrícula (urbano, norteamericano)
     */
    private fun DrawScope.drawGridPattern(color: Color, seed: Int, density: Float) {
        val gridSize = 100f / density
        val random = java.util.Random(seed.toLong())

        // Líneas verticales
        var x = 0f
        while (x < size.width) {
            val offset = random.nextFloat() * 20f - 10f
            drawLine(
                color = color,
                start = Offset(x + offset, 0f),
                end = Offset(x + offset, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        // Líneas horizontales
        var y = 0f
        while (y < size.height) {
            val offset = random.nextFloat() * 20f - 10f
            drawLine(
                color = color,
                start = Offset(0f, y + offset),
                end = Offset(size.width, y + offset),
                strokeWidth = 1f
            )
            y += gridSize
        }
    }

    /**
     * Patrón radial (mediterráneo, mandala)
     */
    private fun DrawScope.drawRadialPattern(color: Color, seed: Int, density: Float) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = maxOf(size.width, size.height) * 0.7f
        val radiusStep = 80f / density
        val segments = 12 + (seed % 8)

        var radius = radiusStep
        while (radius < maxRadius) {
            val path = Path()

            for (i in 0..segments) {
                val angle = (i.toFloat() / segments) * 2 * Math.PI
                val x = centerX + cos(angle).toFloat() * radius
                val y = centerY + sin(angle).toFloat() * radius

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            path.close()

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 1.5f)
            )

            radius += radiusStep
        }
    }

    /**
     * Patrón diagonal (nórdico, cruz)
     */
    private fun DrawScope.drawDiagonalPattern(color: Color, seed: Int, density: Float) {
        val spacing = 80f / density
        val random = java.util.Random(seed.toLong())

        // Diagonales hacia abajo-derecha
        var start = -size.height
        while (start < size.width) {
            val offset = random.nextFloat() * 10f - 5f
            drawLine(
                color = color,
                start = Offset(start + offset, 0f),
                end = Offset(start + size.height + offset, size.height),
                strokeWidth = 1.5f
            )
            start += spacing
        }

        // Diagonales hacia abajo-izquierda
        start = 0f
        while (start < size.width + size.height) {
            val offset = random.nextFloat() * 10f - 5f
            drawLine(
                color = color,
                start = Offset(start + offset, 0f),
                end = Offset(start - size.height + offset, size.height),
                strokeWidth = 1.5f
            )
            start += spacing
        }
    }

    /**
     * Patrón de mosaico irregular (amazónico, orgánico)
     */
    private fun DrawScope.drawMosaicPattern(color: Color, seed: Int, density: Float) {
        val random = java.util.Random(seed.toLong())
        val cellSize = 60f / density
        val cols = (size.width / cellSize).toInt()
        val rows = (size.height / cellSize).toInt()

        for (row in 0..rows) {
            for (col in 0..cols) {
                val baseX = col * cellSize
                val baseY = row * cellSize

                // Variación aleatoria pero consistente
                val offsetX = random.nextFloat() * cellSize * 0.3f
                val offsetY = random.nextFloat() * cellSize * 0.3f

                val path = Path()

                // Forma irregular de 4-6 lados
                val sides = 4 + random.nextInt(3)
                for (i in 0..sides) {
                    val angle = (i.toFloat() / sides) * 2 * Math.PI
                    val radiusVariation = 0.7f + random.nextFloat() * 0.3f
                    val radius = cellSize * 0.4f * radiusVariation

                    val x = baseX + offsetX + cos(angle).toFloat() * radius
                    val y = baseY + offsetY + sin(angle).toFloat() * radius

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                path.close()

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 1f)
                )
            }
        }
    }
}