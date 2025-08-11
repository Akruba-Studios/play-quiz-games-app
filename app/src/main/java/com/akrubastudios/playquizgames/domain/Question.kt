package com.akrubastudios.playquizgames.domain

data class Question(
    val id: String = "",
    val imageUrl: String = "",
    val correctAnswer: String = "",
    val validAnswers: List<String> = emptyList(),
    val hintLetters: String = ""
)