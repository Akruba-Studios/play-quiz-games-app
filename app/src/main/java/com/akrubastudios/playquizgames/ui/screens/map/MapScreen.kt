package com.akrubastudios.playquizgames.ui.screens.map

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.ui.components.PlayerLevelIndicator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import com.caverock.androidsvg.SVG
import java.io.IOException

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive

import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.material.icons.filled.Flight

import androidx.compose.material.icons.filled.SwapHoriz

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import java.util.Locale

import androidx.compose.foundation.clickable // <-- AÑADE ESTA
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.SnackbarHost // <-- AÑADE ESTA
import androidx.compose.material3.SnackbarHostState // <-- AÑADE ESTA
import androidx.compose.runtime.remember // <-- AÑADE ESTA (si no está)
import androidx.compose.runtime.rememberCoroutineScope // <-- AÑADE ESTA
import kotlinx.coroutines.launch // <-- AÑADE ESTA (si no está)

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.AppExpeditionAlertDialog
import com.akrubastudios.playquizgames.ui.components.DialogButtonText
import com.akrubastudios.playquizgames.ui.components.DialogText
import com.akrubastudios.playquizgames.ui.components.DialogTitle
import com.akrubastudios.playquizgames.ui.components.GemIcon
import com.akrubastudios.playquizgames.ui.components.GemsIndicator
import com.akrubastudios.playquizgames.ui.components.VideoBackground
import com.akrubastudios.playquizgames.ui.components.getButtonTextColor
import com.akrubastudios.playquizgames.ui.theme.CyanAccent
import com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent
import com.akrubastudios.playquizgames.ui.theme.DeepNavy
import com.akrubastudios.playquizgames.ui.theme.LightGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

