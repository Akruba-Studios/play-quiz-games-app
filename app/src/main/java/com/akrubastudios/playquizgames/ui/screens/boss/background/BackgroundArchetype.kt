package com.akrubastudios.playquizgames.ui.screens.boss.background

import androidx.compose.ui.graphics.Color

/**
 * Define las características visuales de un arquetipo cultural
 */
data class BackgroundArchetype(
    val id: String,
    val name: String,
    val colorPalette: ArchetypeColorPalette,
    val patternType: PatternType,
    val particleStyle: ParticleStyle,
    val ambientIntensity: Float // 0.5 - 1.5
)

/**
 * Paleta de colores por fase
 */
data class ArchetypeColorPalette(
    val phase1: PhaseColors,
    val phase2: PhaseColors,
    val phase3: PhaseColors
)

/**
 * Colores de una fase específica
 */
data class PhaseColors(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val patternColor: Color,
    val particleColors: List<Color>,
    val vignetteColor: Color
)

/**
 * Tipos de patrones geométricos
 */
enum class PatternType {
    ZIGZAG,        // Escalones (andino, mesoamericano)
    WAVES,         // Ondas (costero, atlántico)
    GRID,          // Cuadrícula (urbano, moderno)
    RADIAL,        // Mandala (mediterráneo)
    DIAGONAL,      // Líneas diagonales (nórdico)
    MOSAIC         // Mosaico irregular (amazónico)
}

/**
 * Estilo de partículas ambientales
 */
data class ParticleStyle(
    val size: Float,        // 2f - 8f
    val speed: Float,       // 0.5f - 2.0f
    val density: Float      // 0.5f - 1.5f multiplicador
)

/**
 * Registro de todos los arquetipos disponibles
 */
class ArchetypeRegistry {
    private val archetypes = mutableMapOf<String, BackgroundArchetype>()

    init {
        registerAllArchetypes()
    }

    fun getArchetype(id: String): BackgroundArchetype {
        return archetypes[id] ?: archetypes["default"]!!
    }

    private fun registerAllArchetypes() {
        // SOUTH AMERICA
        archetypes["southern_cone"] = createSouthernConeArchetype()
        archetypes["andean"] = createAndeanArchetype()
        archetypes["amazon"] = createAmazonArchetype()
        archetypes["caribbean_sa"] = createCaribbeanSAArchetype()
        archetypes["river_plate"] = createRiverPlateArchetype()

        // EUROPE
        archetypes["mediterranean"] = createMediterraneanArchetype()
        archetypes["nordic"] = createNordicArchetype()
        archetypes["central_european"] = createCentralEuropeanArchetype()
        archetypes["atlantic"] = createAtlanticArchetype()

        // NORTH AMERICA
        archetypes["north_american"] = createNorthAmericanArchetype()
        archetypes["mesoamerican"] = createMesoamericanArchetype()
        archetypes["caribbean"] = createCaribbeanArchetype()
        archetypes["central_american"] = createCentralAmericanArchetype()

        // DEFAULT FALLBACK
        archetypes["default"] = createDefaultArchetype()
    }

    // ==================== SOUTH AMERICA ====================

