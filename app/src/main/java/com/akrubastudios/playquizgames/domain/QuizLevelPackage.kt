package com.akrubastudios.playquizgames.domain

import com.google.firebase.firestore.PropertyName

data class QuizLevelPackage(
    val levelId: String = "",
    val levelName: Map<String, String> = emptyMap(),
    val questions: List<Question> = emptyList(),
    @get:PropertyName("isBossLevel")
    val isBossLevel: Boolean = false,
    val studyTopics: Map<String, List<String>> = emptyMap()
)