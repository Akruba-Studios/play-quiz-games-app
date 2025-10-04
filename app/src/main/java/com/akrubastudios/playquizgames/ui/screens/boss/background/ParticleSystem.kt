package com.akrubastudios.playquizgames.ui.screens.boss.background

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Representa una partícula individual en el sistema - Control: 1-PS
 */
data class AmbientParticle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val color: Color,
    val size: Float,
    var alpha: Float,
    val seed: Int // Para movimiento único
)

/**
 * Gestiona el sistema completo de partículas ambientales
 */
class ParticleSystemManager(
    private val canvasWidth: Float,
    private val canvasHeight: Float,
    private val style: ParticleStyle,
    private val colors: List<Color>,
    private val phase: Int
) {
    private val particles = mutableListOf<AmbientParticle>()
    private val random = Random(System.currentTimeMillis())

    // Densidad base ajustada por fase
    private val baseParticleCount = 30
    private val sizeMultiplier = 2.5f  // Para aumentar tamaño particulas
    private val phaseMultiplier = when(phase) {
        1 -> 1.0f
        2 -> 1.3f
        3 -> 1.6f
        else -> 1.0f
    }

    private val targetParticleCount = (baseParticleCount * style.density * phaseMultiplier).toInt()
        .coerceIn(20, 60) // Límites para performance

    init {
        initializeParticles()
    }

    private fun initializeParticles() {
        particles.clear()
        repeat(targetParticleCount) {
            particles.add(createRandomParticle())
        }
    }

    private fun createRandomParticle(): AmbientParticle {
        val angle = random.nextFloat() * 2 * Math.PI
        val speed = style.speed * (0.3f + random.nextFloat() * 0.7f)

        return AmbientParticle(
            x = random.nextFloat() * canvasWidth,
            y = random.nextFloat() * canvasHeight,
            velocityX = (cos(angle) * speed).toFloat(),
            velocityY = (sin(angle) * speed).toFloat(),
            color = colors.random(random),
            size = style.size * (0.5f + random.nextFloat() * 0.5f) * sizeMultiplier, // Usa el multiplicador para agrandar particulas
            alpha = 0.3f + random.nextFloat() * 0.4f,
            seed = random.nextInt()
        )
    }

    /**
     * Actualiza todas las partículas para el siguiente frame
     */
    fun update(deltaTime: Float) {
        particles.forEach { particle ->
            // Actualizar posición
            particle.x += particle.velocityX * deltaTime * 60f
            particle.y += particle.velocityY * deltaTime * 60f

            // Efecto de oscilación sutil usando seed
            val oscillation = sin((System.currentTimeMillis() / 1000.0 + particle.seed) * 0.5) * 0.5f
            particle.alpha = (0.3f + oscillation * 0.3f).toFloat().coerceIn(0.1f, 0.7f)

            // Wrapping en los bordes
            if (particle.x < -50f) particle.x = canvasWidth + 50f
            if (particle.x > canvasWidth + 50f) particle.x = -50f
            if (particle.y < -50f) particle.y = canvasHeight + 50f
            if (particle.y > canvasHeight + 50f) particle.y = -50f
        }
    }

    /**
     * Retorna lista de partículas para dibujar
     */
    fun getParticles(): List<ParticleDrawData> {
        return particles.map { particle ->
            ParticleDrawData(
                position = Offset(particle.x, particle.y),
                color = particle.color.copy(alpha = particle.alpha),
                size = particle.size
            )
        }
    }

    /**
     * Ajusta la cantidad de partículas si cambia la fase
     */
    fun adjustForPhase(newPhase: Int, newColors: List<Color>) {
        val newMultiplier = when(newPhase) {
            1 -> 1.0f
            2 -> 1.3f
            3 -> 1.6f
            else -> 1.0f
        }

        val newTargetCount = (baseParticleCount * style.density * newMultiplier).toInt()
            .coerceIn(20, 60)

        // Añadir o eliminar partículas gradualmente
        when {
            particles.size < newTargetCount -> {
                repeat(newTargetCount - particles.size) {
                    particles.add(createRandomParticle())
                }
            }
            particles.size > newTargetCount -> {
                particles.shuffle()
                while (particles.size > newTargetCount) {
                    particles.removeAt(particles.lastIndex)
                }
            }
        }

        // Actualizar colores gradualmente (30% de las partículas)
        val particlesToRecolor = (particles.size * 0.3f).toInt()
        particles.shuffled().take(particlesToRecolor).forEach { particle ->
            particle.color.let { newColors.random(random) }
        }
    }
}

/**
 * Datos simplificados para dibujar una partícula
 */
data class ParticleDrawData(
    val position: Offset,
    val color: Color,
    val size: Float
)