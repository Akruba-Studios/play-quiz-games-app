package com.akrubastudios.playquizgames.domain

/**
 * Contiene la lógica de negocio para calcular el nivel del jugador y su progreso.
 * Al ser un 'object', actúa como un singleton, garantizando una única instancia.
 */
object PlayerLevelManager {

    // Abandonamos la fórmula y definimos los umbrales de XP totales requeridos para cada nivel.
    // El índice de la lista corresponde al "Nivel - 1".
    private val levelThresholds = listOf(
        0L,         // Nivel 1
        10000L,     // Nivel 2
        25000L,     // Nivel 3
        45000L,     // Nivel 4
        75000L,     // Nivel 5
        115000L,    // Nivel 6
        170000L,    // Nivel 7
        240000L,    // Nivel 8
        330000L,    // Nivel 9
        450000L,    // Nivel 10
        600000L,    // Nivel 11
        780000L,    // Nivel 12
        1000000L,   // Nivel 13
        1270000L,   // Nivel 14
        1600000L    // Nivel 15
        // ... podemos añadir más niveles fácilmente aquí.
    )

    /**
     * Contenedor de datos para el resultado del cálculo de nivel.
     * Facilita el manejo de todos los valores relacionados con el nivel del jugador.
     */
    data class LevelInfo(
        val level: Int,
        // MODIFICADO: Cambiamos los nombres para mayor claridad. Ahora manejaremos XP totales.
        val playerTotalXp: Long,            // El XP total del jugador (ej. 23,200)
        val currentLevelThresholdXp: Long,  // El XP necesario para el nivel actual (ej. 10,000)
        val nextLevelThresholdXp: Long      // El XP necesario para el siguiente nivel (ej. 25,000)
    )

    /**
     * Calcula toda la información relevante del nivel de un jugador basándose en su XP total.
     *
     * @param totalXp El total de puntos de experiencia acumulados por el jugador.
     * @return Un objeto [LevelInfo] con el nivel, el progreso y los umbrales de XP.
     */
    fun calculateLevelInfo(totalXp: Long): LevelInfo {
        if (totalXp < 0) return LevelInfo(1, 0, 0, levelThresholds[1])

        // --- INICIO DE LA MODIFICACIÓN DE LÓGICA ---

        // 'findLastIndex' es una forma eficiente de encontrar el nivel.
        // Busca el último umbral en la tabla que el jugador ha superado.
        val currentLevelIndex = levelThresholds.indexOfLast { it <= totalXp }
        val level = currentLevelIndex + 1

        val currentThreshold = levelThresholds[currentLevelIndex]
        // Nos aseguramos de no intentar acceder a un índice que no existe si el jugador está en el nivel máximo.
        val nextThreshold = levelThresholds.getOrNull(currentLevelIndex + 1) ?: currentThreshold

        return LevelInfo(
            level = level,
            playerTotalXp = totalXp,
            currentLevelThresholdXp = currentThreshold,
            nextLevelThresholdXp = nextThreshold
        )
    }
}