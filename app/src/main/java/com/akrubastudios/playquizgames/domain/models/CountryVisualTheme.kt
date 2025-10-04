package com.akrubastudios.playquizgames.domain.models

import androidx.compose.ui.graphics.Color
import com.akrubastudios.playquizgames.ui.screens.boss.background.ArchetypeRegistry
import com.akrubastudios.playquizgames.ui.screens.boss.background.BackgroundArchetype

/**
 * Datos visuales completos de un país cargados desde Firebase
 */
data class CountryVisualTheme(
    val countryId: String,
    val visualArchetype: String,
    val accentColor: String, // Hex color de la bandera
    val guardianData: GuardianData
)

/**
 * Datos del guardián del país
 */
data class GuardianData(
    val name: Map<String, String>, // "es" -> "Nombre", "en" -> "Name"
    val dialogues: Map<String, Map<String, List<String>>> // phase -> lang -> [dialogues]
)

/**
 * Representación interna del tema visual con colores parseados
 */
data class ParsedVisualTheme(
    val countryId: String,
    val archetype: BackgroundArchetype,
    val accentColor: Color,
    val guardianData: GuardianData
)

/**
 * Mapper de Firebase a modelo interno
 */
fun CountryVisualTheme.toParsed(archetypeRegistry: ArchetypeRegistry): ParsedVisualTheme {
    return ParsedVisualTheme(
        countryId = this.countryId,
        archetype = archetypeRegistry.getArchetype(this.visualArchetype),
        accentColor = parseHexColor(this.accentColor),
        guardianData = this.guardianData
    )
}

/**
 * Parser de color hexadecimal con fallback
 */
private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        Color(colorInt or 0xFF000000)
    } catch (e: Exception) {
        Color(0xFFD4AF37) // Gold fallback
    }
}