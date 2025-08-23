package com.akrubastudios.playquizgames.domain

data class Question(
    val id: String = "",
    val imageUrl: String = "",
    val correctAnswer_es: String = "", // <-- NUEVO
    val correctAnswer_en: String = "", // <-- NUEVO
    val validAnswers: Map<String, List<String>> = emptyMap(),
    val hintLetters: String = "",
    val questionText_es: String = "", // <-- AÑADE ESTA LÍNEA
    val questionText_en: String = "", // <-- AÑADE ESTA LÍNEA
    val fun_fact_es: String = "",     // <-- AÑADE ESTA LÍNEA
    val fun_fact_en: String = ""      // <-- AÑADE ESTA LÍNEA
)