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
        var score = 0

        // RAM Score (40% del peso total) - Máximo 40 puntos
        score += when {
            ramGB <= 1f -> 0
            ramGB <= 2f -> 8
            ramGB <= 3f -> 16
            ramGB <= 4f -> 24
            ramGB <= 6f -> 32
            ramGB <= 8f -> 38
            else -> 40
        }

        // CPU Score (30% del peso total) - Máximo 30 puntos
        val cpuScore = when {
            cpuCores <= 2 -> 5
            cpuCores <= 4 -> 15
            cpuCores <= 6 -> 22
            cpuCores <= 8 -> 28
            else -> 30
        }
        score += cpuScore

        // Architecture Bonus/Penalty (5% del peso)
        val archScore = when (cpuArch) {
            "arm64", "aarch64" -> 5
            "armv7" -> 3
            "x86_64" -> 4
            "x86" -> 2
            else -> 1
        }
        score += archScore

        // Android Version Score (10% del peso) - Máximo 10 puntos
        val androidScore = when {
            androidVersion >= 33 -> 10 // Android 13+
            androidVersion >= 30 -> 8  // Android 11+
            androidVersion >= 28 -> 6  // Android 9+
            androidVersion >= 24 -> 4  // Android 7+
            androidVersion >= 21 -> 2  // Android 5+
            else -> 0
        }
        score += androidScore

        // Screen Resolution Penalty (15% del peso)
        // Resoluciones más altas requieren más recursos
        val resolutionPenalty = when {
            screenResolution <= (1280L * 720L) -> 15    // HD o menor
            screenResolution <= (1920L * 1080L) -> 12   // Full HD
            screenResolution <= (2560L * 1440L) -> 8    // QHD
            screenResolution <= (3840L * 2160L) -> 4    // 4K
            else -> 0 // 4K+
        }
        score += resolutionPenalty

        // Asegurar que el score esté en el rango 0-100
        return max(0, min(100, score))
    }

    /**
     * Determina la gama del dispositivo basándose en el puntaje
     */
    private fun determineDeviceTier(score: Int): DeviceTier {
        return when {
            score <= 25 -> DeviceTier.VERY_LOW
            score <= 50 -> DeviceTier.LOW
            score <= 75 -> DeviceTier.MEDIUM
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