// ===================================================================
// COMPOSABLE MONITOR VISUAL DE FPS - CONTROL 44-MS
// ===================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current // Necesitaremos el contexto para los strings

    // NUEVO: Controlar el tracking según el lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                }
                Lifecycle.Event.ON_PAUSE -> {
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        lifecycleOwner.lifecycle.addObserver(viewModel)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ========================================================================
    // SISTEMA DE EFECTOS AMBIENTALES - CONFIGURACIÓN CENTRALIZADA
    // ========================================================================

    // --- ESTADOS INDIVIDUALES DE EFECTOS ---
    // Sistema de tormenta y Lluvia
    var isStormActive by remember { mutableStateOf(false) }
    var isRainActive by remember { mutableStateOf(false) }
    var stormFadeAlpha by remember { mutableStateOf(0f) }
    var rainFadeAlpha by remember { mutableStateOf(0f) }
    // Otros Efectos
    var isFishActive by remember { mutableStateOf(false) }
    var isMistActive by remember { mutableStateOf(false) }
    var isGodRaysActive by remember { mutableStateOf(false) }
    var isSpecularActive by remember { mutableStateOf(false) }
    var isGradientActive by remember { mutableStateOf(false) }
    var isBubblesActive by remember { mutableStateOf(false) }
    var fishFadeAlpha by remember { mutableStateOf(0f) }
    var mistFadeAlpha by remember { mutableStateOf(0f) }
    var godRaysFadeAlpha by remember { mutableStateOf(0f) }
    var specularFadeAlpha by remember { mutableStateOf(0f) }
    var gradientFadeAlpha by remember { mutableStateOf(0f) }
    var bubblesFadeAlpha by remember { mutableStateOf(0f) }

    // --- LISTAS DE CALIDAD GRÁFICA (Define qué efectos se muestran en cada nivel) ---
    val stormQualityLevels = remember { listOf("VERY_HIGH", "HIGH") }
    val rainQualityLevels = remember { listOf("VERY_HIGH", "MEDIUM") }

    val fishQualityLevels = remember { listOf("VERY_HIGH") } // Peces
    val mistQualityLevels = remember { listOf("LOW") } // Neblina
    val godRaysQualityLevels = remember { listOf("MEDIUM") } // Rayos verticales
    val specularQualityLevels = remember { listOf("MEDIUM") } //
    val bubblesQualityLevels = remember { listOf("MEDIUM") } // Burbujas
    val gradientQualityLevels = remember { emptyList<String>() } // Esto es para que el efecto no funcione en ninguan calidad, queda anulado


    // --- CONFIGURACIÓN DEL SISTEMA ---
    // Capa Ambiental (efectos sutiles frecuentes)
    val ambientTickInterval = 5000L        // Cada cuántos ms hace un "tick" (5000 = 5 segundos)
    val ambientCycleLength = 4             // Cuántos ticks para evaluar (4 * 5s = 20 segundos)
    val ambientProbability = 0.70f         // Probabilidad de activar efecto cuando se evalúa (0.70 = 70%)
    val ambientEffectDuration = 15000L     // Duración de efectos ambientales en ms (15000 = 15 segundos)

    // Capa Climática (eventos dramáticos poco frecuentes)
    val climaticCycleLength = 3           // Cuántos ticks para evaluar (18 * 5s = 90 segundos)
    val climaticProbability = 0.80f        // Probabilidad de evento climático (0.30 = 30%)
    val climaticEffectDuration = 20000L    // Duración de eventos climáticos en ms (20000 = 20 segundos) - ACTUAL: 20s para pruebas, pero era 45s

    // Control de superposición
    val allowEffectOverlap = true          // true = efectos pueden ocurrir simultáneamente, false = solo uno a la vez

    // Variables calculadas para efectos ambientales
    val shouldShowStorm = uiState.oceanQuality in stormQualityLevels && isStormActive
    val shouldShowRain = uiState.oceanQuality in rainQualityLevels && isRainActive
    val shouldShowFish = uiState.oceanQuality in fishQualityLevels && isFishActive
    val shouldShowMist = uiState.oceanQuality in mistQualityLevels && isMistActive
    val shouldShowGodRays = uiState.oceanQuality in godRaysQualityLevels && isGodRaysActive
    val shouldShowSpecular = uiState.oceanQuality in specularQualityLevels && isSpecularActive
    val shouldShowGradient = uiState.oceanQuality in gradientQualityLevels && isGradientActive
    val shouldShowBubbles = uiState.oceanQuality in bubblesQualityLevels && isBubblesActive

    val thunderPlayer = remember { MediaPlayer.create(context, R.raw.sfx_thunder) }
    // Creamos un par de reproductores para el bucle sin fisuras
    val rainPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.sfx_rain}")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            thunderPlayer.release()
            rainPlayer.release()
        }
    }
    // ========================================================================
    // CENTRO DE MANDO ÚNICO - SISTEMA CLIMÁTICO UNIFICADO
    // ========================================================================
    LaunchedEffect(key1 = "weather_scheduler", uiState.oceanQuality) {
        var ambientCounter = 0
        var climaticCounter = 0

        // Variable para rastrear si hay un efecto activo (para control de overlap)
        var activeEffectsCount = 0
        var isAmbientEffectActive = false
        var isClimaticEffectActive = false

        while (true) {
            delay(ambientTickInterval) // Tick base del sistema

            // ✅ SOLO incrementar si NO hay efecto activo
            if (!isAmbientEffectActive) {
                ambientCounter++
            }
            if (!isClimaticEffectActive) {
                climaticCounter++
            }

            // ====================================================================
            // CAPA AMBIENTAL - Efectos sutiles frecuentes
            // ====================================================================
            if (ambientCounter > ambientCycleLength && !isAmbientEffectActive) {
                // Comprobar si debemos activar un efecto ambiental
                if (Random.nextFloat() < ambientProbability) {
                    // Si NO se permite overlap Y ya hay un efecto activo, saltamos
                    if (!allowEffectOverlap && activeEffectsCount > 0) {
                        // No hacer nada, esperamos al siguiente ciclo
                    } else {
                        ambientCounter = 0
                        // Pool de efectos ambientales disponibles
                        val availableEffects = mutableListOf<String>()

                        // Solo añadimos efectos cuya calidad gráfica actual lo permite
                        if (uiState.oceanQuality in fishQualityLevels && !isFishActive) {
                            availableEffects.add("fish")
                        }
                        if (uiState.oceanQuality in mistQualityLevels && !isMistActive) {
                            availableEffects.add("mist")
                        }
                        if (uiState.oceanQuality in godRaysQualityLevels && !isGodRaysActive) {
                            availableEffects.add("godRays")
                        }
                        if (uiState.oceanQuality in specularQualityLevels && !isSpecularActive) {
                            availableEffects.add("specular")
                        }
                        if (uiState.oceanQuality in gradientQualityLevels && !isGradientActive) {
                            availableEffects.add("gradient")
                        }
                        if (uiState.oceanQuality in bubblesQualityLevels && !isBubblesActive) {
                            availableEffects.add("bubbles")
                        }

                        // Si hay efectos disponibles, elegimos uno al azar, estos efectos no se supornen
                        if (availableEffects.isNotEmpty()) {
                            val chosenEffect = availableEffects.random()

                            // Activamos el efecto elegido
                            when (chosenEffect) {
                                "fish" -> {
                                    isFishActive = true
                                    isAmbientEffectActive = true
                                    activeEffectsCount++
                                    launch {
                                        // FADE IN (3 segundos)
                                        for (i in 0..30) {
                                            fishFadeAlpha = i / 30f
                                            delay(100)
                                        }
                                        // DURACIÓN ACTIVA
                                        delay(ambientEffectDuration - 5000) // Restamos 5s (3s fade in + 2s fade out)
                                        // FADE OUT (2 segundos)
                                        for (i in 30 downTo 0) {
                                            fishFadeAlpha = i / 30f
                                            delay(66)
                                        }
                                        isFishActive = false
                                        fishFadeAlpha = 0f
                                        isAmbientEffectActive = false
                                        activeEffectsCount--
                                    }
                                }
                                "mist" -> {
                                    isMistActive = true
                                    isAmbientEffectActive = true
                                    activeEffectsCount++
                                    launch {
                                        for (i in 0..30) {
                                            mistFadeAlpha = i / 30f
                                            delay(100)
                                        }
                                        delay(ambientEffectDuration - 5000)
                                        for (i in 30 downTo 0) {
                                            mistFadeAlpha = i / 30f
                                            delay(66)
                                        }
                                        isMistActive = false
                                        mistFadeAlpha = 0f
                                        isAmbientEffectActive = false
                                        activeEffectsCount--
                                    }
                                }
                                "godRays" -> {
                                    isGodRaysActive = true
                                    isAmbientEffectActive = true
                                    activeEffectsCount++
                                    launch {
                                        for (i in 0..30) {
                                            godRaysFadeAlpha = i / 30f
                                            delay(100)
                                        }
                                        delay(ambientEffectDuration - 5000)
                                        for (i in 30 downTo 0) {
                                            godRaysFadeAlpha = i / 30f
                                            delay(66)
                                        }
                                        isGodRaysActive = false
                                        godRaysFadeAlpha = 0f
                                        isAmbientEffectActive = false
                                        activeEffectsCount--
                                    }
                                }
                                "specular" -> {
                                    isSpecularActive = true
                                    isAmbientEffectActive = true
                                    activeEffectsCount++
                                    launch {
                                        for (i in 0..30) {
                                            specularFadeAlpha = i / 30f
                                            delay(100)
                                        }
                                        delay(ambientEffectDuration - 5000)
                                        for (i in 30 downTo 0) {
                                            specularFadeAlpha = i / 30f
                                            delay(66)
                                        }
                                        isSpecularActive = false
                                        specularFadeAlpha = 0f
                                        isAmbientEffectActive = false
                                        activeEffectsCount--
                                    }
                                }
                                "gradient" -> {
                                    isGradientActive = true
                                    isAmbientEffectActive = true
                                    activeEffectsCount++
                                    launch {
                                        for (i in 0..30) {
                                            gradientFadeAlpha = i / 30f
                                            delay(100)
                                        }
                                        delay(ambientEffectDuration - 5000)
                                        for (i in 30 downTo 0) {
                                            gradientFadeAlpha = i / 30f
                                            delay(66)
                                        }
                                        isGradientActive = false
                                        gradientFadeAlpha = 0f
                                        isAmbientEffectActive = false
                                        activeEffectsCount--
                                    }
                                }
                                "bubbles" -> {
                                    isBubblesActive = true
                                    isAmbientEffectActive = true
                                    activeEffectsCount++
                                    launch {
                                        for (i in 0..30) {
                                            bubblesFadeAlpha = i / 30f
                                            delay(100)
                                        }
                                        delay(ambientEffectDuration - 5000)
                                        for (i in 30 downTo 0) {
                                            bubblesFadeAlpha = i / 30f
                                            delay(66)
                                        }
                                        isBubblesActive = false
                                        bubblesFadeAlpha = 0f
                                        isAmbientEffectActive = false
                                        activeEffectsCount--
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ✅ Si la probabilidad FALLÓ, también resetear
                    // para que vuelva a intentar en el próximo ciclo
                    ambientCounter = 0
                }
            }

            // ====================================================================
            // CAPA CLIMÁTICA - Eventos dramáticos poco frecuentes
            // ====================================================================
            if (climaticCounter > climaticCycleLength && !isClimaticEffectActive) {
                // Comprobar si debemos activar un evento climático
                if (Random.nextFloat() < climaticProbability) {
                    // Si NO se permite overlap Y ya hay un efecto activo, saltamos
                    if (!allowEffectOverlap && activeEffectsCount > 0) {
                        // No hacer nada, esperamos al siguiente ciclo
                    } else {
                        climaticCounter = 0
                        val currentQuality = uiState.oceanQuality
                        val hasStorm = currentQuality in stormQualityLevels
                        val hasRain = currentQuality in rainQualityLevels

                        when {
                            // VERY_HIGH: ambos sincronizados (TU LÓGICA ACTUAL PRESERVADA)
                            currentQuality == "VERY_HIGH" && hasStorm && hasRain -> {
                                isStormActive = true
                                isRainActive = true
                                isClimaticEffectActive = true
                                activeEffectsCount++
                                launch {
                                    // FADE IN (3 segundos) - Ambos sincronizados
                                    for (i in 0..30) {
                                        stormFadeAlpha = i / 30f
                                        rainFadeAlpha = i / 30f
                                        delay(100)
                                    }
                                    // DURACIÓN ACTIVA
                                    delay(climaticEffectDuration - 5000) // Restamos 5s (3s fade in + 2s fade out)
                                    // FADE OUT (2 segundos)
                                    for (i in 30 downTo 0) {
                                        stormFadeAlpha = i / 30f
                                        rainFadeAlpha = i / 30f
                                        delay(66)
                                    }
                                    isStormActive = false
                                    isRainActive = false
                                    stormFadeAlpha = 0f
                                    rainFadeAlpha = 0f
                                    isClimaticEffectActive = false
                                    activeEffectsCount--
                                }
                            }
                            // Solo tormenta
                            hasStorm && !hasRain -> {
                                isStormActive = true
                                isClimaticEffectActive = true
                                activeEffectsCount++
                                launch {
                                    // FADE IN (3 segundos)
                                    for (i in 0..30) {
                                        stormFadeAlpha = i / 30f
                                        delay(100)
                                    }
                                    // DURACIÓN ACTIVA
                                    delay(climaticEffectDuration - 5000)
                                    // FADE OUT (2 segundos)
                                    for (i in 30 downTo 0) {
                                        stormFadeAlpha = i / 30f
                                        delay(66)
                                    }
                                    isStormActive = false
                                    stormFadeAlpha = 0f
                                    isClimaticEffectActive = false
                                    activeEffectsCount--
                                }
                            }
                            // Solo lluvia
                            hasRain && !hasStorm -> {
                                isRainActive = true
                                isClimaticEffectActive = true
                                activeEffectsCount++
                                launch {
                                    // FADE IN (3 segundos)
                                    for (i in 0..30) {
                                        rainFadeAlpha = i / 30f
                                        delay(100)
                                    }
                                    // DURACIÓN ACTIVA
                                    delay(climaticEffectDuration - 5000)
                                    // FADE OUT (2 segundos)
                                    for (i in 30 downTo 0) {
                                        rainFadeAlpha = i / 30f
                                        delay(66)
                                    }
                                    isRainActive = false
                                    rainFadeAlpha = 0f
                                    isClimaticEffectActive = false
                                    activeEffectsCount--
                                }
                            }
                        }
                    }
                } else {
                    // ✅ Si la probabilidad FALLÓ, también resetear
                    // para que vuelva a intentar en el próximo ciclo
                    climaticCounter = 0
                }
            }
        }
    }
    // ========================================================================

    /// Control de sonido de lluvia y música con AUTO-RE-ENCADENAMIENTO
    LaunchedEffect(isRainActive) {
        if (isRainActive) {
            viewModel.musicManager.duckVolume()

            rainPlayer.volume = 0f
            rainPlayer.play()

            // Fade in
            for (i in 0..20) {
                delay(100)
                rainPlayer.volume = i / 20f
            }
        } else {
            // Fade out
            for (i in 20 downTo 0) {
                delay(100)
                rainPlayer.volume = i / 20f
            }

            rainPlayer.pause()
            rainPlayer.seekTo(0)
            viewModel.musicManager.restoreVolume()
        }
    }

    val sheetState = rememberModalBottomSheetState()

    var showGemsTutorialDialog by remember { mutableStateOf(false) }

    var showGraphicsDialog by remember { mutableStateOf(false) }

    val currentLanguageCode = Locale.getDefault().language

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val bottomBarHeight = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 72.dp     // Zona crítica - más compacto
            screenWidth < 370.dp -> 76.dp     // Zona transición
            else -> 80.dp                     // Zona normal (actual)
        }
    }
    val iconSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 20.dp     // Zona crítica
            screenWidth < 370.dp -> 22.dp     // Zona transición
            else -> 24.dp                     // Zona normal (actual)
        }
    }
    val labelTextSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 11.sp     // Zona crítica
            screenWidth < 370.dp -> 12.sp     // Zona transición
            else -> 14.sp                     // Zona normal (actual)
        }
    }
    val badgeSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 6.dp      // Zona crítica
            screenWidth < 370.dp -> 7.dp      // Zona transición
            else -> 8.dp                      // Zona normal (actual)
        }
    }
    val fabPadding = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 12.dp     // Zona crítica
            screenWidth < 370.dp -> 14.dp     // Zona transición
            else -> 16.dp                     // Zona normal (actual)
        }
    }
    var headerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // Scaffold nos da la estructura de la pantalla principal
    key(currentLanguageCode) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background, // LightGray
                    tonalElevation = 3.dp // Le damos la sombra a la envoltura
                ) {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(bottomBarHeight)
                    ) {
                        // --- Elemento 1: Ranking (sin cambios) ---
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(Routes.RANKING_SCREEN) },
                            icon = {
                                Icon(
                                    Icons.Filled.Leaderboard,
                                    contentDescription = stringResource(R.string.bottom_nav_ranking),
                                    modifier = Modifier.size(iconSize)
                                )
                            },
                            label = { Text(stringResource(R.string.bottom_nav_ranking), fontSize = labelTextSize) }
                        )

                        // --- Elemento 2: Modo Libre (Lógica Corregida) ---
                        val isFreeModeEnabled = (uiState.playerLevelInfo?.level ?: 0) >= 5

                        NavigationBarItem(
                            enabled = isFreeModeEnabled,
                            selected = false,
                            onClick = {
                                // Este onClick solo se disparará si isFreeModeEnabled es true.
                                navController.navigate(Routes.FREE_MODE_SCREEN)
                            },
                            icon = {
                                // Si está deshabilitado, envolvemos el icono en un Box para capturar el clic.
                                if (!isFreeModeEnabled) {
                                    Box(
                                        modifier = Modifier.clickable(
                                            // Recordatorio: interactionSource y indication en null para
                                            // quitar el efecto de "ripple" al tocar.
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                // Usamos nuestro nuevo layout reutilizable
                                                val inflater = LayoutInflater.from(context)
                                                val layout = inflater.inflate(R.layout.custom_toast_layout, null)

                                                val textView = layout.findViewById<TextView>(R.id.toast_text)
                                                textView.text = context.getString(R.string.free_mode_unlock_toast)

                                                Toast(context).apply {
                                                    duration = Toast.LENGTH_SHORT
                                                    view = layout
                                                    show()
                                                }
                                            }
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.SwapHoriz,
                                            contentDescription = stringResource(R.string.bottom_nav_free_mode),
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                } else {
                                    // Si está habilitado, mostramos el icono normalmente.
                                    Icon(
                                        Icons.Filled.SwapHoriz,
                                        contentDescription = stringResource(R.string.bottom_nav_free_mode),
                                        modifier = Modifier.size(iconSize)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.bottom_nav_free_mode), fontSize = labelTextSize) }
                        )

                        // --- Elemento 3: Perfil (sin cambios) ---
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(Routes.PROFILE_SCREEN) },
                            icon = {
                                // --- INICIO DE LA MODIFICACIÓN ---
                                BadgedBox(
                                    badge = {
                                        // El Badge solo se muestra si hasProfileNotification es true.
                                        if (uiState.hasProfileNotification) {
                                            Badge(modifier = Modifier.size(badgeSize)) // El punto rojo por defecto
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.AccountCircle,
                                        contentDescription = stringResource(R.string.bottom_nav_profile)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.bottom_nav_profile), fontSize = labelTextSize) }
                        )
                    }
                }
            }
        ) { innerPadding -> // El contenido principal debe usar este padding

            // CAMBIO: Usar Box en lugar de Column para layering correcto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Ignoramos el padding superior para permitir que el Surface se pegue al borde.
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // CAPA 1: El mapa de fondo
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ocean_background_static),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        CircularProgressIndicator()
                    }
                } else {
                    InteractiveWorldMap(
                        countries = uiState.countries,
                        conqueredCountryIds = uiState.conqueredCountryIds,
                        dominatedCountryIds = uiState.dominatedCountryIds,
                        availableCountryIds = uiState.availableCountryIds,
                        oceanQuality = uiState.oceanQuality,
                        onCountryClick = { countryId ->
                            navController.navigate(
                                Routes.COUNTRY_SCREEN.replace("{countryId}", countryId)
                            )
                        },
                        modifier = Modifier.fillMaxSize(), // El mapa ocupa todo el espacio
                        shouldShowStorm = shouldShowStorm,
                        shouldShowRain = shouldShowRain,
                        shouldShowFish = shouldShowFish,
                        shouldShowMist = shouldShowMist,
                        shouldShowGodRays = shouldShowGodRays,
                        shouldShowSpecular = shouldShowSpecular,
                        shouldShowGradient = shouldShowGradient,
                        shouldShowBubbles = shouldShowBubbles,
                        stormFadeAlpha = stormFadeAlpha,
                        rainFadeAlpha = rainFadeAlpha,
                        fishFadeAlpha = fishFadeAlpha,
                        mistFadeAlpha = mistFadeAlpha,
                        godRaysFadeAlpha = godRaysFadeAlpha,
                        specularFadeAlpha = specularFadeAlpha,
                        gradientFadeAlpha = gradientFadeAlpha,
                        bubblesFadeAlpha = bubblesFadeAlpha,
                        onThunderSound = {
                            if (!thunderPlayer.isPlaying) {
                                thunderPlayer.seekTo(0)
                                thunderPlayer.start()
                            }
                        }
                    )
                }

                // CAPA 2: El título y PlayerlevelIndicator dentro un surface
                // Usamos un Surface como el contenedor principal del panel.
                Surface( // <-- NUEVO SURFACE QUE ACTÚA COMO FONDO SÓLIDO
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background // <-- Color de fondo (LightGray)
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                headerHeight = with(density) { coordinates.size.height.toDp() }
                            },
                        // Usamos el mismo color que el BottomAppBar para consistencia.
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 3.dp // Añade una pequeña sombra para dar profundidad
                    ) {
                        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                        val titlePadding = remember(screenWidth) {
                            when {
                                screenWidth < 340.dp -> 12.dp
                                screenWidth < 370.dp -> 14.dp
                                else -> 16.dp
                            }
                        }
                        val bottomPadding = remember(screenWidth) {
                            when {
                                screenWidth < 340.dp -> 6.dp
                                screenWidth < 370.dp -> 7.dp
                                else -> 8.dp
                            }
                        }
                        val spacerWidth = remember(screenWidth) {
                            when {
                                screenWidth < 340.dp -> 8.dp
                                screenWidth < 370.dp -> 10.dp
                                else -> 10.dp
                            }
                        }
                        // Usamos una Columna para apilar el título y el indicador verticalmente.
                        Column(
                            modifier = Modifier.padding(bottom = bottomPadding), // Un pequeño padding inferior
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. El Título
                            Text(
                                stringResource(R.string.map_title),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(titlePadding),
                                textAlign = TextAlign.Center
                            )

                            // Envolvemos los indicadores en una Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = titlePadding),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Indicador de Nivel a la izquierda
                                Box(modifier = Modifier.weight(1f)) {
                                    uiState.playerLevelInfo?.let { levelInfo ->
                                        PlayerLevelIndicator(
                                            levelInfo = levelInfo,
                                            boostCount = uiState.unassignedPcBoosts
                                        )
                                    }
                                }
                                // Espacio entre ellos (PlayerlevelIndicator y GEmsIndicator)
                                Spacer(modifier = Modifier.width(spacerWidth))

                                // Indicador de Gemas a la derecha
                                GemsIndicator(
                                    gems = uiState.gems,
                                    onClick = { showGemsTutorialDialog = true }
                                )
                            }
                        }
                    }
                }

                if (!uiState.isLoading) {
                    val context = LocalContext.current // Necesario para el Toast

                    // Icono de Olas para Ajustes Gráficos
                    IconButton(
                        onClick = { showGraphicsDialog = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = headerHeight + 8.dp, start = 8.dp)
                    ) {
                        // Animación de pulso para VERY_HIGH y HIGH
                        val shouldPulse = uiState.oceanQuality in listOf("VERY_HIGH", "HIGH")
                        val infiniteTransition = rememberInfiniteTransition(label = "wave_pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_scale"
                        )
                        // --- INICIO DEL CAMBIO (PASO 2) ---
                        Box(contentAlignment = Alignment.Center) {
                            val iconColor = when (uiState.oceanQuality) {
                                "VERY_HIGH" -> DarkGoldAccent
                                "HIGH" -> Color.White
                                "MEDIUM" -> Color.White
                                "LOW" -> Color.Gray.copy(alpha = 0.8f)
                                else -> Color.White
                            }
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = stringResource(R.string.cd_graphics_quality_indicator),
                                modifier = Modifier.graphicsLayer {
                                    scaleX = if (shouldPulse) pulseScale else 1f
                                    scaleY = if (shouldPulse) pulseScale else 1f
                                },
                                tint = iconColor
                            )
                            // Dibuja línea diagonal según calidad
                            when (uiState.oceanQuality) {
                                "MEDIUM" -> {
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawLine(
                                            color = DarkGoldAccent.copy(alpha = 0.8f),
                                            start = Offset(x = size.width * 0.2f, y = size.height * 0.2f),
                                            end = Offset(x = size.width * 0.8f, y = size.height * 0.8f),
                                            strokeWidth = 3.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                                "LOW" -> {
                                    Canvas(modifier = Modifier.matchParentSize()) {
                                        drawLine(
                                            color = Color.Red.copy(alpha = 0.8f),
                                            start = Offset(x = size.width * 0.2f, y = size.height * 0.2f),
                                            end = Offset(x = size.width * 0.8f, y = size.height * 0.8f),
                                            strokeWidth = 3.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FAB de Gemas (Izquierda)
                    FloatingActionButton(
                        onClick = {
                            if (uiState.isRewardFeatureUnlocked) {
                                // CASO 1: La función está DESBLOQUEADA
                                if (!uiState.isRewardCooldownActive) {
                                    // Y NO está en cooldown -> Muestra el diálogo
                                    viewModel.onShowRewardDialog()
                                } else {
                                    // Y SÍ está en cooldown -> Muestra el Toast de recarga
                                    val inflater = LayoutInflater.from(context)
                                    val layout = inflater.inflate(R.layout.custom_toast_layout, null)
                                    val textView = layout.findViewById<TextView>(R.id.toast_text)
                                    textView.text = context.getString(R.string.reward_cooldown_toast) // <-- Nuevo string

                                    Toast(context).apply {
                                        duration = Toast.LENGTH_SHORT
                                        view = layout
                                        show()
                                    }
                                }
                            } else {
                                // CASO 2: La función está BLOQUEADA -> Muestra el Toast de instrucción
                                val inflater = LayoutInflater.from(context)
                                val layout = inflater.inflate(R.layout.custom_toast_layout, null)
                                val textView = layout.findViewById<TextView>(R.id.toast_text)
                                textView.text = context.getString(R.string.reward_unlock_toast)

                                Toast(context).apply {
                                    duration = Toast.LENGTH_LONG
                                    view = layout
                                    show()
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(fabPadding),
                        shape = FloatingActionButtonDefaults.shape,
                        elevation = FloatingActionButtonDefaults.elevation(),
                        containerColor = if (uiState.isRewardFeatureUnlocked && !uiState.isRewardCooldownActive) {
                            // Estado ACTIVO y listo
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            // Estado BLOQUEADO o en COOLDOWN
                            Color(0xFF6E6E6E) // Gris oscuro sólido
                        }
                    ) {
                        if (uiState.isRewardFeatureUnlocked && uiState.isRewardCooldownActive) {
                            // Desbloqueado PERO en Cooldown: Muestra el timer
                            Text(
                                text = "${uiState.rewardCooldownSeconds}s",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBDBDBD)
                            )
                        } else {
                            // Desbloqueado y listo, O permanentemente bloqueado: Muestra el icono y el "+"
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp), // Ancho reducido
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    // El color del texto también refleja el estado
                                    color = if (uiState.isRewardFeatureUnlocked) {
                                        DeepNavy
                                    } else {
                                        Color(0xFFBDBDBD) // Gris claro si está bloqueado
                                    }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = GemIcon, // Tu ícono original
                                    contentDescription = stringResource(R.string.cd_get_gem_reward),
                                    modifier = Modifier.size(24.dp),
                                    tint = if (uiState.isRewardFeatureUnlocked) {
                                        DeepNavy
                                    } else {
                                        Color(0xFFBDBDBD) // Tinta el ícono de gris si está bloqueado
                                    }
                                )
                            }
                        }
                    }

                    // FAB de Expediciones (avión) (Derecha)
                    if (uiState.expeditionAvailable) {
                        FloatingActionButton(
                            onClick = { viewModel.requestExpeditionDialog() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd) // Alineado a la esquina inferior derecha del Box
                                .padding(fabPadding),       // Padding simétrico
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flight,
                                contentDescription = stringResource(R.string.cd_start_expedition)
                            )
                        }
                    }
                }
            }
        }
    }
    if (uiState.showRewardDialog) {
        val activity = LocalContext.current as Activity
        AppAlertDialog(
            onDismissRequest = { viewModel.onDismissRewardDialog() },
            title = { DialogTitle(text = stringResource(R.string.reward_dialog_title)) }, // Nuevo string
            text = { DialogText(text = stringResource(R.string.reward_dialog_text, 5)) }, // Nuevo string, pasándole el 5
            confirmButton = {
                TextButton(onClick = { viewModel.onClaimRewardConfirmed(activity) }) {
                    DialogButtonText(text = stringResource(R.string.reward_dialog_button_watch)) // Nuevo string
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissRewardDialog() }) {
                    DialogButtonText(text = stringResource(R.string.reward_dialog_button_cancel)) // Nuevo string
                }
            }
        )
    }
    // SEGUNDO DIÁLOGO COMPLEJO - VERSIÓN MEJORADA CON BOTONES PERSONALIZADOS
    if (uiState.showExpeditionDialog) {
        val buttonTextColor = getButtonTextColor()

        AppExpeditionAlertDialog(
            onDismissRequest = { viewModel.dismissExpeditionDialog() },
            title = { DialogTitle(text = stringResource(R.string.expedition_dialog_title)) },
            text = { DialogText(text = stringResource(R.string.expedition_dialog_text)) },
            expeditionButtons = { backgroundColor, textColor ->
                uiState.availableExpeditions.forEach { (continentId, continentName) ->
                    Button(
                        onClick = {
                            viewModel.onExpeditionContinentSelected(continentId)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColor
                        )
                    ) {
                        Text(
                            text = continentName,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissExpeditionDialog() }) {
                    DialogButtonText(text = stringResource(R.string.expedition_dialog_button_later))
                }
            }
        )
    }

    if (uiState.showWelcomeDialog) {
        AppAlertDialog(
            onDismissRequest = { viewModel.welcomeDialogShown() },
            title = stringResource(R.string.welcome_dialog_title),
            text = stringResource(R.string.welcome_dialog_message, uiState.firstCountryName),
            confirmButtonText = stringResource(R.string.dialog_button_ok)
        )
    }

    if (uiState.showFreeModeUnlockedDialog) {
        AppAlertDialog(
            onDismissRequest = { viewModel.freeModeTutorialShown() },
            title = stringResource(R.string.free_mode_unlocked_title),
            text = stringResource(R.string.free_mode_unlocked_message),
            confirmButtonText = stringResource(R.string.dialog_button_awesome)
        )
    }

    if (uiState.showDominationRewardsSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dominationTutorialShown() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                // Contenido del Bottom Sheet
                DominationRewardsContent(
                    onDismiss = {
                        // Podemos añadir una corrutina para cerrar el sheet con animación
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.dominationTutorialShown()
                            }
                        }
                    }
                )
            }
        }
    }
    if (showGemsTutorialDialog) {
        AppAlertDialog(
            onDismissRequest = { showGemsTutorialDialog = false },
            title = stringResource(R.string.gems_tutorial_title),
            text = stringResource(R.string.gems_tutorial_message),
            confirmButtonText = stringResource(R.string.dialog_button_ok)
        )
    }
    if (showGraphicsDialog) {
        // Obtenemos el nombre corto del nivel de calidad actual
        val qualityName = qualityCodeToName(quality = uiState.oceanQuality)

        AppAlertDialog(
            onDismissRequest = { showGraphicsDialog = false },
            title = { DialogTitle(text = stringResource(R.string.graphics_indicator_dialog_title)) },
            // Usamos HtmlCompat para renderizar la etiqueta <b> que pusimos en el string
            text = { DialogText(text = htmlToString(stringResource(R.string.graphics_indicator_dialog_message, qualityName))) },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate(Routes.SETTINGS_SCREEN)
                    showGraphicsDialog = false
                }) {
                    DialogButtonText(text = stringResource(R.string.graphics_indicator_dialog_button_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showGraphicsDialog = false }) {
                    // Reutilizamos el string "Entendido" que ya tienes
                    DialogButtonText(text = stringResource(R.string.dialog_button_ok))
                }
            }
        )
    }
}

