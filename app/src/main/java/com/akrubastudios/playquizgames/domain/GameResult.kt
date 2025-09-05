package com.akrubastudios.playquizgames.domain

data class GameResult(
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val starsEarned: Int = 0,
    val pcGained: Int = 0,
    val gemsGained: Int = 0
)