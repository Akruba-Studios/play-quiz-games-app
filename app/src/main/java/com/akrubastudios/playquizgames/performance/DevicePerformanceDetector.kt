package com.akrubastudios.playquizgames.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

/**
 * Detecta automáticamente la gama de rendimiento del dispositivo
 * basándose en múltiples métricas de hardware
 */
class DevicePerformanceDetector(private val context: Context) {

    data class DeviceSpecs(
        val totalRamGB: Float,
        val cpuCores: Int,
        val androidVersion: Int,
        val screenResolution: Long,
        val cpuArchitecture: String,
        val performanceScore: Int,
        val deviceTier: DeviceTier
    )

    enum class DeviceTier {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Detecta las especificaciones completas del dispositivo
     */
    fun detectDeviceSpecs(): DeviceSpecs {
        val ramGB = getTotalRamGB()
        val cpuCores = getCpuCores()
        val androidVersion = Build.VERSION.SDK_INT
        val screenResolution = getScreenResolution()
        val cpuArch = getCpuArchitecture()

        val performanceScore = calculatePerformanceScore(
            ramGB, cpuCores, androidVersion, screenResolution, cpuArch
        )

        val deviceTier = determineDeviceTier(performanceScore)

        return DeviceSpecs(
            totalRamGB = ramGB,
            cpuCores = cpuCores,
            androidVersion = androidVersion,
            screenResolution = screenResolution,
            cpuArchitecture = cpuArch,
            performanceScore = performanceScore,
            deviceTier = deviceTier
        )
    }

    /**
     * Obtiene la RAM total del dispositivo en GB
     */
    private fun getTotalRamGB(): Float {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)

            // Convertir de bytes a GB
            val totalMemoryGB = memoryInfo.totalMem / (1024f * 1024f * 1024f)

            // Redondear a valores comunes de RAM
            when {
                totalMemoryGB <= 1.5f -> 1f
                totalMemoryGB <= 2.5f -> 2f
                totalMemoryGB <= 3.5f -> 3f
                totalMemoryGB <= 4.5f -> 4f
                totalMemoryGB <= 6f -> 6f
                totalMemoryGB <= 8f -> 8f
                totalMemoryGB <= 12f -> 12f
                else -> 16f
            }
        } catch (e: Exception) {
            // Fallback conservador
            2f
        }
    }

    /**
     * Obtiene el número de núcleos del CPU
     */
    private fun getCpuCores(): Int {
        return try {
            Runtime.getRuntime().availableProcessors()
        } catch (e: Exception) {
            4 // Fallback típico
        }
    }

    /**
     * Calcula la resolución total de la pantalla
     */
    private fun getScreenResolution(): Long {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            (displayMetrics.widthPixels.toLong() * displayMetrics.heightPixels.toLong())
        } catch (e: Exception) {
            1920L * 1080L // Fallback HD
        }
    }

    /**
     * Obtiene la arquitectura del CPU
     */
    private fun getCpuArchitecture(): String {
        return try {
            val supportedAbis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Build.SUPPORTED_ABIS
            } else {
                arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            }

            when {
                supportedAbis.any { it.contains("arm64") || it.contains("aarch64") } -> "arm64"
                supportedAbis.any { it.contains("armeabi-v7a") } -> "armv7"
                supportedAbis.any { it.contains("x86_64") } -> "x86_64"
                supportedAbis.any { it.contains("x86") } -> "x86"
                else -> "unknown"
            }
        } catch (e: Exception) {
            "armv7"
        }
    }

    /**
     * Calcula el puntaje de rendimiento basado en múltiples factores
     */
    private fun calculatePerformanceScore(
        ramGB: Float,
        cpuCores: Int,
        androidVersion: Int,
        screenResolution: Long,
        cpuArch: String
    ): Int {
        var score = 0.0

        // Puntuación de RAM (Peso: 25%) - Un poco más generoso
        val ramScore = when {
            ramGB < 4f -> 10.0
            ramGB < 6f -> 18.0
            ramGB < 8f -> 23.0 // <-- Aumento
            else -> 25.0
        }
        score += ramScore

        // Puntuación de CPU (Peso: 25%) - Un poco más generoso
        val cpuScore = when {
            cpuCores < 8 -> 15.0
            cpuCores == 8 -> 23.0 // <-- Aumento
            else -> 25.0
        }
        score += cpuScore

        // Puntuación de Versión de Android (Peso: 30%)
        val androidScore = when {
            androidVersion >= 33 -> 30.0
            androidVersion >= 31 -> 25.0
            androidVersion >= 29 -> 20.0
            else -> 10.0
        }
        score += androidScore

        // Penalización por Resolución de Pantalla
        val resolutionPenalty = when {
            screenResolution > (2560L * 1440L) -> -15.0
            screenResolution > (1920L * 1080L) -> -8.0
            else -> 0.0
        }
        score += resolutionPenalty

        if (cpuArch == "arm64") {
            score += 5.0
        }

        return max(0.0, min(100.0, score)).toInt()
    }

    /**
     * Determina la gama del dispositivo basándose en el puntaje
     */
    private fun determineDeviceTier(score: Int): DeviceTier {
        return when {
            score < 35 -> DeviceTier.VERY_LOW
            score < 55 -> DeviceTier.LOW
            score < 80 -> DeviceTier.MEDIUM // <-- Umbral para HIGH ahora es 80
            else -> DeviceTier.HIGH
        }
    }

    /**
     * Información de debug para desarrollo
     */
    fun getDebugInfo(): String {
        val specs = detectDeviceSpecs()
        return """
            Device Performance Debug:
            RAM: ${specs.totalRamGB}GB
            CPU Cores: ${specs.cpuCores}
            Android: API ${specs.androidVersion}
            Resolution: ${specs.screenResolution}
            Architecture: ${specs.cpuArchitecture}
            Score: ${specs.performanceScore}/100
            Tier: ${specs.deviceTier}
            
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Build: ${Build.DISPLAY}
        """.trimIndent()
    }
}