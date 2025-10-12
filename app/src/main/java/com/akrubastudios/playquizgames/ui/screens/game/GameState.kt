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
    val questionTransition: Boolean = false,
    val isFunFactUsedInRound: Boolean = false,      // ¿Ya se usó la pista en esta partida?
    val areFunFactsUnlockedForLevel: Boolean = false, // ¿El jugador tiene 3 estrellas en este nivel?
    val showFunFactDialog: Boolean = false,         // ¿Debe mostrarse el diálogo ahora mismo?
    val currentFunFact: String = "",                 // El texto del fun fact a mostrar
    val hasSeenFunFactTutorial: Boolean = true,
    val showFunFactTutorialDialog: Boolean = false,
    val currentGems: Int = 0,
    val showHelpsSheet: Boolean = false,
    val isProcessingHelp: Boolean = false, // Para mostrar un loader
    val revealLetterUses: Int = 0, // Contador de usos por pregunta
    val revealedLetterPositions: Set<Int> = emptySet(), // Letras reveladas
    val isExtraTimeUsed: Boolean = false,
    val isPreloadingImages: Boolean = false,
    val visualTheme: com.akrubastudios.playquizgames.domain.models.ParsedVisualTheme? = null
)