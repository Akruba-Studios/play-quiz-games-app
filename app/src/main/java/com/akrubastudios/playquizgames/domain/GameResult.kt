package com.akrubastudios.playquizgames.domain

data class GameResult(
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int
)