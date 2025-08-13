package com.akrubastudios.playquizgames.domain

data class UserLevelCompletion(
    val levelId: String = "",
    val userId: String = "", // Para futuras consultas
    val highScore: Int = 0,
    val starsEarned: Int = 0
)