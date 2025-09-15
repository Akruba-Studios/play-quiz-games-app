package com.akrubastudios.playquizgames.ui.screens.map

import android.R.attr.path
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
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import com.caverock.androidsvg.SVG
import java.io.IOException

import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import androidx.core.graphics.PathParser
import kotlinx.coroutines.withContext

import androidx.compose.animation.core.*

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Flight

import androidx.compose.material.icons.filled.SwapHoriz

import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import java.util.Locale

import androidx.compose.foundation.clickable // <-- AÑADE ESTA
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RocketLaunch
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

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.sp
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.AppExpeditionAlertDialog
import com.akrubastudios.playquizgames.ui.components.DialogButtonText
import com.akrubastudios.playquizgames.ui.components.DialogText
import com.akrubastudios.playquizgames.ui.components.DialogTitle
import com.akrubastudios.playquizgames.ui.components.GemsBalanceIndicator
import com.akrubastudios.playquizgames.ui.components.getButtonTextColor
import com.akrubastudios.playquizgames.ui.theme.CyanAccent
import com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent
import com.akrubastudios.playquizgames.ui.theme.LightGray
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current // Necesitaremos el contexto para los strings
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()

    var showGemsTutorialDialog by remember { mutableStateOf(false) }

    val currentLanguageCode = Locale.getDefault().language

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
                        tonalElevation = 0.dp
                    ) {
                        // --- Elemento 1: Ranking (sin cambios) ---
                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate(Routes.RANKING_SCREEN) },
                            icon = {
                                Icon(
                                    Icons.Filled.Leaderboard,
                                    contentDescription = stringResource(R.string.bottom_nav_ranking)
                                )
                            },
                            label = { Text(stringResource(R.string.bottom_nav_ranking)) }
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
                                                scope.launch {
                                                    val message =
                                                        context.getString(R.string.free_mode_unlock_toast)
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.SwapHoriz,
                                            contentDescription = stringResource(R.string.bottom_nav_free_mode)
                                        )
                                    }
                                } else {
                                    // Si está habilitado, mostramos el icono normalmente.
                                    Icon(
                                        Icons.Filled.SwapHoriz,
                                        contentDescription = stringResource(R.string.bottom_nav_free_mode)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.bottom_nav_free_mode)) }
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
                                            Badge() // El punto rojo por defecto
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.AccountCircle,
                                        contentDescription = stringResource(R.string.bottom_nav_profile)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.bottom_nav_profile)) }
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
                        CircularProgressIndicator()
                    }
                } else {
                    InteractiveWorldMap(
                        countries = uiState.countries,
                        conqueredCountryIds = uiState.conqueredCountryIds,
                        dominatedCountryIds = uiState.dominatedCountryIds,
                        availableCountryIds = uiState.availableCountryIds,
                        onCountryClick = { countryId ->
                            navController.navigate(
                                Routes.COUNTRY_SCREEN.replace("{countryId}", countryId)
                            )
                        },
                        modifier = Modifier.fillMaxSize() // El mapa ocupa todo el espacio
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
                            .align(Alignment.TopCenter) // Lo posiciona en la parte superior
                            .fillMaxWidth(),
                        // Usamos el mismo color que el BottomAppBar para consistencia.
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 3.dp // Añade una pequeña sombra para dar profundidad
                    ) {
                        // Usamos una Columna para apilar el título y el indicador verticalmente.
                        Column(
                            modifier = Modifier.padding(bottom = 8.dp), // Un pequeño padding inferior
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. El Título
                            Text(
                                stringResource(R.string.map_title),
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )

                            // Envolvemos los indicadores en una Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
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
                                // Espacio entre ellos
                                Spacer(modifier = Modifier.width(16.dp))

                                // Indicador de Gemas a la derecha
                                GemsBalanceIndicator(
                                    gems = uiState.gems,
                                    modifier = Modifier.clickable {
                                        showGemsTutorialDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                // El icono del avión (botón flotante) se muestra si una expedición está disponible.
                if (uiState.expeditionAvailable) {
                    FloatingActionButton(
                        onClick = { viewModel.requestExpeditionDialog() }, // Al hacer clic, abre el diálogo
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Lo posiciona en la esquina inferior derecha
                            .padding(16.dp),
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

    // Diálogo para el desafío de Jefe post-conquista
    uiState.pendingBossChallenge?.let { countryId ->
        val countryName = uiState.countries.find { it.countryId == countryId }?.name?.get("es") ?: "este país"
        val buttonTextColor = getButtonTextColor()

        AppAlertDialog(
            onDismissRequest = { viewModel.clearPendingBossChallenge() },
            title = { DialogTitle(text = stringResource(R.string.conquest_dialog_title)) },
            text = { DialogText(text = stringResource(R.string.conquest_dialog_text, countryName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearPendingBossChallenge()
                        val bossLevelId = uiState.countries.find { it.countryId == countryId }?.bossLevelId ?: ""
                        if (bossLevelId.isNotEmpty()) {
                            val route = Routes.BOSS_SCREEN
                                .replace("{countryId}", countryId)
                                .replace("{levelId}", bossLevelId)
                            navController.navigate(route)
                        }
                    }
                ) {
                    DialogButtonText(text = stringResource(R.string.conquest_dialog_button_challenge))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearPendingBossChallenge() }) {
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
private fun DrawScope.drawOceanBackground(waveTime: Float, canvasSize: androidx.compose.ui.geometry.Size) {
    // OPCION 3: Colores oceánicos claros - estilo tropical/mediterráneo
    val deepOcean = Color(0xFF1B4F72)      // Azul profundo más claro
    val mediumOcean = Color(0xFF2874A6)    // Azul medio vibrante
    val shallowOcean = Color(0xFF3498DB)   // Azul claro brillante
    val surfaceShine = Color(0xFF85C1E9)   // Brillo superficial luminoso

    // CAPA 1: Fondo base con gradiente de profundidad
    drawRect(color = deepOcean, size = canvasSize)

    // CAPA 2: Variaciones de profundidad procedurales
    val noiseScale = 0.002f
    val depthVariationIntensity = 0.3f

    // Generar variaciones de profundidad usando funciones matemáticas
    for (x in 0 until canvasSize.width.toInt() step 8) {
        for (y in 0 until canvasSize.height.toInt() step 8) {
            // Simular Perlin noise con funciones trigonométricas
            val nx = x * noiseScale
            val ny = y * noiseScale

            val noise1 = sin(nx * 3.7f + waveTime * 0.2f) * cos(ny * 2.1f + waveTime * 0.15f)
            val noise2 = sin(nx * 7.3f + waveTime * 0.1f) * cos(ny * 5.9f + waveTime * 0.25f)
            val noise3 = sin(nx * 1.4f + waveTime * 0.3f) * cos(ny * 1.8f + waveTime * 0.08f)

            val combinedNoise = (noise1 * 0.5f + noise2 * 0.3f + noise3 * 0.2f)
            val normalizedNoise = (combinedNoise + 1f) / 2f // 0 a 1

            // Calcular color basado en la "profundidad simulada"
            val depthFactor = normalizedNoise * depthVariationIntensity
            val currentColor = when {
                depthFactor < 0.1f -> deepOcean
                depthFactor < 0.15f -> mediumOcean.copy(alpha = 0.7f)
                else -> shallowOcean.copy(alpha = 0.4f)
            }

            if (depthFactor > 0.05f) {
                drawRect(
                    color = currentColor,
                    topLeft = Offset(x.toFloat(), y.toFloat()),
                    size = androidx.compose.ui.geometry.Size(8f, 8f)
                )
            }
        }
    }

    // CAPA 3: Corrientes marinas direccionales
    val currentDirection1 = waveTime * 0.5f
    val currentDirection2 = waveTime * -0.3f + PI.toFloat()

    // Corriente principal (horizontal)
    for (y in 0 until canvasSize.height.toInt() step 25) {
        val amplitude = 15f + sin(y * 0.01f + waveTime * 0.4f) * 8f
        val frequency = 0.008f + sin(y * 0.005f) * 0.002f

        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, y.toFloat())

        for (x in 0..canvasSize.width.toInt() step 4) {
            val waveY = y + sin(x * frequency + currentDirection1) * amplitude
            path.lineTo(x.toFloat(), waveY)
        }

        val alpha = abs(sin(waveTime * 0.3f + y * 0.008f)) * 0.12f
        drawPath(
            path = path,
            color = mediumOcean.copy(alpha = alpha),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }

    // Corriente secundaria (diagonal)
    for (line in 0..20) {
        val startX = line * canvasSize.width / 20f - canvasSize.width * 0.2f
        val angle = 0.3f + sin(waveTime * 0.2f) * 0.1f

        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(startX, 0f)

        for (step in 0..50) {
            val progress = step / 50f
            val x = startX + progress * canvasSize.width * 1.4f
            val y = progress * canvasSize.height + sin(progress * 4f + currentDirection2) * 30f

            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        val alpha = abs(sin(waveTime * 0.4f + line * 0.3f)) * 0.08f
        drawPath(
            path = path,
            color = shallowOcean.copy(alpha = alpha),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )
    }

    // CAPA 4: Reflexiones especulares sutiles
    val specularCenters = listOf(
        Offset(canvasSize.width * 0.3f, canvasSize.height * 0.2f),
        Offset(canvasSize.width * 0.7f, canvasSize.height * 0.5f),
        Offset(canvasSize.width * 0.2f, canvasSize.height * 0.8f),
        Offset(canvasSize.width * 0.8f, canvasSize.height * 0.3f)
    )

    specularCenters.forEachIndexed { index, center ->
        val timeOffset = index * PI.toFloat() / 2f
        val animatedTime = waveTime * 0.6f + timeOffset

        // Reflexión principal
        val mainRadius = 40f + sin(animatedTime) * 15f
        val mainAlpha = (0.15f + sin(animatedTime * 1.5f) * 0.08f).coerceIn(0f, 0.25f)

        drawCircle(
            color = surfaceShine.copy(alpha = mainAlpha),
            radius = mainRadius,
            center = center
        )

        // Reflexión secundaria más pequeña
        val secondaryRadius = mainRadius * 0.4f
        val secondaryAlpha = mainAlpha * 0.6f

        drawCircle(
            color = surfaceShine.copy(alpha = secondaryAlpha),
            radius = secondaryRadius,
            center = center + Offset(
                sin(animatedTime * 2f) * 20f,
                cos(animatedTime * 1.8f) * 15f
            )
        )
    }

    // CAPA 5: Shimmer atmosférico global mejorado
    val atmosphereIntensity = (sin(waveTime * 0.8f) + 1f) / 2f * 0.06f
    drawRect(
        color = surfaceShine.copy(alpha = atmosphereIntensity),
        size = canvasSize
    )

    // CAPA 6: Efecto de profundidad con gradiente radial desde el centro
    val centerPoint = Offset(canvasSize.width * 0.5f, canvasSize.height * 0.5f)
    val maxDistance = kotlin.math.sqrt(
        (canvasSize.width * canvasSize.width + canvasSize.height * canvasSize.height).toDouble()
    ).toFloat() * 0.5f

    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                deepOcean.copy(alpha = 0.2f)
            ),
            center = centerPoint,
            radius = maxDistance
        ),
        radius = maxDistance,
        center = centerPoint
    )
}

@Composable
fun InteractiveWorldMap(
    countries: List<Country>,
    conqueredCountryIds: List<String>,
    dominatedCountryIds: List<String>,
    availableCountryIds: List<String>,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados para zoom y pan
    var scale by remember { mutableStateOf(1.6f) } // 1.6f es la escala para agrandar por defecto el mapa
    var offset by remember { mutableStateOf(Offset.Zero) }
    // Estados para la animación del océano
    var waveTime by remember { mutableStateOf(0f) }

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

    var pulseAlpha by remember { mutableStateOf(0.7f) }

    val context = LocalContext.current
    val density = LocalDensity.current

    // Definir colores
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

    // Animación del océano
    LaunchedEffect(Unit) {
        while(true) {
            waveTime += 0.016f  // Incremento suave ~60fps
            delay(16)
            // Opcional: reset muy ocasional para evitar overflow
            if (waveTime > 1000f) waveTime -= 1000f
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

    // MODIFICADO: Procesamiento mejorado sin flash
    LaunchedEffect(svgDocument, pathColorMap) {
        svgDocument?.let { svg ->
            if (pathColorMap.isNotEmpty()) {
                try {
                    // NO limpiar processedSvgBitmap aquí para evitar flash
                    // processedSvgBitmap = null  // <-- REMOVIDO

                    val pathCoordinates = extractPathCoordinates(context, pathColorMap.keys)

                    if (!isActive) return@LaunchedEffect

                    val width = 1200
                    val height = (width * svg.documentHeight / svg.documentWidth).toInt()

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT)

                    svg.setDocumentViewBox(0f, 0f, svg.documentWidth, svg.documentHeight)
                    svg.renderToCanvas(canvas)

                    if (!isActive) return@LaunchedEffect

                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.FILL
                    }

                    val newCountryPaths = mutableMapOf<String, android.graphics.Path>()

                    pathColorMap.forEach { (countryId, color) ->
                        pathCoordinates[countryId]?.let { pathData ->
                            paint.color = color
                            val path = android.graphics.Path()
                            parsePathData(pathData, path)
                            newCountryPaths[countryId] = path
                            canvas.drawPath(path, paint)

                            // NUEVO: Agregar grietas para países grises
                            if (color == defaultColor) {
                                val crackPaint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 1f // Grosor de las lineas o bordes de los paises grises
                                    setColor(android.graphics.Color.argb(128, 255, 255, 255)) // Blanco 25% transparente
                                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 6f), 0f)
                                }
                                canvas.drawPath(path, crackPaint)
                            }
                        }
                    }

                    if (isActive) {
                        // ACTUALIZAR TODO ATOMICAMENTE para evitar estados inconsistentes
                        processedSvgBitmap = bitmap
                        countryPaths = newCountryPaths

                        // MARCAR COMO LISTO SOLO CUANDO TODO ESTÉ PROCESADO
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

    // NUEVO: Animación de pulso para candados
    /*
    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                pulseAlpha = value
            }
            animate(
                initialValue = 0.7f,
                targetValue = 0.3f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                pulseAlpha = value
            }
        }
    }
     */

    // Función para detectar qué país fue tocado
    fun detectCountryFromTap(
        tapOffset: Offset,
        svgBitmap: Bitmap,
        canvasSize: androidx.compose.ui.geometry.Size
    ): String? {
        return try {
            val bitmapAspectRatio = svgBitmap.width.toFloat() / svgBitmap.height.toFloat()
            val canvasAspectRatio = canvasSize.width / canvasSize.height

            val scaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                canvasSize.width / svgBitmap.width
            } else {
                canvasSize.height / svgBitmap.height
            }

            val scaledWidth = svgBitmap.width * scaleFactor * scale
            val scaledHeight = svgBitmap.height * scaleFactor * scale

            val centerX = canvasSize.width / 2f
            val centerY = canvasSize.height / 2f
            val left = centerX - (scaledWidth / 2f) + offset.x + (canvasSize.width * 0.20f) // + (205/1080=0.20) +205 es el offset manual para centrar el mapa, tiene que ser el mismo valor en canvas
            val top = centerY - (scaledHeight / 2f) + offset.y - (canvasSize.height * 0.055f) // - (100/1812=0.055) -100 es el offset manual para centrar el mapa, tiene que ser el mismo valor en canvas

            val svgX = (tapOffset.x - left) / (scaleFactor * scale)
            val svgY = (tapOffset.y - top) / (scaleFactor * scale)

            if (svgX < 0 || svgX >= svgBitmap.width || svgY < 0 || svgY >= svgBitmap.height) {
                return null
            }

            val interactableCountries = (conqueredCountryIds + availableCountryIds).toSet()

            for (countryId in interactableCountries) {
                countryPaths[countryId]?.let { path ->
                    val region = android.graphics.Region()
                    val clipRegion = android.graphics.Region(
                        0, 0, svgBitmap.width, svgBitmap.height
                    )

                    region.setPath(path, clipRegion)

                    if (region.contains(svgX.toInt(), svgY.toInt())) {
                        return countryId
                    }
                }
            }
            null

        } catch (e: Exception) {
            android.util.Log.e("HitTesting", "Error en detección de tap", e)
            null
        }
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(0.8f, 5f) //0.8f y 5f; son el minimo y maximo para hacer zoom

        // Usar valores fijos basados en una pantalla promedio (más simple)
        val maxOffsetX = canvasWidth * 0.30f * newScale   // 30% para hacer drag en horizontal
        val maxOffsetY = canvasHeight * 0.10f * newScale  // 12% para hacer drag en vertical

        val newOffset = (offset + panChange).copy(
            x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
            y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
        )
        scale = newScale
        offset = newOffset
    }

    // MODIFICADO: Canvas con mejor control de renderizado
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .transformable(transformableState)
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (isMapReady) {  // Solo permitir taps cuando esté listo
                        processedSvgBitmap?.let { bitmap ->
                            detectCountryFromTap(tapOffset, bitmap, size.toSize())?.let { countryId ->
                                onCountryClick(countryId)
                            }
                        }
                    }
                }
            }
    ) {
        canvasWidth = size.width
        canvasHeight = size.height

        // Fondo oceánico animado
        drawOceanBackground(waveTime, size)

        // MODIFICADO: Renderizado condicional mejorado
        when {
            // Mostrar el mapa solo cuando esté completamente listo
            isMapReady && processedSvgBitmap != null -> {
                val bitmap = processedSvgBitmap!!

                val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val canvasAspectRatio = size.width / size.height

                val scaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                    size.width / bitmap.width
                } else {
                    size.height / bitmap.height
                }

                val scaledWidth = bitmap.width * scaleFactor * scale
                val scaledHeight = bitmap.height * scaleFactor * scale

                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val left = centerX - (scaledWidth / 2f) + offset.x + (size.width * 0.20f) // + (205/1080=0.20) +205 es el offset manual para centrar el mapa, tiene que ser el mismo valor en detectcountryfromtap
                val top = centerY - (scaledHeight / 2f) + offset.y - (size.height * 0.055f) // - (100/1812=0.055) -100 es el offset manual para centrar el mapa, tiene que ser el mismo valor en detectcountryfromtap

                drawImage(
                    image = bitmap.asImageBitmap(),
                    dstOffset = IntOffset(left.toInt(), top.toInt()),
                    dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt()),
                    filterQuality = FilterQuality.High
                )

                // NUEVO: Dibujar candados con coordenadas manuales para países problemáticos
                /*
                countries.forEach { country ->
                    if (pathColorMap[country.countryId] == defaultColor) {
                        countryPaths[country.countryId]?.let { path ->
                            // Coordenadas manuales para países problemáticos
                            val (centerX, centerY) = when (country.countryId) {
                                "es" -> Pair(395f, 420f)  // España - coordenadas ajustadas manualmente
                                "fr" -> Pair(410f, 410f)  // Francia - coordenadas ajustadas manualmente
                                else -> {
                                    // Para el resto usar cálculo automático
                                    val bounds = android.graphics.RectF()
                                    path.computeBounds(bounds, true)
                                    Pair(bounds.centerX(), bounds.centerY())
                                }
                            }

                            // USAR LA MISMA LÓGICA EXACTA QUE EL BITMAP
                            val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            val canvasAspectRatio = size.width / size.height

                            val scaleFactor = if (bitmapAspectRatio > canvasAspectRatio) {
                                size.width / bitmap.width
                            } else {
                                size.height / bitmap.height
                            }

                            val transformedX = left + (centerX * scaleFactor * scale)
                            val transformedY = top + (centerY * scaleFactor * scale)

                            val lockSize = 4.dp.toPx() * scale // 6. Tamaño del candado

                            drawContext.canvas.nativeCanvas.apply {
                                val lockPaint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    setColor(android.graphics.Color.argb(
                                        (255 * pulseAlpha).toInt(), 220, 220, 220 // Color candados: rgb 220,220,220 = gris blanco
                                    ))
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = 3f * scale
                                }

                                // Dibujar candado simple
                                val lockRect = android.graphics.RectF(
                                    transformedX - lockSize/2,
                                    transformedY - lockSize/4,
                                    transformedX + lockSize/2,
                                    transformedY + lockSize/2
                                )
                                drawRoundRect(lockRect, 4f * scale, 4f * scale, lockPaint)

                                // Arco del candado
                                val arcRect = android.graphics.RectF(
                                    transformedX - lockSize/3,
                                    transformedY - lockSize/2,
                                    transformedX + lockSize/3,
                                    transformedY
                                )
                                drawArc(arcRect, 180f, 180f, false, lockPaint)
                            }
                        }
                    }
                }
                 */

                // Debug info (opcional - puedes removarlo)
                /*
                drawContext.canvas.nativeCanvas.drawText(
                    "Scale: ${String.format("%.1f", scale)} | Países: ${pathColorMap.size}",
                    20f,
                    40f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        isAntiAlias = true
                    }
                )
                 */
            }
        }
    }
}