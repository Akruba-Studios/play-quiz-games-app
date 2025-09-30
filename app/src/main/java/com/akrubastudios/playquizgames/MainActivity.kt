package com.akrubastudios.playquizgames

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.ui.theme.PlayQuizGamesTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inyectamos el LanguageManager, que ya es un Singleton gestionado por Hilt. Control 5-MA
    // Esto es parte de nuestra arquitectura actual y está correcto.
    @Inject
    lateinit var languageManager: LanguageManager

    // --- INICIO DEL CÓDIGO A AÑADIR --- Comentar para validar que textos no estan alineados
    // Variable para guardar la densidad REAL del hardware
    private var hardwareDensity: Float = 0f

    override fun attachBaseContext(newBase: Context) {
        // NUEVO ENFOQUE: Solo controlar TEXTO, no layout/dimensiones
        val userMetrics = newBase.resources.displayMetrics

        Log.d("LAYOUT_DEBUG", "PRESERVING layout density: ${userMetrics.density}")
        Log.d("LAYOUT_DEBUG", "PRESERVING layout DPI: ${userMetrics.densityDpi}")
        Log.d("LAYOUT_DEBUG", "User scaledDensity: ${userMetrics.scaledDensity}")

        val newConfiguration = Configuration(newBase.resources.configuration)

        // Solo controlar el escalado de texto
        newConfiguration.fontScale = 1.0f // cambiar a 1.0 u aumentar para hacer pruebas de centrado texto
        // NO TOCAR densityDpi - mantener el original para que tus cálculos de layout funcionen

        val newContext = newBase.createConfigurationContext(newConfiguration)

        // CRÍTICO: Solo modificar scaledDensity (texto), mantener density (layout)
        val newMetrics = DisplayMetrics()
        newMetrics.setTo(userMetrics)
        newMetrics.density = userMetrics.density        // MANTENER original (tus cálculos)
        newMetrics.scaledDensity = userMetrics.density  // FORZAR = density (sin escalado de texto)
        newMetrics.densityDpi = userMetrics.densityDpi  // MANTENER original (tus cálculos)

        newContext.resources.updateConfiguration(newConfiguration, newMetrics)

        hardwareDensity = userMetrics.density  // Usar la densidad original para Compose

        Log.d("LAYOUT_DEBUG", "FINAL density: ${newMetrics.density} (unchanged)")
        Log.d("LAYOUT_DEBUG", "FINAL scaledDensity: ${newMetrics.scaledDensity} (fixed)")

        super.attachBaseContext(newContext)
    }
    // --- FIN DEL CÓDIGO A AÑADIR ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()

        // AGREGAR ESTA VERIFICACIÓN TEMPORAL PARA DEBUG
        Log.d("SCALE_DEBUG", "fontScale: ${resources.configuration.fontScale}")
        Log.d("SCALE_DEBUG", "density: ${resources.displayMetrics.density}")
        Log.d("SCALE_DEBUG", "scaledDensity: ${resources.displayMetrics.scaledDensity}")
        Log.d("SCALE_DEBUG", "densityDpi: ${resources.displayMetrics.densityDpi}")

        setContent {
            val currentLanguageCode by languageManager.languageStateFlow.collectAsState()

            val newLocale = Locale(currentLanguageCode)
            val configuration = Configuration(LocalConfiguration.current)
            configuration.setLocale(newLocale)

            // FORZAR configuración fija para Compose
            configuration.fontScale = 1.0f

            // Usar la densidad REAL del hardware (no la modificada por el sistema)
            val fixedDensity = Density(
                density = hardwareDensity,  // Densidad REAL del hardware
                fontScale = 1.0f           // Sin escalado de texto
            )

            Log.d("COMPOSE_DEBUG", "Using ORIGINAL system density: ${fixedDensity.density}")
            Log.d("COMPOSE_DEBUG", "Forcing fontScale: ${fixedDensity.fontScale}")
            Log.d("COMPOSE_DEBUG", "Layout calculations will work normally")

            LaunchedEffect(currentLanguageCode) {
                val locale = Locale(currentLanguageCode)
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)

                // FORZAR CONFIGURACIÓN AQUÍ TAMBIÉN
                config.fontScale = 1.0f
                val metrics = DisplayMetrics()
                metrics.setTo(resources.displayMetrics)
                metrics.scaledDensity = metrics.density

                resources.updateConfiguration(config, metrics)
            }

            // 3. LA CORRECCIÓN: Primero proveemos la configuración del idioma.
            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalDensity provides fixedDensity  // ESTA ES LA CLAVE
            ) {
                key(currentLanguageCode) {
                    PlayQuizGamesTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
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