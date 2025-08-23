package com.akrubastudios.playquizgames.core

import java.util.Locale

/**
 * Un objeto singleton que centraliza la lógica de detección de idioma.
 */
object LanguageManager {
    /**
     * Devuelve el sufijo de idioma a utilizar ('es' o 'en').
     * Por defecto, si el idioma del dispositivo no es español, usará inglés.
     */
    fun getLanguageSuffix(): String {
        return when (Locale.getDefault().language) {
            "es" -> "es"
            else -> "en"
        }
    }
}