// Crea este nuevo Composable separado para el contenido del Bottom Sheet.
@Composable
private fun DominationRewardsContent(onDismiss: () -> Unit) {
    // Definimos los valores de recompensa (para no hardcodearlos)
    val xpBonus = 20000
    val pcBonus = 5000

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.domination_rewards_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Recompensa 1: XP
        RewardRow(
            icon = Icons.Default.EmojiEvents,
            text = stringResource(R.string.domination_reward_xp, xpBonus)
        )
        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // --- APAGADO POR AHORA ---
        /* <-- Comienza el comentario aquí
        // Recompensa 2: Categoría Secreta
        RewardRow(
            icon = Icons.Default.LockOpen,
            text = stringResource(R.string.domination_reward_category)
        )
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        */
        // --- FIN DEL APAGADO ---


        // Recompensa 3: PC Boost
        RewardRow(
            icon = Icons.Default.RocketLaunch, // O algún otro icono bueno
            text = stringResource(R.string.domination_reward_boost, pcBonus)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDismiss) {
            Text(stringResource(R.string.dialog_button_awesome))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Crea este Composable de utilidad para las filas
@Composable
private fun RewardRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Convierte un string con etiquetas HTML simples (como <b>) a un AnnotatedString.
 */
@Composable
private fun htmlToString(html: String): String {
    return remember(html) {
        // Usamos la librería de AndroidX para parsear el HTML de forma segura
        androidx.core.text.HtmlCompat.fromHtml(html, androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    }
}

/**
 * Convierte el código de calidad (ej. "HIGH") a su nombre corto traducible (ej. "Alta").
 */
@Composable
private fun qualityCodeToName(quality: String): String {
    return when (quality) {
        "VERY_HIGH" -> stringResource(R.string.settings_quality_tier_very_high_short)
        "HIGH" -> stringResource(R.string.settings_quality_tier_high_short)
        "MEDIUM" -> stringResource(R.string.settings_quality_tier_medium_short)
        "LOW" -> stringResource(R.string.settings_quality_tier_low_short)
        else -> quality // Fallback
    }
}

@Composable
fun InteractiveWorldMap(
    countries: List<Country>,
    conqueredCountryIds: List<String>,
    dominatedCountryIds: List<String>,
    availableCountryIds: List<String>,
    oceanQuality: String,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    shouldShowStorm: Boolean = false,
    shouldShowRain: Boolean = false,
    shouldShowFish: Boolean = false,
    shouldShowMist: Boolean = false,
    shouldShowGodRays: Boolean = false,
    shouldShowSpecular: Boolean = false,
    shouldShowGradient: Boolean = false,
    shouldShowBubbles: Boolean = false,
    stormFadeAlpha: Float = 1f,
    rainFadeAlpha: Float = 1f,
    fishFadeAlpha: Float = 1f,
    mistFadeAlpha: Float = 1f,
    godRaysFadeAlpha: Float = 1f,
    specularFadeAlpha: Float = 1f,
    gradientFadeAlpha: Float = 1f,
    bubblesFadeAlpha: Float = 1f,
    onThunderSound: () -> Unit = {}
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalOffsetFactor = remember(screenWidth) {
        when {
            screenWidth < 370.dp -> 0.15f // Menos desplazamiento en pantallas angostas
            screenWidth < 400.dp -> 0.15f // Intermedio
            else -> 0.15f                 // El valor original para pantallas anchas
        }
    }
    val verticalOffsetFactor = remember(screenWidth) {
        when {
            screenWidth < 370.dp -> 0.002f // Menos desplazamiento en pantallas angostas
            screenWidth < 400.dp -> 0.004f // <-- VALOR INTERMEDIO AÑADIDO
            else -> 0.01f                  // El valor original para pantallas anchas
        }
    }
    val maxOffsetFactorX = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 0.065f     // Zona crítica - más restrictivo
            screenWidth < 370.dp -> 0.06f     // Zona transición
            else -> 0.05f                     // Zona normal (actual)
        }
    }
    val maxOffsetFactorY = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 0.030f     // Zona crítica - más restrictivo
            screenWidth < 370.dp -> 0.04f     // Zona transición
            else -> 0.02f                     // Zona normal (actual)
        }
    }
    // Estados para zoom y pan
    var scale by remember { mutableStateOf(1.6f) } // 1.6f es la escala para agrandar por defecto el mapa
    var offset by remember { mutableStateOf(Offset.Zero) }
    // Estados para la animación del océano
    var waveTime by remember { mutableStateOf(0f) }
    var isAnimationActive by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> isAnimationActive = false
                Lifecycle.Event.ON_RESUME -> isAnimationActive = true
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            isAnimationActive = false // Detener completamente al salir
        }
    }

    var canvasWidth by remember { mutableStateOf(1080f) }
    var canvasHeight by remember { mutableStateOf(1812f) }

    // Estados para el SVG y su procesamiento
    var svgDocument by remember { mutableStateOf<SVG?>(null) }
    var processedSvgBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // NUEVO: Estado para controlar la inicialización completa
    var isMapReady by remember { mutableStateOf(false) }
    var isInitialProcessing by remember { mutableStateOf(true) }

    // Cache de paths y colores
    var pathColorMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var countryPaths by remember { mutableStateOf<Map<String, android.graphics.Path>>(emptyMap()) }

    var allParsedPaths by remember { mutableStateOf<Map<String, android.graphics.Path>>(emptyMap()) }
    var allPathCoordinates by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var pulseAlpha by remember { mutableStateOf(0.7f) }

    var lastToastTime by remember { mutableStateOf(0L) }

    // ✅ NUEVO: Estado para el path de contorno
    var worldOutlinePath by remember { mutableStateOf<android.graphics.Path?>(null) }

    val context = LocalContext.current
    val density = LocalDensity.current

    // Color Grading Dinámico: Color Grading según hora del día
    val currentHour = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val timeBasedTint = remember(currentHour) {
        when (currentHour) {
            in 6..11 -> Color(0xFFFFE5B4).copy(alpha = 0.15f)   // Mañana: Tinte cálido/amarillo - Color(0xFFFFE5B4).copy(alpha = 0.15f)
            in 12..17 -> Color(0xFFFFD700).copy(alpha = 0.10f)  // Tarde: Tinte dorado suave - Color(0xFFFFD700).copy(alpha = 0.10f)
            in 18..19 -> Color(0xFFFF8C42).copy(alpha = 0.25f)  // Atardecer: Tinte naranja - Color(0xFFFF8C42).copy(alpha = 0.20f)
            else -> Color(0xFF1A4D7A).copy(alpha = 0.25f)       // Noche: Tinte azul oscuro
        }
    }
    // Fin Color Grading Dinámico

    // Definir colores - Se quedan solo por la nomenclatura ya que los colores ahora se definen con texturas
    val dominatedColor = DarkGoldAccent.toArgb() // PAÍSES DOMINADOS
    val conqueredColor = CyanAccent.toArgb() // PAÍSES CONQUISTADOS
    val availableColor = LightGray.toArgb() // PAÍSES DISPONIBLES
    val defaultColor = Color(0xFF6B7280).toArgb() // Gris neutro

    // Función para extraer coordenadas de paths del SVG
    suspend fun extractPathCoordinates(
        context: Context,
        countryIds: Set<String>
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val pathMap = mutableMapOf<String, String>()

        try {
            val inputStream = context.assets.open("world-map.min.svg")
            val svgContent = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            countryIds.forEach { countryId ->
                var pathData: String? = null

                // ESTRATEGIA 1: Buscar <path id="xx" d="...">
                val directPathPattern = """<path[^>]*id=["']$countryId["'][^>]*d=["']([^"']*)["']""".toRegex()
                directPathPattern.find(svgContent)?.let {
                    pathData = it.groupValues[1]
                }

                // ESTRATEGIA 2: Si no se encontró, buscar <g id="xx">...<path d="...">
                if (pathData == null) {
                    val groupPattern = """<g[^>]*id=["']$countryId["'][^>]*>(.*?)</g>""".toRegex(RegexOption.DOT_MATCHES_ALL)
                    groupPattern.find(svgContent)?.let { groupMatch ->
                        val groupContent = groupMatch.groupValues[1]
                        val pathsInGroupPattern = """<path[^>]*d=["']([^"']*)["']""".toRegex()
                        val allPaths = pathsInGroupPattern.findAll(groupContent).map { it.groupValues[1] }.toList()

                        if (allPaths.isNotEmpty()) {
                            pathData = allPaths.joinToString(" ")
                        }
                    }
                }

                pathData?.let { data ->
                    pathMap[countryId] = data
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("InteractiveWorldMap", "Error extrayendo paths", e)
        }

        pathMap
    }

    // Función para obtener TODOS los IDs de países del SVG
    suspend fun extractAllCountryIdsFromSVG(context: Context): Set<String> = withContext(Dispatchers.IO) {
        val countryIds = mutableSetOf<String>()

        try {
            val inputStream = context.assets.open("world-map.min.svg")
            val svgContent = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            // Buscar todos los id="..." en el SVG
            val idPattern = """id=["']([a-zA-Z]{2})["']""".toRegex()
            idPattern.findAll(svgContent).forEach { match ->
                countryIds.add(match.groupValues[1])
            }
        } catch (e: Exception) {
            android.util.Log.e("InteractiveWorldMap", "Error extrayendo todos los IDs", e)
        }

        countryIds
    }

    // Función para convertir path data SVG a Path de Android
    fun parsePathData(pathData: String, path: android.graphics.Path) {
        try {
            androidx.core.graphics.PathParser.createPathFromPathData(pathData)?.let { parsedPath ->
                path.set(parsedPath)
            }
        } catch (e: Exception) {
            android.util.Log.e("InteractiveWorldMap", "Error parseando path data: ${pathData.take(50)}", e)
            path.addRect(0f, 0f, 100f, 100f, android.graphics.Path.Direction.CW)
        }
    }

    // MODIFICADO: Carga inicial del SVG con mejor control de estado
    LaunchedEffect(Unit) {
        try {
            isInitialProcessing = true
            val inputStream = context.assets.open("world-map.min.svg")
            val svg = SVG.getFromInputStream(inputStream)
            svgDocument = svg
        } catch (e: IOException) {
            android.util.Log.e("InteractiveWorldMap", "Error cargando SVG", e)
            isInitialProcessing = false
        }
    }

    // ✅ NUEVO: Parsear el path de contorno una sola vez
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Leer el path desde assets
                val inputStream = context.assets.open("world_outline_path.txt")
                val pathData = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                // Parsear el path
                val path = android.graphics.Path()
                parsePathData(pathData, path)
                worldOutlinePath = path
                Log.d("InteractiveWorldMap", "Path de contorno parseado exitosamente")
            } catch (e: Exception) {
                Log.e("InteractiveWorldMap", "Error cargando/parseando world outline path", e)
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Extraer todos los IDs una sola vez
                val allIds = extractAllCountryIdsFromSVG(context)

                // 2. Extraer todas las coordenadas una sola vez
                val coordinates = extractPathCoordinates(context, allIds)
                allPathCoordinates = coordinates

                // 3. Parsear TODOS los paths una sola vez
                val parsed = mutableMapOf<String, android.graphics.Path>()
                coordinates.forEach { (countryId, pathData) ->
                    val path = android.graphics.Path()
                    parsePathData(pathData, path)
                    parsed[countryId] = path
                }
                allParsedPaths = parsed

                Log.d("InteractiveWorldMap", "Paths parseados: ${parsed.size}")
            } catch (e: Exception) {
                Log.e("InteractiveWorldMap", "Error parseando paths iniciales", e)
            }
        }
    }

    // Recalcular colores cuando cambien las listas
    LaunchedEffect(dominatedCountryIds, conqueredCountryIds, availableCountryIds, countries) {
        if (countries.isEmpty()) return@LaunchedEffect

        val newColorMap = mutableMapOf<String, Int>()

        // Usamos una lógica de prioridades para asignar el color correcto
        countries.forEach { country ->
            val countryId = country.countryId
            newColorMap[countryId] = when {
                dominatedCountryIds.contains(countryId) -> dominatedColor
                conqueredCountryIds.contains(countryId) -> conqueredColor
                availableCountryIds.contains(countryId) -> availableColor
                else -> defaultColor
            }
        }

        pathColorMap = newColorMap
    }

    // REEMPLAZAR desde línea 443 hasta línea 544
    LaunchedEffect(svgDocument, pathColorMap, allParsedPaths) {
        svgDocument?.let { svg ->
            if (pathColorMap.isNotEmpty() && allParsedPaths.isNotEmpty()) {  // AÑADIDA CONDICIÓN
                try {
                    // YA NO NECESITAS ESTAS LÍNEAS (ELIMINAR):
                    // val allCountryIds = extractAllCountryIdsFromSVG(context)
                    // val pathCoordinates = extractPathCoordinates(context, allCountryIds)

                    // USAR allParsedPaths en lugar de volver a parsear
                    if (!isActive) return@LaunchedEffect

                    val width = 1200
                    val height = (width * svg.documentHeight / svg.documentWidth).toInt()

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT)

                    svg.setDocumentViewBox(0f, 0f, svg.documentWidth, svg.documentHeight)
                    svg.renderToCanvas(canvas)

                    if (!isActive) return@LaunchedEffect

                    // Cargar texturas (esto queda igual)
                    val textureAvailable = BitmapFactory.decodeResource(context.resources, R.drawable.texture_available)
                    val textureConquered = BitmapFactory.decodeResource(context.resources, R.drawable.texture_conquered)
                    val textureDominated = BitmapFactory.decodeResource(context.resources, R.drawable.texture_dominated)
                    val textureLocked = BitmapFactory.decodeResource(context.resources, R.drawable.texture_locked)
                    val texturePaper = BitmapFactory.decodeResource(context.resources, R.drawable.old_paper_texture)

                    val colorMap = mapOf(
                        TextureType.AVAILABLE to availableColor,
                        TextureType.CONQUERED to conqueredColor,
                        TextureType.DOMINATED to dominatedColor,
                        TextureType.LOCKED to defaultColor
                    )

                    val availablePaint = createTexturePaint(textureAvailable, TextureType.AVAILABLE, colorMap)
                    val conqueredPaint = createTexturePaint(textureConquered, TextureType.CONQUERED, colorMap)
                    val dominatedPaint = createTexturePaint(textureDominated, TextureType.DOMINATED, colorMap)
                    val lockedPaint = createTexturePaint(textureLocked, TextureType.LOCKED, colorMap)
                    val paperPaint = createTexturePaint(texturePaper, TextureType.PAPER, colorMap)

                    val newCountryPaths = mutableMapOf<String, android.graphics.Path>()

                    // CAMBIO PRINCIPAL: Usar allParsedPaths en lugar de parsear de nuevo
                    pathColorMap.forEach { (countryId, color) ->
                        allParsedPaths[countryId]?.let { path ->  // USAR PATH YA PARSEADO
                            val paintToUse = when (color) {
                                dominatedColor -> dominatedPaint
                                conqueredColor -> conqueredPaint
                                availableColor -> availablePaint
                                else -> lockedPaint
                            }

                            newCountryPaths[countryId] = path
                            canvas.drawPath(path, paintToUse)

                            if (color == defaultColor) {
                                val crackPaint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 1f
                                    setColor(android.graphics.Color.argb(128, 255, 255, 255))
                                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 6f), 0f)
                                }
                                canvas.drawPath(path, crackPaint)
                            }
                        }
                    }

                    // Dibujar países decorativos (papel)
                    allParsedPaths.keys.forEach { countryId ->  // USAR allParsedPaths
                        if (!pathColorMap.containsKey(countryId)) {
                            allParsedPaths[countryId]?.let { path ->  // USAR PATH YA PARSEADO
                                canvas.drawPath(path, paperPaint)
                            }
                        }
                    }

                    if (isActive) {
                        processedSvgBitmap = bitmap
                        countryPaths = newCountryPaths

                        if (isInitialProcessing) {
                            isMapReady = true
                            isInitialProcessing = false
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("InteractiveWorldMap", "Error procesando SVG con colores", e)
                    isInitialProcessing = false
                }
            }
        }
    }

    // Función para detectar qué país fue tocado
    fun detectCountryFromTap(
        tapOffset: Offset,
        svgBitmap: Bitmap,
        pathCoordinates: Map<String, String>,
        canvasSize: androidx.compose.ui.geometry.Size,
        hOffset: Float,
        vOffset: Float,
        currentScale: Float,
        currentOffset: Offset
    ): String? {
        return try {
            val bitmapAspectRatio = svgBitmap.width.toFloat() / svgBitmap.height.toFloat()
            val canvasAspectRatio = canvasSize.width / canvasSize.height

            val baseFitScaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                canvasSize.width / svgBitmap.width
            } else {
                canvasSize.height / svgBitmap.height
            }

            val scaledWidth = svgBitmap.width * baseFitScaleFactor
            val scaledHeight = svgBitmap.height * baseFitScaleFactor

            val centerX = canvasSize.width / 2f
            val centerY = canvasSize.height / 2f

            // Usar los MISMOS valores que tu Canvas
            val baseLeft = centerX - (scaledWidth / 2f) + (canvasSize.width * hOffset)
            val baseTop = centerY - (scaledHeight / 2f) + (canvasSize.height * vOffset)

            // Aplicar transformaciones inversas
            val transformedX = (tapOffset.x - centerX) / scale + centerX - offset.x
            val transformedY = (tapOffset.y - centerY) / scale + centerY - offset.y

            val svgX = (transformedX - baseLeft) / baseFitScaleFactor
            val svgY = (transformedY - baseTop) / baseFitScaleFactor

            if (svgX < 0 || svgX >= svgBitmap.width || svgY < 0 || svgY >= svgBitmap.height) {
                return null
            }

            // Ahora iteramos sobre el mapa de coordenadas que nos pasan
            for ((countryId, pathData) in pathCoordinates) {
                val path = android.graphics.Path()
                // parsePathData es una función que ya tienes y es rápida
                parsePathData(pathData, path)
                val region = android.graphics.Region()
                val clipRegion = android.graphics.Region(0, 0, svgBitmap.width, svgBitmap.height)
                region.setPath(path, clipRegion)

                if (region.contains(svgX.toInt(), svgY.toInt())) {
                    return countryId
                }
            }
            return null

        } catch (e: Exception) {
            android.util.Log.e("HitTesting", "Error en detección de tap", e)
            null
        }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1.1f, 10f) //0.8f y 5f; son el minimo y maximo para hacer zoom

        // Usar valores fijos basados en una pantalla promedio (más simple)
        val maxOffsetX = canvasWidth * maxOffsetFactorX * newScale   // 30% para hacer drag en horizontal
        val maxOffsetY = canvasHeight * maxOffsetFactorY * newScale  // 12% para hacer drag en vertical

        val newOffset = (offset + panChange).copy(
            x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
            y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
        )
        scale = newScale
        offset = newOffset
    }

    // Nueva estructura con capas separadas
    Box(modifier = modifier.fillMaxSize()) {

        // CAPA 1: Fondos y Efectos del Océano (Renderizado Condicional)
        // Primero, creamos un composable reutilizable para el fondo estático.
        val staticOceanBackground = @Composable {
            Image(
                painter = painterResource(id = R.drawable.ocean_background_static),
                contentDescription = "Ocean Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // FONDOS BASE (según calidad gráfica)
        when (oceanQuality) {
            "VERY_HIGH" -> {
                VideoBackground(videoResId = R.raw.ocean_background, modifier = Modifier.fillMaxSize())
            }
            "HIGH" -> {
                VideoBackground(videoResId = R.raw.ocean_background, modifier = Modifier.fillMaxSize())
            }
            "MEDIUM" -> {
                staticOceanBackground()
            }
            "LOW" -> {
                staticOceanBackground()
            }
        }

        // EFECTOS AMBIENTALES DINÁMICOS (controlados por el scheduler)
        if (shouldShowFish) {
            OceanFishEffect(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = fishFadeAlpha
            )
        }

        if (shouldShowMist) {
            OceanMistEffect(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = mistFadeAlpha
            )
        }

        if (shouldShowGodRays) {
            OceanGodRaysEffect(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = godRaysFadeAlpha
            )
        }

        if (shouldShowSpecular) {
            OceanSpecularEffect(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = specularFadeAlpha
            )
        }

        if (shouldShowGradient) {
            OceanGradientOverlay(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = gradientFadeAlpha
            )
        }

        if (shouldShowBubbles) {
            OceanBubblesEffect(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = bubblesFadeAlpha
            )
        }

        // EFECTOS CLIMÁTICOS (lógica preservada)
        if (shouldShowStorm) {
            StormEffect(
                modifier = Modifier.fillMaxSize(),
                onThunderSound = onThunderSound,
                fadeAlpha = stormFadeAlpha
            )
        }
        if (shouldShowRain) {
            RainEffect(
                modifier = Modifier.fillMaxSize(),
                fadeAlpha = rainFadeAlpha
            )
        }

        // EFECTOS PERMANENTES (según calidad)
        if (oceanQuality in listOf("VERY_HIGH", "HIGH", "MEDIUM")) {
            // Color Grading Dinámico
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(color = timeBasedTint)
            }
            // Vignette Dinámico
            OceanVignette(modifier = Modifier.fillMaxSize())
        }

        // CAPA 2: Mapa en canvas original
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformableState)
                .pointerInput(allPathCoordinates) { // El listener se actualiza si los paths cambian
                    detectTapGestures { tapOffset ->
                        // Solo procedemos si los paths ya se han calculado
                        if (isMapReady && allPathCoordinates.isNotEmpty()) {
                            processedSvgBitmap?.let { bitmap ->
                                // NO se recalcula nada aquí, solo se usa.
                                val tappedCountryId = detectCountryFromTap(
                                    tapOffset = tapOffset,
                                    svgBitmap = bitmap,
                                    pathCoordinates = allPathCoordinates, // <-- Usa la variable de estado
                                    size.toSize(),
                                    hOffset = horizontalOffsetFactor,
                                    vOffset = verticalOffsetFactor,
                                    currentScale = scale,
                                    currentOffset = offset
                                )

                                if (tappedCountryId != null) {
                                    val isGameCountry = pathColorMap.containsKey(tappedCountryId)

                                    if (isGameCountry) {
                                        // Es uno de los 32 países del juego
                                        val isInteractable = conqueredCountryIds.contains(tappedCountryId) ||
                                                availableCountryIds.contains(tappedCountryId)
                                        if (isInteractable) {
                                            onCountryClick(tappedCountryId)
                                        } else {
                                            // País del juego, pero bloqueado
                                            val currentTime = System.currentTimeMillis()
                                            if ((currentTime - lastToastTime) > 3000L) { // Cooldown de 3 seg
                                                lastToastTime = currentTime
                                                // Muestra Toast de "Conquista vecinos"
                                                showCustomToast(context, context.getString(R.string.map_toast_conquer_neighbors))
                                            }
                                        }
                                    } else {
                                        // País inactivo (no está en el juego)
                                        val currentTime = System.currentTimeMillis()
                                        if ((currentTime - lastToastTime) > 3000L) { // Cooldown de 3 seg
                                            lastToastTime = currentTime
                                            // Muestra Toast de "Próximamente"
                                            showCustomToast(context, context.getString(R.string.map_toast_coming_soon))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            when {
                isMapReady && processedSvgBitmap != null -> {
                    val bitmap = processedSvgBitmap!!

                    // Calcular dimensiones base una sola vez
                    val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    val canvasAspectRatio = size.width / size.height

                    val baseFitScaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                        size.width / bitmap.width
                    } else {
                        size.height / bitmap.height
                    }

                    // Calcular posición base igual que tu código original
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // Aplicar transformaciones usando Canvas transforms (hardware-accelerated)
                    drawContext.transform.scale(
                        scaleX = scale,
                        scaleY = scale,
                        pivot = Offset(centerX, centerY)
                    )

                    drawContext.transform.translate(
                        left = offset.x,
                        top = offset.y
                    )

                    // Posición base del bitmap usando TU lógica original
                    val scaledWidth = bitmap.width * baseFitScaleFactor
                    val scaledHeight = bitmap.height * baseFitScaleFactor
                    val baseLeft = centerX - (scaledWidth / 2f) + (size.width * horizontalOffsetFactor)
                    val baseTop = centerY - (scaledHeight / 2f) + (size.height * verticalOffsetFactor)

                    // Usar FilterQuality optimizada
                    val quality = if (transformableState.isTransformInProgress) {
                        FilterQuality.Low  // Mejor que None, pero más rápido que High
                    } else {
                        FilterQuality.High
                    }

                    drawImage(
                        image = bitmap.asImageBitmap(),
                        dstOffset = IntOffset(baseLeft.toInt(), baseTop.toInt()),
                        dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt()),
                        filterQuality = quality
                    )
                    // ✅ NUEVO: DIBUJAR SOMBRA DEL CONTORNO
                    worldOutlinePath?.let { outlinePath ->
                        // Crear una matriz para transformar el path
                        val matrix = android.graphics.Matrix()

                        // Escalar el path al mismo tamaño que el bitmap
                        matrix.setScale(baseFitScaleFactor, baseFitScaleFactor)

                        // Trasladar a la misma posición que el bitmap
                        matrix.postTranslate(baseLeft, baseTop)

                        // Aplicar la transformación al path
                        val transformedPath = android.graphics.Path()
                        outlinePath.transform(matrix, transformedPath)

                        // Convertir a Path de Compose
                        val composePath = transformedPath.asComposePath()
                        /*
                        // ✅ PRUEBA SIMPLE: UN SOLO BORDE ROJO GRUESO
                        drawPath(
                            path = composePath,
                            color = Color.Red, // ROJO SÓLIDO
                            style = Stroke(
                                width = 5f / scale, // BIEN GRUESO para que se vea
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                         */

                        // SOMBRA EXTERIOR (Glow)
                        drawPath(
                            path = composePath,
                            color = Color.Black.copy(alpha = 0.4f), // Opacidad
                            style = Stroke(
                                width = 9f / scale, // Grosor y Se ajusta con el zoom - Antes 12f
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                            blendMode = BlendMode.Multiply
                        )

                        // BORDE PRINCIPAL
                        drawPath(
                            path = composePath,
                            color = Color(0xFF2C3E50).copy(alpha = 0.6f), // Azul oscuro
                            style = Stroke(
                                width = 3f / scale, // Antes 4f
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // HIGHLIGHT INTERNO (opcional - efecto 3D)
                        drawPath(
                            path = composePath,
                            color = Color.White.copy(alpha = 0.15f),
                            style = Stroke(
                                width = 1.5f / scale, // Antes 2f
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                            blendMode = BlendMode.Screen
                        )
                    }
                }
            }
        }
    }
}

// --- INICIO CENTRO CONTROL PARA TINTES DE TEXTURAS EN PAISES ---
// 1. Enum para identificar el tipo de textura de forma segura
private enum class TextureType {
    AVAILABLE,
    CONQUERED,
    DOMINATED,
    LOCKED,
    PAPER
}
// 2. Interruptores globales para activar/desactivar cada tinte
private const val USE_TINT_FOR_AVAILABLE = false // Cambia a false para apagar el tinte
private const val USE_TINT_FOR_CONQUERED = false // Cambia a false para apagar el tinte
private const val USE_TINT_FOR_DOMINATED = false // Cambia a false para apagar el tinte
private const val USE_TINT_FOR_LOCKED = false    // Cambia a false para apagar el tinte
private const val USE_TINT_FOR_PAPER = false      // Actualmente apagado, como en tu código original

/**
 * Crea un objeto Paint configurado con una textura y aplica un tinte condicional.
 */
private fun createTexturePaint(
    texture: Bitmap,
    type: TextureType, // Ahora recibe un tipo, no un color
    baseColors: Map<TextureType, Int> // Pasamos todos los colores base
): android.graphics.Paint {
    val shader = BitmapShader(texture, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
        setShader(shader)
    }

    // Lógica centralizada para decidir si se aplica el tinte
    val (useTint, tintColor) = when (type) {
        TextureType.AVAILABLE -> Pair(USE_TINT_FOR_AVAILABLE, baseColors[TextureType.AVAILABLE])
        TextureType.CONQUERED -> Pair(USE_TINT_FOR_CONQUERED, baseColors[TextureType.CONQUERED])
        TextureType.DOMINATED -> Pair(USE_TINT_FOR_DOMINATED, baseColors[TextureType.DOMINATED])
        TextureType.LOCKED    -> Pair(USE_TINT_FOR_LOCKED, baseColors[TextureType.LOCKED])
        TextureType.PAPER     -> Pair(USE_TINT_FOR_PAPER, android.graphics.Color.argb(180, 139, 119, 101)) // Color sepia original (180, 139, 119, 101)
    }

    if (useTint && tintColor != null) {
        val tintedAlphaColor = android.graphics.Color.argb(
            120, // Opacidad del tinte (puedes hacerla una constante también)
            android.graphics.Color.red(tintColor),
            android.graphics.Color.green(tintColor),
            android.graphics.Color.blue(tintColor)
        )
        paint.colorFilter = PorterDuffColorFilter(tintedAlphaColor, PorterDuff.Mode.OVERLAY)
    }

    return paint
}
// --- FIN CENTRO CONTROL PARA TINTES DE TEXTURAS EN PAISES ---

private fun showCustomToast(context: Context, message: String) {
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.custom_toast_layout, null)
    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = message

    Toast(context).apply {
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}