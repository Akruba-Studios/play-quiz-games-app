package com.akrubastudios.playquizgames.domain

data class QuizLevelPackage(
    val levelId: String = "",
    val levelName: Map<String, String> = emptyMap(),
    val questions: List<Question> = emptyList(),
    val isBossLevel: Boolean = false,
    val studyTopics: Map<String, List<String>> = emptyMap()
)