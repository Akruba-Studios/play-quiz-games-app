package com.akrubastudios.playquizgames.domain

import kotlinx.serialization.Serializable
import kotlin.OptIn // Puede que necesites esta importación
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class Question(
    val id: String,
    val imageUrl: String,
    val correctAnswer: String,
    val validAnswers: List<String>,
    val hintLetters: String
)