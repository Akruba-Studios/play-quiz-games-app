package com.akrubastudios.playquizgames.performance

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

/**
 * Gestor central de configuración oceánica con detección automática
 * y ajuste dinámico de rendimiento
 */
class OceanConfigManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "OceanConfigManager"
        private const val PREFS_NAME = "ocean_performance_config"
        private const val KEY_CURRENT_TIER = "current_tier"
        private const val KEY_USER_OVERRIDE = "user_override_tier"
        private const val KEY_LAST_DETECTION_TIME = "last_detection_time"
        private const val KEY_TOTAL_RUNTIME_SECONDS = "total_runtime_seconds"
        private const val KEY_AVERAGE_FPS = "average_fps"
        private const val KEY_PERFORMANCE_HISTORY = "performance_history"
        private const val KEY_AUTO_ADJUST_ENABLED = "auto_adjust_enabled"

        // Configuración de comportamiento
        private const val BENCHMARK_DURATION_MS = 8000L    // 8 segundos de benchmark inicial
        private const val MONITORING_INTERVAL_MS = 5000L  // Monitorear cada 30 segundos (30000)
        private const val PERFORMANCE_HISTORY_SIZE = 20     // Guardar últimas 20 mediciones
        private const val MIN_FPS_THRESHOLD = 12           // FPS mínimo antes de downgrade
        private const val EXCELLENT_FPS_THRESHOLD = 25     // FPS para considerar upgrade
        private const val DETECTION_COOLDOWN_HOURS = 24    // Re-evaluar cada 24 horas

        @Volatile
        private var INSTANCE: OceanConfigManager? = null

        fun getInstance(context: Context): OceanConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OceanConfigManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val performanceDetector = DevicePerformanceDetector(context)
    private val performanceMonitor = PerformanceMonitor()

    // Estado actual de la configuración
    private val _currentConfig = MutableStateFlow(getDefaultConfig())
    val currentConfig: StateFlow<OceanPerformanceConfig> = _currentConfig.asStateFlow()

    // Estado del sistema de monitoreo
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // Corrutinas para manejo asíncrono
    private val managerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitoringJob: Job? = null
    private var benchmarkJob: Job? = null

    // Historial de rendimiento
    private val performanceHistory = mutableListOf<Float>()

    init {
        loadPerformanceHistory()
        // Inicialización diferida para no bloquear el startup
        managerScope.launch {
            initializeConfiguration()
        }
    }

    /**
     * Inicialización principal del sistema
     */
    private suspend fun initializeConfiguration() {
        try {
            val needsDetection = shouldRunDetection()

            if (needsDetection) {
                Log.d(TAG, "Iniciando detección automática de dispositivo...")
                detectAndConfigureDevice()
            } else {
                Log.d(TAG, "Usando configuración guardada")
                loadSavedConfiguration()
            }

            // Iniciar monitoreo automático si está habilitado
            if (isAutoAdjustEnabled()) {
                startPerformanceMonitoring()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error en inicialización, usando configuración de emergencia", e)
            _currentConfig.value = OceanPerformanceConfig.getEmergencyConfig()
        }
    }

    /**
     * Detecta automáticamente el dispositivo y configura la calidad óptima
     */
    private suspend fun detectAndConfigureDevice() = withContext(Dispatchers.Default) {
        try {
            val deviceSpecs = performanceDetector.detectDeviceSpecs()
            val optimalConfig = OceanPerformanceConfig.getConfigForTier(deviceSpecs.deviceTier)

            Log.i(TAG, "Dispositivo detectado: ${deviceSpecs.deviceTier} (Score: ${deviceSpecs.performanceScore})")
            Log.d(TAG, performanceDetector.getDebugInfo())

            // Guardar configuración detectada
            saveCurrentTier(deviceSpecs.deviceTier)
            saveLastDetectionTime()

            _currentConfig.value = optimalConfig

        } catch (e: Exception) {
            Log.e(TAG, "Error en detección automática", e)
            _currentConfig.value = OceanPerformanceConfig.getVeryLowConfig() // Fallback seguro
        }
    }

    /**
     * Carga la configuración guardada anteriormente
     */
    private fun loadSavedConfiguration() {
        try {
            val userOverride = getUserOverrideTier()
            val savedTier = if (userOverride != null) {
                userOverride
            } else {
                getCurrentTier() ?: DevicePerformanceDetector.DeviceTier.MEDIUM
            }

            val config = OceanPerformanceConfig.getConfigForTier(savedTier)
            _currentConfig.value = config

            Log.d(TAG, "Configuración cargada: ${config.tierName}")

        } catch (e: Exception) {
            Log.e(TAG, "Error cargando configuración guardada", e)
            _currentConfig.value = getDefaultConfig()
        }
    }

    /**
     * Inicia el benchmark inicial para validar la configuración
     */
    fun startInitialBenchmark() {
        benchmarkJob?.cancel()
        benchmarkJob = managerScope.launch {
            try {
                Log.d(TAG, "Iniciando benchmark inicial...")

                delay(2000) // Esperar a que la UI se estabilice

                var totalFrames = 0
                var totalFrameTime = 0L
                val startTime = System.currentTimeMillis()

                while (System.currentTimeMillis() - startTime < BENCHMARK_DURATION_MS) {
                    val frameStart = System.nanoTime()

                    // Simular un frame (en la práctica esto será llamado desde tu Canvas)
                    delay(currentConfig.value.frameDelayMs)

                    val frameTime = System.nanoTime() - frameStart
                    totalFrameTime += frameTime
                    totalFrames++

                    if (totalFrames % 10 == 0) {
                        val currentFPS = 1_000_000_000f / (frameTime.toFloat())
                        recordFramePerformance(currentFPS)
                    }
                }

                val averageFPS = if (totalFrames > 0) {
                    1_000_000_000f / (totalFrameTime.toFloat() / totalFrames)
                } else {
                    15f
                }

                Log.i(TAG, "Benchmark completado: ${averageFPS.toInt()} FPS promedio")

                // Evaluar si necesitamos ajustar la configuración
                evaluateAndAdjustPerformance(averageFPS)

            } catch (e: Exception) {
                Log.e(TAG, "Error en benchmark inicial", e)
            }
        }
    }

    /**
     * Inicia el monitoreo continuo de rendimiento
     */
    private fun startPerformanceMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = managerScope.launch {
            _isMonitoring.value = true
            Log.d(TAG, "Iniciando monitoreo continuo de rendimiento")

            try {
                while (isActive) {
                    delay(MONITORING_INTERVAL_MS)

                    val averageFPS = calculateRecentAverageFPS()
                    if (averageFPS > 0) {
                        evaluateAndAdjustPerformance(averageFPS)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error en monitoreo de rendimiento", e)
            } finally {
                _isMonitoring.value = false
            }
        }
    }

    /**
     * Registra el rendimiento de un frame individual
     * Esta función debe ser llamada desde tu Canvas durante el renderizado
     */
    fun recordFramePerformance(fps: Float) {
        if (fps > 0 && fps < 200) { // Filtrar valores extremos
            performanceMonitor.recordFrame(fps)

            // Actualizar historial
            synchronized(performanceHistory) {
                performanceHistory.add(fps)
                if (performanceHistory.size > PERFORMANCE_HISTORY_SIZE) {
                    performanceHistory.removeAt(0)
                }
            }
        }
    }

    /**
     * Evalúa el rendimiento actual y ajusta la configuración si es necesario
     */
    private suspend fun evaluateAndAdjustPerformance(averageFPS: Float) {
        if (!isAutoAdjustEnabled()) return

        val currentTier = getCurrentTierFromConfig()
        val currentTargetFPS = currentConfig.value.targetFPS

        Log.d(TAG, "Evaluando rendimiento: ${averageFPS.toInt()} FPS (target: $currentTargetFPS)")

        when {
            // Rendimiento muy bajo - bajar calidad inmediatamente
            averageFPS < MIN_FPS_THRESHOLD && currentTier != DevicePerformanceDetector.DeviceTier.VERY_LOW -> {
                Log.w(TAG, "Rendimiento crítico detectado, bajando calidad")
                adjustConfigurationTier(false)
            }

            // Rendimiento excelente sostenido - considerar subir calidad
            averageFPS > EXCELLENT_FPS_THRESHOLD &&
                    averageFPS > currentTargetFPS * 1.2f &&
                    currentTier != DevicePerformanceDetector.DeviceTier.HIGH -> {

                // Verificar que el buen rendimiento sea sostenido
                val recentHistory = getRecentPerformanceHistory(10)
                if (recentHistory.size >= 8 && recentHistory.all { it > EXCELLENT_FPS_THRESHOLD }) {
                    Log.i(TAG, "Rendimiento excelente sostenido, subiendo calidad")
                    adjustConfigurationTier(true)
                }
            }
        }

        // Guardar estadísticas
        saveAverageFPS(averageFPS)
    }

    /**
     * Ajusta la configuración subiendo o bajando un nivel
     */
    private suspend fun adjustConfigurationTier(upgrade: Boolean) {
        val currentTier = getCurrentTierFromConfig()
        val newTier = if (upgrade) {
            when (currentTier) {
                DevicePerformanceDetector.DeviceTier.VERY_LOW -> DevicePerformanceDetector.DeviceTier.LOW
                DevicePerformanceDetector.DeviceTier.LOW -> DevicePerformanceDetector.DeviceTier.MEDIUM
                DevicePerformanceDetector.DeviceTier.MEDIUM -> DevicePerformanceDetector.DeviceTier.HIGH
                DevicePerformanceDetector.DeviceTier.HIGH -> DevicePerformanceDetector.DeviceTier.HIGH // Ya en el máximo
            }
        } else {
            when (currentTier) {
                DevicePerformanceDetector.DeviceTier.HIGH -> DevicePerformanceDetector.DeviceTier.MEDIUM
                DevicePerformanceDetector.DeviceTier.MEDIUM -> DevicePerformanceDetector.DeviceTier.LOW
                DevicePerformanceDetector.DeviceTier.LOW -> DevicePerformanceDetector.DeviceTier.VERY_LOW
                DevicePerformanceDetector.DeviceTier.VERY_LOW -> DevicePerformanceDetector.DeviceTier.VERY_LOW // Ya en el mínimo
            }
        }

        if (newTier != currentTier) {
            val newConfig = OceanPerformanceConfig.getConfigForTier(newTier)
            _currentConfig.value = newConfig
            saveCurrentTier(newTier)

            val action = if (upgrade) "subió" else "bajó"
            Log.i(TAG, "Configuración $action automáticamente a: ${newConfig.tierName}")
        }
    }

    /**
     * API pública para obtener la configuración actual
     */
    fun getCurrentConfig(): OceanPerformanceConfig {
        return currentConfig.value
    }

    /**
     * Permite al usuario forzar una configuración específica
     */
    fun setUserOverrideConfig(tier: DevicePerformanceDetector.DeviceTier) {
        val config = OceanPerformanceConfig.getConfigForTier(tier)
        _currentConfig.value = config
        saveUserOverrideTier(tier)

        Log.i(TAG, "Usuario forzó configuración: ${config.tierName}")
    }

    /**
     * Limpia el override del usuario y vuelve a la detección automática
     */
    fun clearUserOverride() {
        clearUserOverrideTier()
        managerScope.launch {
            detectAndConfigureDevice()
        }
    }

    /**
     * Habilita/deshabilita el ajuste automático de calidad
     */
    fun setAutoAdjustEnabled(enabled: Boolean) {
        saveAutoAdjustEnabled(enabled)

        if (enabled && !_isMonitoring.value) {
            startPerformanceMonitoring()
        } else if (!enabled && _isMonitoring.value) {
            stopPerformanceMonitoring()
        }
    }

    /**
     * Detiene el monitoreo de rendimiento
     */
    fun stopPerformanceMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
        Log.d(TAG, "Monitoreo de rendimiento detenido")
    }

    /**
     * Fuerza una re-evaluación completa del dispositivo
     */
    fun forceDeviceRedetection() {
        managerScope.launch {
            Log.i(TAG, "Forzando re-detección del dispositivo...")
            clearLastDetectionTime()
            detectAndConfigureDevice()
        }
    }

    /**
     * Obtiene estadísticas de rendimiento para mostrar al usuario
     */
    fun getPerformanceStats(): PerformanceStats {
        val recentFPS = calculateRecentAverageFPS()
        val totalRuntime = getTotalRuntimeSeconds()
        val currentTier = getCurrentTierFromConfig()

        return PerformanceStats(
            currentTier = currentTier,
            averageFPS = recentFPS,
            targetFPS = currentConfig.value.targetFPS,
            totalRuntimeSeconds = totalRuntime,
            isAutoAdjustEnabled = isAutoAdjustEnabled(),
            isMonitoring = _isMonitoring.value,
            performanceHistory = performanceHistory.toList(),
            computationalLoad = currentConfig.value.getComputationalLoad(),
            deviceInfo = performanceDetector.getDebugInfo()
        )
    }

    // === FUNCIONES PRIVADAS DE UTILIDAD ===

    private fun getDefaultConfig(): OceanPerformanceConfig {
        return OceanPerformanceConfig.getMediumConfig() // Configuración segura por defecto
    }

    private fun shouldRunDetection(): Boolean {
        val lastDetection = getLastDetectionTime()
        val now = System.currentTimeMillis()
        val hoursSinceLastDetection = (now - lastDetection) / (1000 * 60 * 60)

        return lastDetection == 0L || hoursSinceLastDetection >= DETECTION_COOLDOWN_HOURS
    }

    private fun getCurrentTierFromConfig(): DevicePerformanceDetector.DeviceTier {
        val config = currentConfig.value
        return when (config.tierName) {
            "Gama Muy Baja" -> DevicePerformanceDetector.DeviceTier.VERY_LOW
            "Gama Baja" -> DevicePerformanceDetector.DeviceTier.LOW
            "Gama Media" -> DevicePerformanceDetector.DeviceTier.MEDIUM
            "Gama Alta" -> DevicePerformanceDetector.DeviceTier.HIGH
            else -> DevicePerformanceDetector.DeviceTier.MEDIUM
        }
    }

    private fun calculateRecentAverageFPS(): Float {
        synchronized(performanceHistory) {
            if (performanceHistory.isEmpty()) return 0f

            val recentSamples = performanceHistory.takeLast(10)
            return recentSamples.average().toFloat()
        }
    }

    private fun getRecentPerformanceHistory(count: Int): List<Float> {
        synchronized(performanceHistory) {
            return performanceHistory.takeLast(count)
        }
    }

    // === FUNCIONES DE PERSISTENCIA ===

    private fun saveCurrentTier(tier: DevicePerformanceDetector.DeviceTier) {
        prefs.edit().putString(KEY_CURRENT_TIER, tier.name).apply()
    }

    private fun getCurrentTier(): DevicePerformanceDetector.DeviceTier? {
        val tierName = prefs.getString(KEY_CURRENT_TIER, null)
        return tierName?.let { DevicePerformanceDetector.DeviceTier.valueOf(it) }
    }

    private fun saveUserOverrideTier(tier: DevicePerformanceDetector.DeviceTier) {
        prefs.edit().putString(KEY_USER_OVERRIDE, tier.name).apply()
    }

    private fun getUserOverrideTier(): DevicePerformanceDetector.DeviceTier? {
        val tierName = prefs.getString(KEY_USER_OVERRIDE, null)
        return tierName?.let { DevicePerformanceDetector.DeviceTier.valueOf(it) }
    }

    private fun clearUserOverrideTier() {
        prefs.edit().remove(KEY_USER_OVERRIDE).apply()
    }

    private fun saveLastDetectionTime() {
        prefs.edit().putLong(KEY_LAST_DETECTION_TIME, System.currentTimeMillis()).apply()
    }

    private fun getLastDetectionTime(): Long {
        return prefs.getLong(KEY_LAST_DETECTION_TIME, 0L)
    }

    private fun clearLastDetectionTime() {
        prefs.edit().remove(KEY_LAST_DETECTION_TIME).apply()
    }

    private fun saveAutoAdjustEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ADJUST_ENABLED, enabled).apply()
    }

    private fun isAutoAdjustEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_ADJUST_ENABLED, true) // Por defecto habilitado
    }

    private fun saveAverageFPS(fps: Float) {
        prefs.edit().putFloat(KEY_AVERAGE_FPS, fps).apply()
    }

    private fun getTotalRuntimeSeconds(): Long {
        return prefs.getLong(KEY_TOTAL_RUNTIME_SECONDS, 0L)
    }

    private fun savePerformanceHistory() {
        synchronized(performanceHistory) {
            val historyString = performanceHistory.joinToString(",")
            prefs.edit().putString(KEY_PERFORMANCE_HISTORY, historyString).apply()
        }
    }

    private fun loadPerformanceHistory() {
        val historyString = prefs.getString(KEY_PERFORMANCE_HISTORY, "")
        if (!historyString.isNullOrEmpty()) {
            synchronized(performanceHistory) {
                performanceHistory.clear()
                try {
                    val values = historyString.split(",").map { it.toFloat() }
                    performanceHistory.addAll(values.takeLast(PERFORMANCE_HISTORY_SIZE))
                } catch (e: Exception) {
                    Log.w(TAG, "Error cargando historial de rendimiento", e)
                }
            }
        }
    }

    /**
     * Limpieza de recursos al destruir la instancia
     */
    fun destroy() {
        monitoringJob?.cancel()
        benchmarkJob?.cancel()
        savePerformanceHistory()
        managerScope.cancel()

        Log.d(TAG, "OceanConfigManager destruido")
    }

    /**
     * Data class para estadísticas de rendimiento
     */
    data class PerformanceStats(
        val currentTier: DevicePerformanceDetector.DeviceTier,
        val averageFPS: Float,
        val targetFPS: Int,
        val totalRuntimeSeconds: Long,
        val isAutoAdjustEnabled: Boolean,
        val isMonitoring: Boolean,
        val performanceHistory: List<Float>,
        val computationalLoad: Float,
        val deviceInfo: String
    )
}