    private fun createSouthernConeArchetype() = BackgroundArchetype(
        id = "southern_cone",
        name = "Southern Cone",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1E3A5F),
                backgroundBottom = Color(0xFF3A5F8F),
                patternColor = Color(0xFF74ACDF).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF74ACDF), Color(0xFFB8D4E8)),
                vignetteColor = Color(0xFF0A1F3F)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF2D4A6F),
                backgroundBottom = Color(0xFF5A7FA8),
                patternColor = Color(0xFF74ACDF).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFF74ACDF), Color(0xFFFFD700)),
                vignetteColor = Color(0xFF0A2F4F)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF3F5A7F),
                backgroundBottom = Color(0xFF6A8FB8),
                patternColor = Color(0xFF74ACDF).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFFF6B6B), Color(0xFFFFD700)),
                vignetteColor = Color(0xFF0A3F5F)
            )
        ),
        patternType = PatternType.DIAGONAL,
        particleStyle = ParticleStyle(size = 4f, speed = 1.0f, density = 1.0f),
        ambientIntensity = 1.0f
    )

    private fun createAndeanArchetype() = BackgroundArchetype(
        id = "andean",
        name = "Andean",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF2C1810),
                backgroundBottom = Color(0xFF5A3820),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.12f),
                particleColors = listOf(Color(0xFFD4AF37), Color(0xFFCD7F32)),
                vignetteColor = Color(0xFF1A0F08)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF3C2820),
                backgroundBottom = Color(0xFF6A4830),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.18f),
                particleColors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00)),
                vignetteColor = Color(0xFF2A1F18)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF4C3830),
                backgroundBottom = Color(0xFF7A5840),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.25f),
                particleColors = listOf(Color(0xFFFF6347), Color(0xFFFFD700)),
                vignetteColor = Color(0xFF3A2F28)
            )
        ),
        patternType = PatternType.ZIGZAG,
        particleStyle = ParticleStyle(size = 3f, speed = 0.8f, density = 0.8f),
        ambientIntensity = 1.2f
    )

    private fun createAmazonArchetype() = BackgroundArchetype(
        id = "amazon",
        name = "Amazon",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF0D3B2F),
                backgroundBottom = Color(0xFF1A5C47),
                patternColor = Color(0xFF4ADE80).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF4ADE80), Color(0xFF22D3EE)),
                vignetteColor = Color(0xFF052519)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF1D4B3F),
                backgroundBottom = Color(0xFF2A6C57),
                patternColor = Color(0xFF4ADE80).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFF10B981), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF153529)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF2D5B4F),
                backgroundBottom = Color(0xFF3A7C67),
                patternColor = Color(0xFF4ADE80).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF254539)
            )
        ),
        patternType = PatternType.MOSAIC,
        particleStyle = ParticleStyle(size = 5f, speed = 1.2f, density = 1.3f),
        ambientIntensity = 1.4f
    )

    private fun createCaribbeanSAArchetype() = BackgroundArchetype(
        id = "caribbean_sa",
        name = "Caribbean South America",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF0C4A6E),
                backgroundBottom = Color(0xFF0369A1),
                patternColor = Color(0xFF06B6D4).copy(alpha = 0.12f),
                particleColors = listOf(Color(0xFF06B6D4), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF042F4E)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF1C5A7E),
                backgroundBottom = Color(0xFF1369A1),
                patternColor = Color(0xFF06B6D4).copy(alpha = 0.18f),
                particleColors = listOf(Color(0xFF0EA5E9), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF143F5E)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF2C6A8E),
                backgroundBottom = Color(0xFF2379B1),
                patternColor = Color(0xFF06B6D4).copy(alpha = 0.25f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF244F6E)
            )
        ),
        patternType = PatternType.WAVES,
        particleStyle = ParticleStyle(size = 4f, speed = 1.1f, density = 1.2f),
        ambientIntensity = 1.1f
    )

    private fun createRiverPlateArchetype() = BackgroundArchetype(
        id = "river_plate",
        name = "River Plate",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1E293B),
                backgroundBottom = Color(0xFF334155),
                patternColor = Color(0xFF94A3B8).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF94A3B8), Color(0xFF60A5FA)),
                vignetteColor = Color(0xFF0F172A)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF2E394B),
                backgroundBottom = Color(0xFF435165),
                patternColor = Color(0xFF94A3B8).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFF60A5FA), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF1F273A)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF3E495B),
                backgroundBottom = Color(0xFF536175),
                patternColor = Color(0xFF94A3B8).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF2F374A)
            )
        ),
        patternType = PatternType.WAVES,
        particleStyle = ParticleStyle(size = 4f, speed = 0.9f, density = 1.0f),
        ambientIntensity = 0.9f
    )

    // ==================== EUROPE ====================

    private fun createMediterraneanArchetype() = BackgroundArchetype(
        id = "mediterranean",
        name = "Mediterranean",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF0C4A6E),
                backgroundBottom = Color(0xFF075985),
                patternColor = Color(0xFFFFFFFF).copy(alpha = 0.08f),
                particleColors = listOf(Color(0xFF7DD3FC), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF082F49)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF1C5A7E),
                backgroundBottom = Color(0xFF176995),
                patternColor = Color(0xFFFFFFFF).copy(alpha = 0.12f),
                particleColors = listOf(Color(0xFF38BDF8), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF183F59)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF2C6A8E),
                backgroundBottom = Color(0xFF2779A5),
                patternColor = Color(0xFFFFFFFF).copy(alpha = 0.18f),
                particleColors = listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF284F69)
            )
        ),
        patternType = PatternType.RADIAL,
        particleStyle = ParticleStyle(size = 4f, speed = 1.0f, density = 1.0f),
        ambientIntensity = 1.0f
    )

    private fun createNordicArchetype() = BackgroundArchetype(
        id = "nordic",
        name = "Nordic",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1E293B),
                backgroundBottom = Color(0xFF334155),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF60A5FA), Color(0xFFE0F2FE)),
                vignetteColor = Color(0xFF0F172A)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF2E394B),
                backgroundBottom = Color(0xFF435165),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFF3B82F6), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF1F273A)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF3E495B),
                backgroundBottom = Color(0xFF536175),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B)),
                vignetteColor = Color(0xFF2F374A)
            )
        ),
        patternType = PatternType.DIAGONAL,
        particleStyle = ParticleStyle(size = 3f, speed = 0.7f, density = 0.8f),
        ambientIntensity = 0.8f
    )

    private fun createCentralEuropeanArchetype() = BackgroundArchetype(
        id = "central_european",
        name = "Central European",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF27272A),
                backgroundBottom = Color(0xFF3F3F46),
                patternColor = Color(0xFFA1A1AA).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFFA1A1AA), Color(0xFFD4D4D8)),
                vignetteColor = Color(0xFF18181B)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF37373A),
                backgroundBottom = Color(0xFF4F4F56),
                patternColor = Color(0xFFA1A1AA).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFFD4AF37), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF28282B)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF47474A),
                backgroundBottom = Color(0xFF5F5F66),
                patternColor = Color(0xFFA1A1AA).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFD4AF37)),
                vignetteColor = Color(0xFF38383B)
            )
        ),
        patternType = PatternType.GRID,
        particleStyle = ParticleStyle(size = 4f, speed = 0.9f, density = 0.9f),
        ambientIntensity = 1.0f
    )

    private fun createAtlanticArchetype() = BackgroundArchetype(
        id = "atlantic",
        name = "Atlantic",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1E3A5F),
                backgroundBottom = Color(0xFF2D5A8F),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF60A5FA), Color(0xFFBAE6FD)),
                vignetteColor = Color(0xFF0F1F3F)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF2E4A6F),
                backgroundBottom = Color(0xFF3D6A9F),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFF3B82F6), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF1F2F4F)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF3E5A7F),
                backgroundBottom = Color(0xFF4D7AAF),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF2F3F5F)
            )
        ),
        patternType = PatternType.WAVES,
        particleStyle = ParticleStyle(size = 4f, speed = 1.0f, density = 1.1f),
        ambientIntensity = 1.0f
    )

    // ==================== NORTH AMERICA ====================

    private fun createNorthAmericanArchetype() = BackgroundArchetype(
        id = "north_american",
        name = "North American",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1F2937),
                backgroundBottom = Color(0xFF374151),
                patternColor = Color(0xFF93C5FD).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF93C5FD), Color(0xFFFCA5A5)),
                vignetteColor = Color(0xFF111827)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF2F3947),
                backgroundBottom = Color(0xFF475161),
                patternColor = Color(0xFF93C5FD).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFF60A5FA), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF212837)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF3F4957),
                backgroundBottom = Color(0xFF576171),
                patternColor = Color(0xFF93C5FD).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFEF4444), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF313847)
            )
        ),
        patternType = PatternType.GRID,
        particleStyle = ParticleStyle(size = 4f, speed = 1.0f, density = 1.0f),
        ambientIntensity = 1.0f
    )

    private fun createMesoamericanArchetype() = BackgroundArchetype(
        id = "mesoamerican",
        name = "Mesoamerican",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1A3A2A),
                backgroundBottom = Color(0xFF2A5A4A),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.12f),
                particleColors = listOf(Color(0xFFD4AF37), Color(0xFF4ADE80)),
                vignetteColor = Color(0xFF0A1F15)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF2A4A3A),
                backgroundBottom = Color(0xFF3A6A5A),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.18f),
                particleColors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00)),
                vignetteColor = Color(0xFF1A2F25)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF3A5A4A),
                backgroundBottom = Color(0xFF4A7A6A),
                patternColor = Color(0xFFD4AF37).copy(alpha = 0.25f),
                particleColors = listOf(Color(0xFFDC2626), Color(0xFFFFD700)),
                vignetteColor = Color(0xFF2A3F35)
            )
        ),
        patternType = PatternType.ZIGZAG,
        particleStyle = ParticleStyle(size = 5f, speed = 1.1f, density = 1.2f),
        ambientIntensity = 1.3f
    )

    private fun createCaribbeanArchetype() = BackgroundArchetype(
        id = "caribbean",
        name = "Caribbean",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF065F46),
                backgroundBottom = Color(0xFF059669),
                patternColor = Color(0xFF34D399).copy(alpha = 0.12f),
                particleColors = listOf(Color(0xFF34D399), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF064E3B)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF166F56),
                backgroundBottom = Color(0xFF15A679),
                patternColor = Color(0xFF34D399).copy(alpha = 0.18f),
                particleColors = listOf(Color(0xFF10B981), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF165E4B)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF267F66),
                backgroundBottom = Color(0xFF25B689),
                patternColor = Color(0xFF34D399).copy(alpha = 0.25f),
                particleColors = listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF266E5B)
            )
        ),
        patternType = PatternType.WAVES,
        particleStyle = ParticleStyle(size = 5f, speed = 1.2f, density = 1.3f),
        ambientIntensity = 1.2f
    )

    private fun createCentralAmericanArchetype() = BackgroundArchetype(
        id = "central_american",
        name = "Central American",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF134E4A),
                backgroundBottom = Color(0xFF0F766E),
                patternColor = Color(0xFF5EEAD4).copy(alpha = 0.12f),
                particleColors = listOf(Color(0xFF5EEAD4), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF134E4A)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFF235E5A),
                backgroundBottom = Color(0xFF1F867E),
                patternColor = Color(0xFF5EEAD4).copy(alpha = 0.18f),
                particleColors = listOf(Color(0xFF14B8A6), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF235E5A)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFF336E6A),
                backgroundBottom = Color(0xFF2F968E),
                patternColor = Color(0xFF5EEAD4).copy(alpha = 0.25f),
                particleColors = listOf(Color(0xFFF97316), Color(0xFFFBBF24)),
                vignetteColor = Color(0xFF336E6A)
            )
        ),
        patternType = PatternType.WAVES,
        particleStyle = ParticleStyle(size = 4f, speed = 1.1f, density = 1.1f),
        ambientIntensity = 1.1f
    )

    // ==================== DEFAULT ====================

    private fun createDefaultArchetype() = BackgroundArchetype(
        id = "default",
        name = "Default",
        colorPalette = ArchetypeColorPalette(
            phase1 = PhaseColors(
                backgroundTop = Color(0xFF1E3A8A),
                backgroundBottom = Color(0xFF3B82F6),
                patternColor = Color(0xFF60A5FA).copy(alpha = 0.1f),
                particleColors = listOf(Color(0xFF60A5FA), Color(0xFFBAE6FD)),
                vignetteColor = Color(0xFF1E293B)
            ),
            phase2 = PhaseColors(
                backgroundTop = Color(0xFFF59E0B),
                backgroundBottom = Color(0xFFEF4444),
                patternColor = Color(0xFFFCD34D).copy(alpha = 0.15f),
                particleColors = listOf(Color(0xFFFCD34D), Color(0xFFFCA5A5)),
                vignetteColor = Color(0xFF7C2D12)
            ),
            phase3 = PhaseColors(
                backgroundTop = Color(0xFFDC2626),
                backgroundBottom = Color(0xFF7F1D1D),
                patternColor = Color(0xFFFCA5A5).copy(alpha = 0.2f),
                particleColors = listOf(Color(0xFFFCA5A5), Color(0xFFFCD34D)),
                vignetteColor = Color(0xFF450A0A)
            )
        ),
        patternType = PatternType.GRID,
        particleStyle = ParticleStyle(size = 4f, speed = 1.0f, density = 1.0f),
        ambientIntensity = 1.0f
    )
}