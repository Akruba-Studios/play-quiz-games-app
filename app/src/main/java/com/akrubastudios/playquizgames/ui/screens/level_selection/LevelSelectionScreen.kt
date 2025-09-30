package com.akrubastudios.playquizgames.ui.screens.level_selection

import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
    // Usamos una Surface que actúa como nuestro contenedor principal.
    // Le damos forma, borde y un modificador clickable que SIEMPRE funciona.
    Surface(
        onClick = {
            // Decidimos qué lambda llamar basado en si el nivel está bloqueado.
            if (level.isLocked) {
                onLockedClick()
            } else {
                onLevelClick(level.levelId)
            }
        },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        // Hacemos que el color de fondo parezca un botón deshabilitado si está bloqueado.
        color = if (level.isLocked) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    ) {
        // El contenido interno es una Row, que es lo que tienen los Buttons.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp), // Padding similar al de un OutlinedButton
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hacemos que el texto parezca deshabilitado si está bloqueado.
            val contentColor = if (level.isLocked) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            } else {
                MaterialTheme.colorScheme.primary
            }

            Text(
                text = level.levelName,
                color = contentColor,
                fontWeight = if (level.isLocked) FontWeight.Normal else FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))

            if (level.isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = stringResource(R.string.cd_locked),
                    tint = contentColor // Usamos el mismo color atenuado.
                )
            } else {
                Row {
                    (1..3).forEach { starIndex ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (starIndex <= level.starsEarned) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}