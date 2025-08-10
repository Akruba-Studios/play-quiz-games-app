package com.akrubastudios.playquizgames.domain

data class QuizLevelPackage(
    val levelId: String,
    val levelName: Map<String, String>, // Clave: "en" o "es", Valor: el nombre del nivel
    val questions: List<Question>
)