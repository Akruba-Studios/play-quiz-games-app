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
            // 1. Recolectamos el idioma como antes.
            val currentLanguageCode by languageManager.languageStateFlow.collectAsState()

            // 2. Creamos la configuración actualizada.
            val newLocale = Locale(currentLanguageCode)
            val configuration = Configuration(LocalConfiguration.current)
            configuration.setLocale(newLocale)

            // Este efecto sigue siendo útil para recursos que no son de Compose.
            LaunchedEffect(currentLanguageCode) {
                val locale = Locale(currentLanguageCode)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            // 3. LA CORRECCIÓN: Primero proveemos la configuración del idioma.
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                // 4. AHORA, DENTRO del contexto correcto, aplicamos nuestro tema.
                // La 'key' sigue forzando la recomposición cuando el idioma cambia.
                key(currentLanguageCode) {
                    PlayQuizGamesTheme {
                        // 5. El Surface ya no necesita el 'color' override.
                        // Heredará el color 'surface' correcto de nuestro tema.
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background // <-- ESTA ES LA LÍNEA CORRECTA
                        ) {
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