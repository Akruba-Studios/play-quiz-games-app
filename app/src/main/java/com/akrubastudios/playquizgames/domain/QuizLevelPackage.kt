package com.akrubastudios.playquizgames.domain

import kotlinx.serialization.Serializable
import kotlin.OptIn
import kotlinx.serialization.InternalSerializationApi // Importa la correcta

@OptIn(InternalSerializationApi::class) // <-- USA InternalSerializationApi
@Serializable
data class QuizLevelPackage(
    val levelId: String,
    val levelName: Map<String, String>, // Clave: "en" o "es", Valor: el nombre del nivel
    val questions: List<Question>
)