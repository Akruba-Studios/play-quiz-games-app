package com.akrubastudios.playquizgames.domain

data class RankedUser(
    val rank: Int = 0,
    val displayName: String = "Jugador",
    val totalXp: Long = 0,
    val photoUrl: String? = null
)