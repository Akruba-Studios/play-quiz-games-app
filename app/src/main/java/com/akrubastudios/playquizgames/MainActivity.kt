package com.akrubastudios.playquizgames

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.akrubastudios.playquizgames.ui.screens.game.GameScreen
import com.akrubastudios.playquizgames.ui.theme.PlayQuizGamesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INICIO DE LA SOLUCIÓN UNIVERSAL ---
        hideSystemUI()
        // --- FIN DE LA SOLUCIÓN UNIVERSAL ---

        setContent {
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Volvemos a ocultar las barras si el usuario sale de la app y vuelve.
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        // Usamos el método @Suppress para decirle al compilador que sabemos que
        // estas propiedades están obsoletas, pero las usamos por compatibilidad.
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                // Oculta la barra de estado (la de arriba)
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Le dice a la app que dibuje su layout debajo de la barra de estado
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Mantiene el layout estable
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PlayQuizGamesTheme {
        Greeting("Android")
    }
}