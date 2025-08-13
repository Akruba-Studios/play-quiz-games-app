package com.akrubastudios.playquizgames.domain

data class Category(
    val categoryId: String = "",
    val name: Map<String, String> = emptyMap()
)