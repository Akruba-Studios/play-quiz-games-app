package com.akrubastudios.playquizgames.performance

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracker de FPS real basado en tiempo entre frames reales
 */
class RealFpsTracker {

    private val _currentFPS = MutableStateFlow(0f)
    val currentFPS: StateFlow<Float> = _currentFPS.asStateFlow()

    private val _averageFPS = MutableStateFlow(0f)
    val averageFPS: StateFlow<Float> = _averageFPS.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // Variables para cálculo de FPS
    private var lastFrameTime = System.nanoTime()
    private val fpsHistory = mutableListOf<Float>()
    private var frameCount = 0

    // Configuración
    private val maxHistorySize = 60 // Últimos 60 frames para promedio
    private val updateFrequency = 5 // Actualizar cada 5 frames

    /**
     * Marca el inicio de un nuevo frame
     * Debe llamarse al comienzo de cada iteración del loop de animación
     */
    fun recordFrameStart() {
        val currentTime = System.nanoTime()

        if (_isTracking.value && lastFrameTime != 0L) {
            val frameTimeNanos = currentTime - lastFrameTime

            // Evitar divisiones por cero y valores extremos
            if (frameTimeNanos > 0) {
                val instantFPS = 1_000_000_000f / frameTimeNanos.toFloat()

                // Filtrar valores extremos (menos de 1 FPS o más de 200 FPS)
                if (instantFPS in 1f..200f) {
                    _currentFPS.value = instantFPS

                    // Agregar al historial
                    fpsHistory.add(instantFPS)
                    if (fpsHistory.size > maxHistorySize) {
                        fpsHistory.removeAt(0)
                    }

                    frameCount++

                    // Actualizar promedio cada ciertos frames
                    if (frameCount % updateFrequency == 0 && fpsHistory.isNotEmpty()) {
                        _averageFPS.value = fpsHistory.average().toFloat()
                    }
                }
            }
        }

        lastFrameTime = currentTime
    }

    /**
     * Inicia el tracking de FPS
     */
    fun startTracking() {
        _isTracking.value = true
        lastFrameTime = System.nanoTime()
        fpsHistory.clear()
        frameCount = 0
    }

    /**
     * Detiene el tracking de FPS
     */
    fun stopTracking() {
        _isTracking.value = false
        lastFrameTime = 0L
    }

    /**
     * Obtiene estadísticas detalladas
     */
    fun getStats(): FpsStats {
        return FpsStats(
            currentFPS = _currentFPS.value,
            averageFPS = _averageFPS.value,
            minFPS = if (fpsHistory.isNotEmpty()) fpsHistory.minOrNull() ?: 0f else 0f,
            maxFPS = if (fpsHistory.isNotEmpty()) fpsHistory.maxOrNull() ?: 0f else 0f,
            totalFrames = frameCount,
            historySize = fpsHistory.size
        )
    }

    data class FpsStats(
        val currentFPS: Float,
        val averageFPS: Float,
        val minFPS: Float,
        val maxFPS: Float,
        val totalFrames: Int,
        val historySize: Int
    )
}