package com.akrubastudios.playquizgames.ui.screens.game

import com.akrubastudios.playquizgames.domain.Question

data class GameState(
    val isLoading: Boolean = true,
    val currentQuestion: Question? = null,
    val currentCorrectAnswer: String = "",
    val questionText: String = "",
    val score: Int = 0,
    val questionNumber: Int = 1,
    val totalQuestions: Int = 0,
    val correctAnswersCount: Int = 0,
    val generatedHintLetters: String = "",
    val userAnswer: String = "",
    val remainingTime: Long = 15L, // <-- Tiempo 15 segundos por respuesta
    val difficulty: String = "principiante",
    val usedLetterIndices: Set<Int> = emptySet(),
    val showCorrectAnimation: Boolean = false,
    val showIncorrectAnimation: Boolean = false,
    val timerExplosion: Boolean = false,
    val questionResults: List<Boolean?> = emptyList(), // Se inicializa según totalQuestions
    val showClearAnimation: Boolean = false,
)