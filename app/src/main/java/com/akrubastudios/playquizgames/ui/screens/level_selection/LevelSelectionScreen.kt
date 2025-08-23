package com.akrubastudios.playquizgames.ui.screens.level_selection

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

@Composable
fun LevelSelectionScreen(
    viewModel: LevelSelectionViewModel = hiltViewModel(),
    onLevelClick: (levelId: String, difficulty: String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Aquí podrías añadir un botón de "Atrás"
        Text(
            text = uiState.categoryName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
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
                        val textRes = if (id == "principiante") R.string.difficulty_beginner else R.string.difficulty_hard
                        Text(text = stringResource(textRes))
                    }
                )
            }
        }

        // NUEVO: Pequeño texto explicativo del bono
        Text(
            text = if (selectedDifficulty == "dificil") stringResource(R.string.difficulty_hard_bonus) else stringResource(R.string.difficulty_beginner_tip),
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
                        onLevelClick = { onLevelClick(it, selectedDifficulty) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LevelItem(level: LevelStatus, onLevelClick: (String) -> Unit) {
    OutlinedButton(
        onClick = { onLevelClick(level.levelId) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !level.isLocked
    ) {
        Text(text = level.levelName)
        Spacer(Modifier.weight(1f))
        if (level.isLocked) {
            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.cd_locked))
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