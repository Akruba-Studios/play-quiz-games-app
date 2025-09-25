package com.akrubastudios.playquizgames.performance

import android.os.SystemClock
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max
import kotlin.math.min

/**
 * Monitor de rendimiento en tiempo real para tracking de FPS y métricas de performance
 */
class PerformanceMonitor {

    private data class FrameData(
        val timestamp: Long,
        val fps: Float,
        val frameTimeMs: Long
    )

    // Cola thread-safe para almacenar datos de frames
    private val frameDataQueue = ConcurrentLinkedQueue<FrameData>()

    // Configuración
    private val maxHistorySize = 200 // Mantener últimos 200 frames
    private val cleanupInterval = 100 // Limpiar cada 100 frames
    private var frameCount = 0

    // Estadísticas en tiempo real
    @Volatile
    private var currentFPS = 0f
    @Volatile
    private var averageFPS = 0f
    @Volatile
    private var minFPS = Float.MAX_VALUE
    @Volatile
    private var maxFPS = 0f

    // Para cálculo de FPS
    private var lastFrameTime = SystemClock.elapsedRealtime()
    private var frameTimeAccumulator = 0L
    private var fpsCalculationFrames = 0

    /**
     * Registra un nuevo frame y calcula métricas
     * Esta función debe ser llamada desde el hilo de renderizado
     */
    fun recordFrame(fps: Float) {
        val currentTime = SystemClock.elapsedRealtime()
        val frameTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        // Filtrar valores extremos o inválidos
        if (fps <= 0 || fps > 200) return

        // Actualizar estadísticas básicas
        currentFPS = fps
        minFPS = min(minFPS, fps)
        maxFPS = max(maxFPS, fps)

        // Acumular para cálculo de promedio
        frameTimeAccumulator += frameTime
        fpsCalculationFrames++

        // Calcular promedio cada 30 frames
        if (fpsCalculationFrames >= 30) {
            averageFPS = 1000f / (frameTimeAccumulator.toFloat() / fpsCalculationFrames)
            frameTimeAccumulator = 0L
            fpsCalculationFrames = 0
        }

        // Almacenar datos del frame
        val frameData = FrameData(currentTime, fps, frameTime)
        frameDataQueue.offer(frameData)

        frameCount++

        // Limpiar datos antiguos periódicamente
        if (frameCount % cleanupInterval == 0) {
            cleanupOldData()
        }
    }

    /**
     * Alternativa simple para registrar solo el tiempo entre frames
     */
    fun recordFrameTime() {
        val currentTime = SystemClock.elapsedRealtime()
        val frameTime = currentTime - lastFrameTime

        if (frameTime > 0) {
            val fps = 1000f / frameTime.toFloat()
            recordFrame(fps)
        }

        lastFrameTime = currentTime
    }

    /**
     * Obtiene el FPS actual (último frame)
     */
    fun getCurrentFPS(): Float = currentFPS

    /**
     * Obtiene el FPS promedio reciente
     */
    fun getAverageFPS(): Float = if (averageFPS > 0) averageFPS else currentFPS

    /**
     * Obtiene el FPS mínimo registrado
     */
    fun getMinFPS(): Float = if (minFPS == Float.MAX_VALUE) 0f else minFPS

    /**
     * Obtiene el FPS máximo registrado
     */
    fun getMaxFPS(): Float = maxFPS

    /**
     * Calcula el FPS promedio de los últimos N frames
     */
    fun getRecentAverageFPS(frameCount: Int = 60): Float {
        val recentFrames = getRecentFrames(frameCount)
        if (recentFrames.isEmpty()) return 0f

        return recentFrames.map { it.fps }.average().toFloat()
    }

    /**
     * Obtiene la estabilidad del framerate (desviación estándar)
     */
    fun getFramerateStability(frameCount: Int = 100): Float {
        val recentFrames = getRecentFrames(frameCount)
        if (recentFrames.size < 10) return 0f

        val fpsList = recentFrames.map { it.fps }
        val average = fpsList.average()
        val variance = fpsList.map { (it - average) * (it - average) }.average()

        return kotlin.math.sqrt(variance).toFloat()
    }

