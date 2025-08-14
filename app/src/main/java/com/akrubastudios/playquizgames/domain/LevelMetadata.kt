package com.akrubastudios.playquizgames.domain

data class LevelMetadata(
    val levelId: String = "",
    val levelName: Map<String, String> = emptyMap(),
    val tierId: String = ""
    // No tiene categoryId ni levelNumber porque esos no están en el documento de Firestore
)