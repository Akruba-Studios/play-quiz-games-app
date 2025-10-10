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
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

// ===================================================================
// COMPOSABLE MONITOR VISUAL DE FPS - CONTROL 29-MS
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

    val sheetState = rememberModalBottomSheetState()

    var showGemsTutorialDialog by remember { mutableStateOf(false) }

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

@Composable
fun InteractiveWorldMap(
    countries: List<Country>,
    conqueredCountryIds: List<String>,
    dominatedCountryIds: List<String>,
    availableCountryIds: List<String>,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier
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

    var pulseAlpha by remember { mutableStateOf(0.7f) }

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

                    // Primero extraer TODOS los países del SVG
                    val allCountryIds = extractAllCountryIdsFromSVG(context)

                    // Luego extraer las coordenadas de TODOS
                    val pathCoordinates = extractPathCoordinates(context, allCountryIds)

                    if (!isActive) return@LaunchedEffect

                    val width = 1200
                    val height = (width * svg.documentHeight / svg.documentWidth).toInt()

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT)

                    svg.setDocumentViewBox(0f, 0f, svg.documentWidth, svg.documentHeight)
                    svg.renderToCanvas(canvas)

                    if (!isActive) return@LaunchedEffect

                    // 1. Cargar todas las texturas una sola vez
                    val textureAvailable = BitmapFactory.decodeResource(context.resources, R.drawable.texture_available)
                    val textureConquered = BitmapFactory.decodeResource(context.resources, R.drawable.texture_conquered)
                    val textureDominated = BitmapFactory.decodeResource(context.resources, R.drawable.texture_dominated)
                    val textureLocked = BitmapFactory.decodeResource(context.resources, R.drawable.texture_locked)
                    val texturePaper = BitmapFactory.decodeResource(context.resources, R.drawable.old_paper_texture)

                    // 2. Crear un mapa con los colores base para cada tipo
                    val colorMap = mapOf(
                        TextureType.AVAILABLE to availableColor,
                        TextureType.CONQUERED to conqueredColor,
                        TextureType.DOMINATED to dominatedColor,
                        TextureType.LOCKED to defaultColor
                    )

                    // 3. Crear los "Pinceles" usando el nuevo sistema
                    val availablePaint = createTexturePaint(textureAvailable, TextureType.AVAILABLE, colorMap)
                    val conqueredPaint = createTexturePaint(textureConquered, TextureType.CONQUERED, colorMap)
                    val dominatedPaint = createTexturePaint(textureDominated, TextureType.DOMINATED, colorMap)
                    val lockedPaint = createTexturePaint(textureLocked, TextureType.LOCKED, colorMap)
                    val paperPaint = createTexturePaint(texturePaper, TextureType.PAPER, colorMap)

                    val newCountryPaths = mutableMapOf<String, android.graphics.Path>()

                    pathColorMap.forEach { (countryId, color) ->
                        pathCoordinates[countryId]?.let { pathData ->
                            // 3. Seleccionar el pincel correcto según el color/estado
                            val paintToUse = when (color) {
                                dominatedColor -> dominatedPaint
                                conqueredColor -> conqueredPaint
                                availableColor -> availablePaint
                                else -> lockedPaint // defaultColor
                            }

                            val path = android.graphics.Path()
                            parsePathData(pathData, path)
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

                    // Dibujar textura en países sin contenido (esta lógica no cambia)
                    pathCoordinates.keys.forEach { countryId ->
                        if (!pathColorMap.containsKey(countryId)) {
                            pathCoordinates[countryId]?.let { pathData ->
                                val path = android.graphics.Path()
                                parsePathData(pathData, path)
                                canvas.drawPath(path, paperPaint)
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

    // Función para detectar qué país fue tocado
    fun detectCountryFromTap(
        tapOffset: Offset,
        svgBitmap: Bitmap,
        canvasSize: androidx.compose.ui.geometry.Size,
        hOffset: Float,
        vOffset: Float
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

            val interactableCountries = (conqueredCountryIds + availableCountryIds).toSet()

            for (countryId in interactableCountries) {
                countryPaths[countryId]?.let { path ->
                    val region = android.graphics.Region()
                    val clipRegion = android.graphics.Region(0, 0, svgBitmap.width, svgBitmap.height)
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
        // CAPA 1: FONDO DE VIDEO
        VideoBackground(
            videoResId = R.raw.ocean_background,
            modifier = Modifier.fillMaxSize()
        )
        // CAPA 1.5: Gradient overlay
        // OceanGradientOverlay(modifier = Modifier.fillMaxSize()) // Gradient Overlay Animado

        // CAPA 1.6: Efectos especulares
        // OceanSpecularEffect(modifier = Modifier.fillMaxSize()) // Brillo Especular Animado

        // CAPA 1.7: Vignette
        OceanVignette(modifier = Modifier.fillMaxSize()) // Vignette Dinámico

        // CAPA 1.8: Partículas Flotantes
        OceanBubblesEffect(modifier = Modifier.fillMaxSize()) // Particulas Flotantes

        // CAPA 1.9: Rayos de Luz
        // OceanGodRaysEffect(modifier = Modifier.fillMaxSize()) // Rayos de luz penetrando el agua

        // CAPA 1.10: Color Grading Dinámico
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = timeBasedTint)
        }

        // CAPA 1.11: Niebla Flotante
        // OceanMistEffect(modifier = Modifier.fillMaxSize()) // Neblina atmosférica

        // CAPA 2: Mapa en canvas original
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformableState)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        if (isMapReady) {
                            processedSvgBitmap?.let { bitmap ->
                                detectCountryFromTap(
                                    tapOffset,
                                    bitmap,
                                    size.toSize(),
                                    hOffset = horizontalOffsetFactor,
                                    vOffset = verticalOffsetFactor
                                )?.let { countryId ->
                                    onCountryClick(countryId)
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