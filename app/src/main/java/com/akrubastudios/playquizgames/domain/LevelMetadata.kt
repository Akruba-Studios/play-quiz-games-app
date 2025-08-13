package com.akrubastudios.playquizgames.domain

data class LevelMetadata(
    val levelId: String = "",
    val categoryId: String = "",
    val tierId: String = "",
    val levelNumber: Int = 0,
    val name: Map<String, String> = emptyMap()
)