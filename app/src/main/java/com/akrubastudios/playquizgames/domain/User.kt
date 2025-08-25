package com.akrubastudios.playquizgames.domain

data class User(
    val uid: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val conqueredCountries: List<String> = emptyList(), // <-- AÑADE ESTA
    val dominatedCountries: List<String> = emptyList(),
    val pendingBossChallenge: String? = null,
    val unassignedPcBoosts: Int = 0,
    val totalXp: Long = 0,
    val availableCountries: List<String> = emptyList(),// <-- Y ESTA
    val createdAt: Long = 0,
    val hasSeenWelcomeDialog: Boolean = false,
    val hasSeenConquestTutorial: Boolean = false,
    val hasSeenLevelUnlockTutorial: Boolean = false,
    val hasSeenXpTutorial: Boolean = false,
    val hasSeenFreeModeUnlockedDialog: Boolean = false
)
