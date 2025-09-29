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
 * Gestor central de configuración oceánica con detección automática CONTROL9-OCM:
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
        private const val MONITORING_INTERVAL_MS = 3000L  // Monitorear cada 3 segundos (3000)
        private const val PERFORMANCE_HISTORY_SIZE = 20     // Guardar últimas 20 mediciones
        // private const val MIN_FPS_THRESHOLD = 12           // FPS mínimo antes de downgrade
        // private const val EXCELLENT_FPS_THRESHOLD = 25     // FPS para considerar upgrade
        private const val DETECTION_COOLDOWN_HOURS = 24    // Re-evaluar cada 24 horas

        // Umbrales basados en porcentajes del target FPS
        private const val DOWNGRADE_THRESHOLD_PERCENT = 0.65f  // 65% del target para bajar
        private const val UPGRADE_THRESHOLD_PERCENT = 1.65f      // 165% para subir (era 140%)
        private const val DOWNGRADE_REQUIRED_SAMPLES = 3       // De 8 mediciones para bajar
        private const val UPGRADE_REQUIRED_SAMPLES = 4         // De 8 mediciones para subir
        private const val EVALUATION_SAMPLE_SIZE = 4           // Tamaño de muestra para evaluación
        private const val DOWNGRADE_HYSTERESIS_PERCENT = 0.75f   // 75% para mantener tras upgrade
        private const val UPGRADE_HYSTERESIS_PERCENT = 1.25f     // 125% para mantener tras downgrade

        // Detección de crisis para ajustes inmediatos
        private const val CRISIS_THRESHOLD_PERCENT = 0.20f      // 20% del target (crisis crítica)
        private const val SEVERE_THRESHOLD_PERCENT = 0.35f      // 35% del target (crisis severa)
        private const val SEVERE_CONSECUTIVE_REQUIRED = 2        // Mediciones consecutivas para crisis severa
        // Umbrales absolutos para crisis, independientemente del target
        private const val ABSOLUTE_CRITICAL_FPS = 15f
        private const val ABSOLUTE_SEVERE_FPS = 20f

        // Control de decisiones recientes
        private const val RECENT_CHANGE_COOLDOWN_MS = 15000L     // 15 segundos tras cambio

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
    // Historial específico para evaluación de ajustes
    private val evaluationHistory = mutableListOf<Float>()

    // Control de detección de crisis
    private var lastCrisisDetectionTime = 0L
    private var consecutiveSevereReadings = 0
    private val crisisCooldownMs = 10000L // 10 segundos entre detecciones de crisis

    // Control de histéresis y cambios recientes
    private var lastQualityChangeTime = 0L
    private var lastQualityChangeDirection: String? = null // "UP" o "DOWN"

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
     * Observer de benchmark inicial - solo observa FPS reales sin generar datos artificiales
     */
    fun startInitialBenchmark() {
        benchmarkJob?.cancel()
        benchmarkJob = managerScope.launch {
            try {
                Log.d(TAG, "Iniciando benchmark observer (solo observación)...")

                // Esperar a que la UI se estabilice y empiece el renderizado real
                delay(3000)

                // Variables para observación
                var observedSamples = 0
                var totalObservedFPS = 0f
                val benchmarkDurationMs = 8000L // 8 segundos de observación
                val startTime = System.currentTimeMillis()

                Log.d(TAG, "Benchmark observer iniciado - observando FPS reales por ${benchmarkDurationMs/1000}s")

                // Observar FPS reales cada segundo durante el benchmark
                while (System.currentTimeMillis() - startTime < benchmarkDurationMs) {
                    delay(1000L) // Observar cada segundo

                    // Obtener el FPS real actual del tracker (sin generarlo artificialmente)
                    val currentRealFPS = getCurrentRecentAverageFPS() // Nueva función helper

                    if (currentRealFPS > 0f && currentRealFPS < 200f) { // Filtrar valores válidos
                        totalObservedFPS += currentRealFPS
                        observedSamples++

                        Log.d(TAG, "Benchmark sample $observedSamples: ${currentRealFPS.toInt()} FPS reales")
                    } else {
                        Log.d(TAG, "Benchmark sample descartada: FPS inválido ($currentRealFPS)")
                    }
                }

                // Calcular resultado final
                val finalAverageFPS = if (observedSamples > 0) {
                    totalObservedFPS / observedSamples
                } else {
                    0f
                }

                if (finalAverageFPS > 0) {
                    Log.i(TAG, "Benchmark observer completado: ${finalAverageFPS.toInt()} FPS promedio (${observedSamples} muestras reales)")

                    // OPCIONAL: Solo evaluar si tenemos suficientes muestras válidas
                    if (observedSamples >= 5) {
                        Log.d(TAG, "Evaluando configuración basada en benchmark real...")
                        // NO llamar recordFramePerformance aquí para no contaminar
                        // Solo log para información
                    } else {
                        Log.w(TAG, "Benchmark insuficiente ($observedSamples muestras), saltando evaluación inicial")
                    }
                } else {
                    Log.w(TAG, "Benchmark observer falló - no se obtuvieron FPS válidos")
                }

            } catch (e: CancellationException) {
                // --- INICIO DEL BLOQUE AÑADIDO ---
                // Esto es normal, la corrutina fue cancelada a propósito.
                Log.d(TAG, "Benchmark observer cancelado, lo cual es normal.")
                throw e // Es buena práctica relanzar la excepción de cancelación.
                // --- FIN DEL BLOQUE AÑADIDO ---
            } catch (e: Exception) {
                Log.e(TAG, "Error en benchmark observer", e)
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
        Log.v(TAG, "FPS medido: ${fps.toInt()} (fuente: ${Thread.currentThread().name})")
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
        val currentTargetFPS = currentConfig.value.targetFPS.toFloat()
        val currentTime = System.currentTimeMillis()

        // =============================
        // FASE 1: DETECCIÓN DE CRISIS
        // =============================

        val crisisThreshold = maxOf(
            currentTargetFPS * CRISIS_THRESHOLD_PERCENT,
            ABSOLUTE_CRITICAL_FPS
        )
        val severeThreshold = maxOf(
            currentTargetFPS * SEVERE_THRESHOLD_PERCENT,
            ABSOLUTE_SEVERE_FPS
        )

        // Crisis crítica - bajar inmediatamente
        if (averageFPS < crisisThreshold &&
            currentTier != DevicePerformanceDetector.DeviceTier.VERY_LOW &&
            currentTime - lastCrisisDetectionTime > crisisCooldownMs) {

            Log.w(TAG, "🚨 CRISIS CRÍTICA: ${averageFPS.toInt()}FPS < ${crisisThreshold.toInt()}FPS - Bajando calidad inmediatamente")
            adjustConfigurationTier(false)
            lastCrisisDetectionTime = currentTime
            consecutiveSevereReadings = 0
            evaluationHistory.clear()
            return // Salir sin evaluación normal
        }

        // Crisis severa - bajar tras mediciones consecutivas
        if (averageFPS < severeThreshold && currentTier != DevicePerformanceDetector.DeviceTier.VERY_LOW) {
            consecutiveSevereReadings++

            if (consecutiveSevereReadings >= SEVERE_CONSECUTIVE_REQUIRED &&
                currentTime - lastCrisisDetectionTime > crisisCooldownMs) {

                Log.w(TAG, "⚠️ CRISIS SEVERA: ${consecutiveSevereReadings} mediciones bajo ${severeThreshold.toInt()}FPS - Bajando calidad")
                adjustConfigurationTier(false)
                lastCrisisDetectionTime = currentTime
                consecutiveSevereReadings = 0
                evaluationHistory.clear()
                return // Salir sin evaluación normal
            }

            Log.d(TAG, "Crisis severa detectada: ${consecutiveSevereReadings}/$SEVERE_CONSECUTIVE_REQUIRED mediciones bajo ${severeThreshold.toInt()}FPS")
        } else {
            // Resetear contador si salimos de crisis severa
            consecutiveSevereReadings = 0
        }

        // =============================
        // FASE 2: EVALUACIÓN NORMAL
        // =============================

        // Agregar al historial de evaluación normal
        synchronized(evaluationHistory) {
            evaluationHistory.add(averageFPS)
            if (evaluationHistory.size > EVALUATION_SAMPLE_SIZE) {
                evaluationHistory.removeAt(0)
            }
        }

        // Necesitamos suficiente historial para evaluar
        if (evaluationHistory.size < EVALUATION_SAMPLE_SIZE) {
            Log.d(TAG, "Historial insuficiente: ${evaluationHistory.size}/$EVALUATION_SAMPLE_SIZE muestras")
            return
        }

        // Calcular umbrales con histéresis basados en decisión previa
        val timeSinceLastChange = currentTime - lastQualityChangeTime
        val isInCooldown = timeSinceLastChange < RECENT_CHANGE_COOLDOWN_MS

        val (downgradeThreshold, upgradeThreshold) = if (isInCooldown && lastQualityChangeDirection != null) {
            // Aplicar histéresis - umbrales más estrictos después de cambio reciente
            when (lastQualityChangeDirection) {
                "UP" -> {
                    // Acabamos de subir calidad - usar histéresis para mantenerla
                    val hysteresisDown = currentTargetFPS * DOWNGRADE_HYSTERESIS_PERCENT
                    val normalUp = currentTargetFPS * UPGRADE_THRESHOLD_PERCENT
                    Pair(hysteresisDown, normalUp)
                }
                "DOWN" -> {
                    // Acabamos de bajar calidad - usar histéresis para mantenerla
                    val normalDown = currentTargetFPS * DOWNGRADE_THRESHOLD_PERCENT
                    val hysteresisUp = currentTargetFPS * UPGRADE_HYSTERESIS_PERCENT
                    Pair(normalDown, hysteresisUp)
                }
                else -> {
                    // Umbrales normales
                    val normalDown = currentTargetFPS * DOWNGRADE_THRESHOLD_PERCENT
                    val normalUp = currentTargetFPS * UPGRADE_THRESHOLD_PERCENT
                    Pair(normalDown, normalUp)
                }
            }
        } else {
            // Sin histéresis - umbrales normales
            val normalDown = currentTargetFPS * DOWNGRADE_THRESHOLD_PERCENT
            val normalUp = currentTargetFPS * UPGRADE_THRESHOLD_PERCENT
            Pair(normalDown, normalUp)
        }

        Log.d(TAG, "Evaluación normal: ${averageFPS.toInt()}FPS vs target ${currentTargetFPS.toInt()}FPS " +
                "(bajar<${downgradeThreshold.toInt()}, subir>${upgradeThreshold.toInt()}) " +
                "${if (isInCooldown) "[Histéresis: ${lastQualityChangeDirection}, ${(RECENT_CHANGE_COOLDOWN_MS - timeSinceLastChange) / 1000}s restantes]" else ""}")

        // Contar muestras que cumplen condiciones
        val samplesUnderDowngrade = evaluationHistory.count { it < downgradeThreshold }
        val samplesOverUpgrade = evaluationHistory.count { it > upgradeThreshold }

        when {
            // Condición para BAJAR calidad (evaluación normal)
            samplesUnderDowngrade >= DOWNGRADE_REQUIRED_SAMPLES &&
                    currentTier != DevicePerformanceDetector.DeviceTier.VERY_LOW -> {

                Log.i(TAG, "Bajando calidad (normal): $samplesUnderDowngrade/$EVALUATION_SAMPLE_SIZE muestras bajo ${downgradeThreshold.toInt()}FPS")
                adjustConfigurationTier(false)
                lastQualityChangeTime = currentTime
                lastQualityChangeDirection = "DOWN"
                evaluationHistory.clear()
            }

            // Condición para SUBIR calidad (evaluación normal)
            samplesOverUpgrade >= UPGRADE_REQUIRED_SAMPLES &&
                    currentTier != DevicePerformanceDetector.DeviceTier.HIGH -> {

                Log.i(TAG, "Subiendo calidad: $samplesOverUpgrade/$EVALUATION_SAMPLE_SIZE muestras sobre ${upgradeThreshold.toInt()}FPS")
                adjustConfigurationTier(true)
                lastQualityChangeTime = currentTime
                lastQualityChangeDirection = "UP"
                evaluationHistory.clear()
            }

            else -> {
                Log.d(TAG, "Sin cambios: bajo=${samplesUnderDowngrade}/${DOWNGRADE_REQUIRED_SAMPLES}, " +
                        "alto=${samplesOverUpgrade}/${UPGRADE_REQUIRED_SAMPLES}")
            }
        }

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
     * Obtiene el FPS promedio reciente sin afectar el historial de evaluación
     */
    private fun getCurrentRecentAverageFPS(): Float {
        synchronized(performanceHistory) {
            if (performanceHistory.isEmpty()) return 0f

            // Tomar las últimas 3-5 mediciones para obtener FPS actual
            val recentSamples = performanceHistory.takeLast(3)
            return if (recentSamples.isNotEmpty()) {
                recentSamples.average().toFloat()
            } else {
                0f
            }
        }
    }

    fun pauseMonitoring() {
        Log.d(TAG, "Pausando monitoreo automático")
        monitoringJob?.cancel()
        _isMonitoring.value = false
    }

    fun resumeMonitoring() {
        if (isAutoAdjustEnabled() && !_isMonitoring.value) {
            Log.d(TAG, "Reanudando monitoreo automático")
            startPerformanceMonitoring()
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

    // CAMBIO 4: Método adicional para debug de crisis (OPCIONAL)
    // ==============================================================
    // AGREGAR esta función al final de la clase OceanConfigManager:
    /**
     * Obtiene información de debug sobre el sistema de crisis
     */
    fun getCrisisDebugInfo(): String {
        val currentTargetFPS = currentConfig.value.targetFPS.toFloat()
        val crisisThreshold = currentTargetFPS * CRISIS_THRESHOLD_PERCENT
        val severeThreshold = currentTargetFPS * SEVERE_THRESHOLD_PERCENT
        val timeSinceLastCrisis = System.currentTimeMillis() - lastCrisisDetectionTime

        return """
        Sistema de Crisis Debug:
        - Target FPS: ${currentTargetFPS.toInt()}
        - Crisis crítica: < ${crisisThreshold.toInt()} FPS
        - Crisis severa: < ${severeThreshold.toInt()} FPS
        - Lecturas severas consecutivas: $consecutiveSevereReadings/$SEVERE_CONSECUTIVE_REQUIRED
        - Tiempo desde última crisis: ${timeSinceLastCrisis / 1000}s
        - Cooldown restante: ${maxOf(0, (crisisCooldownMs - timeSinceLastCrisis) / 1000)}s
    """.trimIndent()
    }
}