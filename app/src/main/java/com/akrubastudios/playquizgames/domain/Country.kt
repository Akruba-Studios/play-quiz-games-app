package com.akrubastudios.playquizgames.domain

data class Country(
    val countryId: String = "",
    val name: Map<String, String> = emptyMap(),
    val continentId: String = "",
    val pcRequired: Long = 0, // Usamos Long para números grandes
    val bossLevelId: String = "",
    val availableCategories: Map<String, Boolean> = emptyMap(),
    val neighbors: List<String> = emptyList(),
    val backgroundImageUrl: String = ""
)