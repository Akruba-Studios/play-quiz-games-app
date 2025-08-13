package com.akrubastudios.playquizgames.domain

data class User(
    val uid: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val conqueredCountries: List<String> = emptyList(), // <-- AÑADE ESTA
    val totalXp: Long = 0, // <-- Y ESTA
    val createdAt: Long = 0
)
