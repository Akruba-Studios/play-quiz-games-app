package com.akrubastudios.playquizgames.domain

/**
 * Representa el progreso de un usuario en un nivel específico DENTRO de un país.
 * Se utiliza para rastrear el contador local de estrellas para el cálculo de PC.
 */
data class UserLevelCountryProgress(
    val userId: String = "",
    val levelId: String = "",
    val countryId: String = "",
    // Este campo almacena cuántas estrellas ya han sido recompensadas en este país para este nivel.
    val starsEarnedInCountry: Int = 0
)