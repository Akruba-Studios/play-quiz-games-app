package com.akrubastudios.playquizgames.ui.screens.level_selection

import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.domain.LevelStatus
import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.LanguageManager
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.sp
import com.akrubastudios.playquizgames.core.AppConstants
import com.akrubastudios.playquizgames.ui.components.ScreenBackground
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable // Control 3-LSS
fun LevelSelectionScreen(
    viewModel: LevelSelectionViewModel = hiltViewModel(),
    onLevelClick: (levelId: String, difficulty: String) -> Unit,
    onBackClick: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadLevels()
    }

    // --- LÓGICA DE GESTOS DE DESLIZAMIENTO ---
    var dragAmount by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 70.dp // Distancia mínima para que el swipe se registre

    ScreenBackground(
        backgroundUrl = AppConstants.MENU_BACKGROUND_URL,
        imageLoader = viewModel.imageLoader // <-- AÑADE ESTA LÍNEA
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragAmount = 0f },
                        onDragEnd = {
                            if (abs(dragAmount) > dragThreshold.toPx()) {
                                if (dragAmount > 0) {
                                    // Deslizar hacia la derecha -> Seleccionar "Fácil"
                                    viewModel.onDifficultyChange("principiante")
                                } else {
                                    // Deslizar hacia la izquierda -> Seleccionar "Difícil"
                                    viewModel.onDifficultyChange("dificil")
                                }
                            }
                        }
                    ) { change, drag ->
                        dragAmount += drag.x
                        change.consume()
                    }
                }
        ) {
            // Aquí podrías añadir un botón de "Atrás"
            Text(
                text = uiState.categoryName,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), // <-- AÑADE ESTA LÍNEA
                textAlign = TextAlign.Center // <-- AÑADE ESTA LÍNEA
            )

            // --- INICIO DEL CÓDIGO AÑADIDO: SELECTOR DE DIFICULTAD ---
            // 1. Usamos una lista de IDs para la lógica. No se traduce.
            val difficultyIds = listOf("principiante", "dificil")
            // 2. El índice se calcula comparando con la lista de IDs.
            val selectedIndex = difficultyIds.indexOf(selectedDifficulty)

            TabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 3. Iteramos sobre los IDs.
                difficultyIds.forEachIndexed { index, id ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { viewModel.onDifficultyChange(id) },
                        // 4. Obtenemos el texto traducido DENTRO del Tab.
                        text = {
                            val textRes =
                                if (id == "principiante") R.string.difficulty_beginner else R.string.difficulty_hard
                            Text(text = stringResource(textRes))
                        }
                    )
                }
            }

            // NUEVO: Pequeño texto explicativo del bono
            Text(
                text = if (selectedDifficulty == "dificil") stringResource(R.string.difficulty_hard_bonus) else stringResource(
                    R.string.difficulty_beginner_tip
                ),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(uiState.levels) { level ->
                        LevelItem(
                            level = level,
                            onLevelClick = { onLevelClick(it, selectedDifficulty) },
                            onLockedClick = {
                                // --- LÓGICA DEL TOAST PERSONALIZADO ---
                                val inflater = LayoutInflater.from(context)
                                val layout = inflater.inflate(R.layout.custom_toast_layout, null)

                                val textView = layout.findViewById<TextView>(R.id.toast_text)
                                textView.text = context.getString(R.string.level_unlock_toast)

                                Toast(context).apply {
                                    duration = Toast.LENGTH_SHORT
                                    view = layout
                                    show()
                                }
                                // --- FIN DE LA LÓGICA DEL TOAST ---
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp)) // Un poco de espacio
                        Button(
                            onClick = onBackClick, // ¡Simplemente llama a la función que ya existe!
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.level_selection_back_to_country))
                        }
                    }
                }
            }
        }
    }
    if (uiState.showLevelUnlockTutorialDialog) {
        AppAlertDialog(
            onDismissRequest = { viewModel.levelUnlockTutorialShown() },
            title = stringResource(R.string.level_unlock_tutorial_title),
            text = stringResource(R.string.level_unlock_tutorial_message),
            confirmButtonText = stringResource(R.string.dialog_button_ok)
        )
    }
}

// REEMPLAZA ESTA FUNCIÓN COMPLETA
@Composable
fun LevelItem(
    level: LevelStatus,
    onLevelClick: (String) -> Unit,
    onLockedClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // 1. Definimos los tamaños dinámicos para fuente y estrellas
    val levelNameFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 13.sp // Zona crítica
            screenWidth < 370.dp -> 14.sp // Zona de transición
            else -> 16.sp                 // Tamaño normal (bodyLarge)
        }
    }

    val starSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 18.dp // Zona crítica
            screenWidth < 370.dp -> 20.dp // Zona de transición
            else -> 24.dp                 // Tamaño normal
        }
    }

    // 2. El ancho del contenedor de estrellas AHORA es dinámico
    val starContainerWidth = remember(starSize) {
        (starSize * 3) + 4.dp // 3 estrellas + 4dp de espacio entre cada una
    }
    // Usamos una Surface que actúa como nuestro contenedor principal.
    // Le damos forma, borde y un modificador clickable que SIEMPRE funciona.
    val itemShape = MaterialTheme.shapes.medium

    Surface(
        onClick = {
            if (level.isLocked) {
                onLockedClick()
            } else {
                onLevelClick(level.levelId)
            }
        },
        modifier = Modifier, // Limpiamos el modifier de aquí
        shape = itemShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        // El color del Surface ahora es siempre transparente,
        // porque el fondo lo controlará la Row interna.
        color = Color.Transparent
    ) {
        // El contenido interno es una Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                // ¡LA SOLUCIÓN ESTÁ AQUÍ!
                .background(
                    color = if (level.isLocked) {
                        // Color semitransparente si está bloqueado
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    } else {
                        // Color sólido si está DESBLOQUEADO
                        MaterialTheme.colorScheme.background.copy(alpha = 0.6f) // 0.8f - 80% de Opacidad
                    }
                )
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hacemos que el contenido parezca deshabilitado si está bloqueado.
            val contentColor = if (level.isLocked) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            } else {
                MaterialTheme.colorScheme.primary
            }

            Text(
                text = level.levelName,
                color = contentColor,
                fontSize = levelNameFontSize,
                fontWeight = if (level.isLocked) FontWeight.Normal else FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))

            if (level.isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = stringResource(R.string.cd_locked),
                    tint = contentColor
                )
            } else {
                Row(
                    modifier = Modifier.width(starContainerWidth),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..3).forEach { starIndex ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starIndex <= level.starsEarned) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(starSize)
                        )
                    }
                }
            }
        }
    }
}