    /**
     * Detecta si hay problemas de rendimiento
     */
    fun detectPerformanceIssues(): PerformanceIssues {
        val recentFrames = getRecentFrames(120)
        if (recentFrames.size < 60) {
            return PerformanceIssues()
        }

        val fpsList = recentFrames.map { it.fps }
        val avgFPS = fpsList.average().toFloat()
        val stability = getFramerateStability()

        // Detectar diferentes tipos de problemas
        val hasLowFPS = avgFPS < 15f
        val hasUnstableFPS = stability > 8f
        val hasFrameSpikes = fpsList.any { it < avgFPS * 0.5f }
        val hasConsistentDrops = fpsList.takeLast(30).count { it < 12f } > 15

        return PerformanceIssues(
            hasLowFPS = hasLowFPS,
            hasUnstableFPS = hasUnstableFPS,
            hasFrameSpikes = hasFrameSpikes,
            hasConsistentDrops = hasConsistentDrops,
            averageFPS = avgFPS,
            stabilityScore = stability,
            severity = when {
                hasConsistentDrops || avgFPS < 10f -> PerformanceSeverity.CRITICAL
                hasLowFPS || stability > 12f -> PerformanceSeverity.HIGH
                hasUnstableFPS || hasFrameSpikes -> PerformanceSeverity.MEDIUM
                else -> PerformanceSeverity.NONE
            }
        )
    }

    /**
     * Obtiene métricas detalladas de rendimiento
     */
    fun getDetailedMetrics(): DetailedPerformanceMetrics {
        val recentFrames = getRecentFrames(200)

        return DetailedPerformanceMetrics(
            totalFramesRecorded = frameCount,
            currentFPS = currentFPS,
            averageFPS = getAverageFPS(),
            minFPS = getMinFPS(),
            maxFPS = getMaxFPS(),
            recentAverageFPS = getRecentAverageFPS(),
            stabilityScore = getFramerateStability(),
            performanceIssues = detectPerformanceIssues(),
            frameHistory = recentFrames.map { it.fps },
            monitoringDurationSeconds = if (recentFrames.isNotEmpty()) {
                (recentFrames.last().timestamp - recentFrames.first().timestamp) / 1000
            } else 0
        )
    }

    /**
     * Reinicia todas las estadísticas
     */
    fun reset() {
        frameDataQueue.clear()
        frameCount = 0
        currentFPS = 0f
        averageFPS = 0f
        minFPS = Float.MAX_VALUE
        maxFPS = 0f
        frameTimeAccumulator = 0L
        fpsCalculationFrames = 0
        lastFrameTime = SystemClock.elapsedRealtime()
    }

    /**
     * Obtiene los frames más recientes
     */
    private fun getRecentFrames(count: Int): List<FrameData> {
        return frameDataQueue.toList().takeLast(count)
    }

    /**
     * Limpia datos antiguos para mantener el uso de memoria bajo control
     */
    private fun cleanupOldData() {
        while (frameDataQueue.size > maxHistorySize) {
            frameDataQueue.poll()
        }

        // Resetear estadísticas min/max cada cierto tiempo para que reflejen el estado actual
        if (frameCount % 1000 == 0) {
            minFPS = Float.MAX_VALUE
            maxFPS = 0f
        }
    }

    /**
     * Data classes para estructurar los resultados
     */
    data class PerformanceIssues(
        val hasLowFPS: Boolean = false,
        val hasUnstableFPS: Boolean = false,
        val hasFrameSpikes: Boolean = false,
        val hasConsistentDrops: Boolean = false,
        val averageFPS: Float = 0f,
        val stabilityScore: Float = 0f,
        val severity: PerformanceSeverity = PerformanceSeverity.NONE
    ) {
        fun hasAnyIssues(): Boolean = hasLowFPS || hasUnstableFPS || hasFrameSpikes || hasConsistentDrops

        fun getDescription(): String {
            return when (severity) {
                PerformanceSeverity.CRITICAL -> "Rendimiento crítico: ${averageFPS.toInt()} FPS"
                PerformanceSeverity.HIGH -> "Rendimiento bajo: ${averageFPS.toInt()} FPS"
                PerformanceSeverity.MEDIUM -> "Rendimiento inestable: ${stabilityScore.toInt()} variación"
                PerformanceSeverity.NONE -> "Rendimiento estable: ${averageFPS.toInt()} FPS"
            }
        }
    }

    enum class PerformanceSeverity {
        NONE,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    data class DetailedPerformanceMetrics(
        val totalFramesRecorded: Int,
        val currentFPS: Float,
        val averageFPS: Float,
        val minFPS: Float,
        val maxFPS: Float,
        val recentAverageFPS: Float,
        val stabilityScore: Float,
        val performanceIssues: PerformanceIssues,
        val frameHistory: List<Float>,
        val monitoringDurationSeconds: Long
    ) {
        override fun toString(): String {
            return """
                Performance Metrics:
                - Current FPS: ${currentFPS.toInt()}
                - Average FPS: ${averageFPS.toInt()} 
                - Range: ${minFPS.toInt()}-${maxFPS.toInt()}
                - Recent Average: ${recentAverageFPS.toInt()}
                - Stability: ${stabilityScore.toInt()}
                - Issues: ${performanceIssues.getDescription()}
                - Frames Recorded: $totalFramesRecorded
                - Monitoring Time: ${monitoringDurationSeconds}s
            """.trimIndent()
        }
    }
}