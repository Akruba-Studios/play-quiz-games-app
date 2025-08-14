package com.akrubastudios.playquizgames.domain

data class LevelStatus(
    val levelId: String,
    val levelName: String,
    val starsEarned: Int,
    val isLocked: Boolean
)