package com.akrubastudios.playquizgames.domain

data class Question(
    val id: String = "",
    val imageUrl: String = "",
    val correctAnswer: String = "",
    val validAnswers: List<String> = emptyList(),
    val hintLetters: String = "",
    val questionText_es: String = "", // <-- AÑADE ESTA LÍNEA
    val questionText_en: String = "", // <-- AÑADE ESTA LÍNEA
    val fun_fact_es: String = "",     // <-- AÑADE ESTA LÍNEA
    val fun_fact_en: String = ""      // <-- AÑADE ESTA LÍNEA
)