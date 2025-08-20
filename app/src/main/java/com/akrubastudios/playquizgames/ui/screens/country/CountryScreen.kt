package com.akrubastudios.playquizgames.ui.screens.country

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import com.akrubastudios.playquizgames.ui.screens.country.CountryStatus

@Composable
fun CountryScreen(
    viewModel: CountryViewModel = hiltViewModel(),
    // MODIFICADO: Necesitamos nuevas lambdas para la navegación
    onPlayCategoryClick: (categoryId: String) -> Unit,
    onChallengeBossClick: (bossLevelId: String) -> Unit,
    onApplyBoostClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isScreenLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.country == null) {
        // Estado de error si el país no se pudo cargar
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error al cargar los datos del país.")
        }
    } else {
        // Usamos una LazyColumn para contenido que podría ser largo
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = uiState.country?.name?.get("es") ?: "País",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- LÓGICA DE UI BASADA EN EL ESTADO DEL PAÍS ---
            when (uiState.countryStatus) {
                CountryStatus.AVAILABLE -> {
                    item {
                        CountryProgress(
                            current = uiState.currentPc,
                            total = uiState.pcRequired,
                            statusText = "Progreso de Conquista"
                        )
                        // Mostramos el botón solo si la condición se cumple.
                        if (uiState.canApplyBoost) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onApplyBoostClick,
                                enabled = !uiState.isApplyingBoost,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            ) {
                                if (uiState.isApplyingBoost) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Usar Boost de 5,000 PC")
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        CategoryList(
                            categories = uiState.availableCategories,
                            onCategoryClick = onPlayCategoryClick
                        )
                    }
                }
                CountryStatus.CONQUERED -> {
                    item {
                        CountryProgress(
                            current = uiState.pcRequired,
                            total = uiState.pcRequired,
                            statusText = "¡País Conquistado!"
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        ChallengeBossButton(
                            bossLevelId = uiState.country?.bossLevelId ?: "",
                            onChallengeClick = onChallengeBossClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StudyTopics(topics = uiState.studyTopics)
                    }
                }
                CountryStatus.DOMINATED -> {
                    item {
                        CountryProgress(
                            current = uiState.pcRequired,
                            total = uiState.pcRequired,
                            statusText = "¡PAÍS DOMINADO!"
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Has desbloqueado todo el contenido de este país.", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryList(
                            categories = uiState.availableCategories,
                            onCategoryClick = onPlayCategoryClick
                        )
                    }
                }
                CountryStatus.LOCKED -> {
                    item {
                        Icon(Icons.Default.Lock, contentDescription = "Bloqueado", modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Este país está bloqueado.", style = MaterialTheme.typography.headlineSmall)
                        Text("Conquista países vecinos para desbloquearlo.")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBackClick) {
                    Text("Volver al Mapa")
                }
            }
        }
    }
}

@Composable
private fun CountryProgress(current: Long, total: Long, statusText: String) {
    Text(text = statusText, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { current.toFloat() / total.toFloat() },
        modifier = Modifier.fillMaxWidth(0.8f).height(8.dp),
        strokeCap = StrokeCap.Round
    )
    Text(
        text = "$current / $total PC",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun CategoryList(categories: List<com.akrubastudios.playquizgames.domain.Category>, onCategoryClick: (String) -> Unit) {
    Text("Categorías Disponibles", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(16.dp))
    categories.forEach { category ->
        Button(
            onClick = { onCategoryClick(category.categoryId) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(category.name["es"] ?: "Categoría")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ChallengeBossButton(bossLevelId: String, onChallengeClick: (String) -> Unit) {
    Button(
        onClick = { onChallengeClick(bossLevelId) },
        modifier = Modifier.fillMaxWidth(0.9f).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text("Desafiar al Guardián", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StudyTopics(topics: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    if (topics.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth(0.9f)) {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                modifier = Modifier.clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = "Información")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reunir Intel (Pistas para el Jefe)", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Cerrar" else "Expandir"
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                    topics.forEach { topic ->
                        Text("• $topic", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}