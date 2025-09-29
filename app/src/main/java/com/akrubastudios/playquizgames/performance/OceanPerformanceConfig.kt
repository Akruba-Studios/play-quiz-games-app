package com.akrubastudios.playquizgames.performance

import com.akrubastudios.playquizgames.performance.DevicePerformanceDetector.DeviceTier

/**
 * Configuración de efectos oceánicos optimizada por gama de dispositivo - Control 1OPC
 */
data class OceanPerformanceConfig(
    // Configuración básica de rendering
    val stepSize: Int,
    val frameDelayMs: Long,
    val depthIntensity: Float,

    // Configuración de corrientes marinas
    val currentSpacing: Int,
    val curveDetailSpacing: Int,

    // Configuración de efectos especulares
    val specularCenters: Int,
    val specularEnabled: Boolean,

    // Configuración de capas de efectos
    val atmosphereEnabled: Boolean,
    val reflectionsEnabled: Boolean,
    val shimmerEnabled: Boolean,

    // Configuración de optimización avanzada
    val noiseCalculationInterval: Int, // Cada cuántos frames recalcular ruido
    val preCalculatedNoiseTables: Boolean,
    val targetFPS: Int,

    // Información de la configuración
    val tierName: String,
    val qualityDescription: String
) {

    companion object {

        /**
         * Configuración para dispositivos de gama muy baja
         * Prioriza rendimiento sobre calidad visual
         */
        fun getVeryLowConfig() = OceanPerformanceConfig(
            stepSize = 16,                    // Un poco más de detalle que el antiguo 'Low'
            frameDelayMs = 50L,              // 20 FPS
            depthIntensity = 0.40f,          // Efectos sutiles

            currentSpacing = 70,             // Corrientes muy espaciadas
            curveDetailSpacing = 14,         // Curvas simples

            specularCenters = 1,             // Mínimos reflejos
            specularEnabled = true,          // Al menos uno

            atmosphereEnabled = false,        // Sin efectos atmosféricos
            reflectionsEnabled = false,       // Sin reflexiones complejas
            shimmerEnabled = false,           // Sin shimmer

            noiseCalculationInterval = 3,    // Recalcular cada 3 frames
            preCalculatedNoiseTables = true, // Usar tablas optimizadas
            targetFPS = 20,

            tierName = "Gama Muy Baja",
            qualityDescription = "Optimizado para máximo rendimiento"
        )

        /**
         * Configuración para dispositivos de gama baja
         * Balance básico entre rendimiento y calidad
         */
        fun getLowConfig() = OceanPerformanceConfig(
            stepSize = 12,                   // Moderadamente detallado
            frameDelayMs = 33L,             // 30 FPS
            depthIntensity = 0.50f,         // Efectos más visibles

            currentSpacing = 50,            // Corrientes moderadas
            curveDetailSpacing = 10,        // Curvas con más detalle

            specularCenters = 2,            // Un punto especular
            specularEnabled = true,

            atmosphereEnabled = false,       // Aún sin atmosfera
            reflectionsEnabled = true,       // Reflexiones básicas
            shimmerEnabled = false,          // Sin shimmer aún

            noiseCalculationInterval = 2,   // Recalcular cada 2 frames
            preCalculatedNoiseTables = true,
            targetFPS = 30,

            tierName = "Gama Baja",
            qualityDescription = "Balance básico rendimiento-calidad"
        )

        /**
         * Configuración para dispositivos de gama media
         * Calidad visual moderada con buen rendimiento
         */
        fun getMediumConfig() = OceanPerformanceConfig(
            stepSize = 8,                  // Tu configuración actual
            frameDelayMs = 33L,            // 30 FPS
            depthIntensity = 0.65f,        // Mejorada vs actual

            currentSpacing = 35,           // Tu configuración actual
            curveDetailSpacing = 6,        // Tu configuración actual

            specularCenters = 2,           // Tu configuración actual
            specularEnabled = true,

            atmosphereEnabled = true,       // Efectos atmosféricos ligeros
            reflectionsEnabled = true,
            shimmerEnabled = true,          // Shimmer habilitado

            noiseCalculationInterval = 1,  // Cada frame (tu actual)
            preCalculatedNoiseTables = true,
            targetFPS = 30,

            tierName = "Gama Media",
            qualityDescription = "Calidad visual balanceada"
        )

        /**
         * Configuración para dispositivos de gama alta
         * Máxima calidad visual y efectos premium
         */
        fun getHighConfig() = OceanPerformanceConfig(
            stepSize = 6,                   // Muy detallado
            frameDelayMs = 16L,            // 60 FPS
            depthIntensity = 0.85f,        // Efectos intensos

            currentSpacing = 25,           // Corrientes densas
            curveDetailSpacing = 4,        // Curvas muy suaves

            specularCenters = 4,           // Múltiples reflejos
            specularEnabled = true,

            atmosphereEnabled = true,       // Todos los efectos
            reflectionsEnabled = true,
            shimmerEnabled = true,

            noiseCalculationInterval = 1,  // Máxima fluidez
            preCalculatedNoiseTables = true,
            targetFPS = 60,

            tierName = "Gama Alta",
            qualityDescription = "Máxima calidad visual"
        )

        /**
         * Configuración ultra para dispositivos gaming/flagship
         * (Opcional - para futuras expansiones)
         */
        fun getUltraConfig() = OceanPerformanceConfig(
            stepSize = 4,                   // Máximo detalle
            frameDelayMs = 16L,            // 60 FPS
            depthIntensity = 1.0f,         // Efectos al máximo

            currentSpacing = 15,           // Corrientes ultra-densas
            curveDetailSpacing = 2,        // Curvas perfectas

            specularCenters = 6,           // Reflejos complejos
            specularEnabled = true,

            atmosphereEnabled = true,
            reflectionsEnabled = true,
            shimmerEnabled = true,

            noiseCalculationInterval = 1,
            preCalculatedNoiseTables = true,
            targetFPS = 60,

            tierName = "Ultra",
            qualityDescription = "Calidad cinematográfica"
        )

        /**
         * Obtiene la configuración según la gama del dispositivo
         */
        fun getConfigForTier(tier: DeviceTier): OceanPerformanceConfig {
            return when (tier) {
                DeviceTier.VERY_LOW -> getVeryLowConfig()
                DeviceTier.LOW -> getLowConfig()
                DeviceTier.MEDIUM -> getMediumConfig()
                DeviceTier.HIGH -> getHighConfig()
            }
        }

        /**
         * Lista de todas las configuraciones disponibles
         * Útil para testing y configuración manual
         */
        fun getAllConfigs(): List<OceanPerformanceConfig> {
            return listOf(
                getVeryLowConfig(),
                getLowConfig(),
                getMediumConfig(),
                getHighConfig(),
                getUltraConfig()
            )
        }

        /**
         * Configuración de emergencia para cuando todo falle
         */
        fun getEmergencyConfig() = OceanPerformanceConfig(
            stepSize = 24,                  // Súper básico
            frameDelayMs = 100L,           // 10 FPS
            depthIntensity = 0.2f,         // Mínimos efectos

            currentSpacing = 100,          // Muy pocos elementos
            curveDetailSpacing = 20,

            specularCenters = 0,           // Sin efectos especulares
            specularEnabled = false,

            atmosphereEnabled = false,      // Sin efectos adicionales
            reflectionsEnabled = false,
            shimmerEnabled = false,

            noiseCalculationInterval = 5,  // Máximo ahorro
            preCalculatedNoiseTables = true,
            targetFPS = 10,

            tierName = "Emergencia",
            qualityDescription = "Configuración de supervivencia"
        )
    }

    /**
     * Calcula el factor de carga computacional estimado
     * Útil para monitoreo y ajustes dinámicos
     */
    fun getComputationalLoad(): Float {
        val baseLoad = 1000f / (stepSize * stepSize) // Carga base por área
        val fpsMultiplier = targetFPS / 30f           // Factor FPS
        val effectsMultiplier = when {
            atmosphereEnabled && reflectionsEnabled && shimmerEnabled -> 1.5f
            (atmosphereEnabled || reflectionsEnabled || shimmerEnabled) -> 1.2f
            else -> 1f
        }

        return baseLoad * fpsMultiplier * effectsMultiplier * depthIntensity
    }

    /**
     * Información legible para debug/configuración
     */
    override fun toString(): String {
        return """
            $tierName Ocean Config:
            - Step Size: ${stepSize}px
            - Target FPS: $targetFPS (${frameDelayMs}ms delay)
            - Depth Intensity: $depthIntensity
            - Current Spacing: ${currentSpacing}px
            - Curve Detail: ${curveDetailSpacing}px
            - Specular Centers: $specularCenters
            - Effects: ${if (atmosphereEnabled) "Atmosphere " else ""}${if (reflectionsEnabled) "Reflections " else ""}${if (shimmerEnabled) "Shimmer" else ""}
            - Computational Load: ${"%.1f".format(getComputationalLoad())}
            - Description: $qualityDescription
        """.trimIndent()
    }
}