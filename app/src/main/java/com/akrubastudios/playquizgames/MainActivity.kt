package com.akrubastudios.playquizgames

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key // <-- NUEVA IMPORTACIÓN
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.ui.theme.PlayQuizGamesTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inyectamos el LanguageManager, que ya es un Singleton gestionado por Hilt.
    // Esto es parte de nuestra arquitectura actual y está correcto.
    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        setContent {
            // Recolectamos el idioma desde nuestro StateFlow.
            // Esta es la única fuente de verdad para el idioma en la app.
            val currentLanguageCode by languageManager.languageStateFlow.collectAsState()

            // Este efecto actualiza el Locale del contexto de Android cada vez que nuestro
            // StateFlow cambia. Esto prepara el terreno para que stringResource() funcione.
            LaunchedEffect(currentLanguageCode) {
                val locale = Locale(currentLanguageCode)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            // Aquí está la solución final y única.
            // Usamos el composable 'key' y le pasamos el 'currentLanguageCode' como "identificador".
            // Cuando 'currentLanguageCode' cambia (de "es" a "en"), Compose entiende que
            // todo lo que está dentro de este bloque 'key' es obsoleto y necesita ser
            // completamente recreado desde cero.
            key(currentLanguageCode) {
                PlayQuizGamesTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Al recrear la UI, CompositionLocalProvider asegura que el nuevo
                        // árbol de Composables se construya con la configuración de idioma actualizada.
                        // Esto fuerza a TODOS los stringResource() a leer el nuevo valor.
                        val newLocale = Locale(currentLanguageCode)
                        val configuration = Configuration(LocalConfiguration.current)
                        configuration.setLocale(newLocale)

                        CompositionLocalProvider(LocalConfiguration provides configuration) {
                            NavGraph()
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }
}