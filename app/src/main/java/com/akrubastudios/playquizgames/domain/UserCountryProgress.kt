package com.akrubastudios.playquizgames.domain

data class UserCountryProgress(
    val userId: String = "",
    val countryId: String = "",
    val currentPc: Long = 0
)