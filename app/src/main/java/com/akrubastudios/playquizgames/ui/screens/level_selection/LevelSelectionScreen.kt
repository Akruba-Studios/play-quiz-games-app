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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akrubastudios.playquizgames.domain.LevelStatus

@Composable
fun LevelSelectionScreen(
    viewModel: LevelSelectionViewModel = hiltViewModel(),
    onLevelClick: (levelId: String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Aquí podrías añadir un botón de "Atrás"
        Text(
            text = uiState.categoryName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.levels) { level ->
                    LevelItem(level = level, onLevelClick = onLevelClick)
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
            Icon(Icons.Default.Lock, contentDescription = "Bloqueado")
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