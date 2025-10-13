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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.akrubastudios.playquizgames.ui.screens.country.CountryStatus
import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.akrubastudios.playquizgames.ui.components.ScreenBackground
import com.akrubastudios.playquizgames.ui.components.TextWithBorder
import com.akrubastudios.playquizgames.ui.theme.DeepNavy
import com.akrubastudios.playquizgames.ui.theme.LightGray
import kotlinx.coroutines.delay

@Composable // Control: 3-CS
fun CountryScreen(
    viewModel: CountryViewModel = hiltViewModel(),
    // MODIFICADO: Necesitamos nuevas lambdas para la navegación
    onPlayCategoryClick: (categoryId: String) -> Unit,
    onChallengeBossClick: (bossLevelId: String) -> Unit,
    onApplyBoostClick: () -> Unit,
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

    val isDominated = uiState.countryStatus == CountryStatus.DOMINATED

    ScreenBackground(
        backgroundUrl = uiState.country?.backgroundImageUrl ?: "",
        imageLoader = viewModel.imageLoader,
        imageAlpha = 1.0f,  // 1.0f - 100% opaca, la imagen se verá con toda su fuerza
        scrimAlpha = 0.7f,   // 0.7 - 70% opaco en el velo
        forceFullBrightness = uiState.isDominated
    ) {
        if (uiState.isScreenLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ocean_background_static),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_text),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else if (uiState.country == null) {
            // Estado de error si el país no se pudo cargar
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.country_error_loading))
            }
        } else {
            // Usamos una LazyColumn para contenido que podría ser largo
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    if (isDominated) {
                        // En modo Dominado, usamos TextWithBorder
                        TextWithBorder(
                            text = uiState.countryName,
                            style = MaterialTheme.typography.displayMedium.copy(
                                textAlign = TextAlign.Center, // Aseguramos el centrado
                                color = DeepNavy
                            ),
                            borderColor = LightGray, // Un borde negro da el mejor contraste
                            borderWidth = 6f
                        )
                    } else {
                        // En otros modos, usamos el Text normal
                        Text(
                            text = uiState.countryName,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- LÓGICA DE UI BASADA EN EL ESTADO DEL PAÍS ---
                when (uiState.countryStatus) {
                    CountryStatus.AVAILABLE -> {
                        item {
                            CountryProgress(
                                current = uiState.currentPc,
                                total = uiState.pcRequired,
                                statusText = stringResource(R.string.country_progress_conquest)
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
                                            Text(
                                                stringResource(R.string.country_button_use_boost),
                                                textAlign = TextAlign.Center // <-- AÑADE ESTA LÍNEA
                                            )
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
                                statusText = stringResource(R.string.country_status_conquered)
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
                            Spacer(modifier = Modifier.height(32.dp))
                            // Usamos TextWithBorder directamente aquí
                            TextWithBorder(
                                text = stringResource(R.string.country_domination_message, uiState.guardianName),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    textAlign = TextAlign.Center, // Aseguramos el centrado
                                    color = DeepNavy
                                ),
                                borderColor = Color.LightGray,
                                borderWidth = 6f,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    CountryStatus.LOCKED -> {
                        item {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = stringResource(R.string.cd_locked),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.country_status_locked_title),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(stringResource(R.string.country_status_locked_subtitle))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onBackClick) {
                        Text(stringResource(R.string.country_back_to_map))
                    }
                }
            }
        }
    }
    if (uiState.showConquestTutorialDialog) {
        AppAlertDialog(
            onDismissRequest = { viewModel.conquestTutorialShown() },
            title = stringResource(R.string.conquest_tutorial_title),
            text = stringResource(R.string.conquest_tutorial_message, uiState.countryName),
            confirmButtonText = stringResource(R.string.dialog_button_ok)
        )
    }
}

@Composable
private fun CountryProgress(current: Long, total: Long, statusText: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextWithBorder(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            borderColor = Color.White,
            borderWidth = 3f
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { current.toFloat() / total.toFloat() },
        modifier = Modifier.fillMaxWidth(0.8f).height(8.dp),
        strokeCap = StrokeCap.Round
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        TextWithBorder(
            text = "$current / $total PC",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            borderColor = Color.White,
            borderWidth = 2f // Un borde más fino para el texto más pequeño
        )
    }
}

@Composable
private fun CategoryList(categories: List<CategoryState>, onCategoryClick: (String) -> Unit) {
    Text(
        stringResource(R.string.country_available_categories),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center // <-- AÑADE ESTA LÍNEA
    )
    Spacer(modifier = Modifier.height(16.dp))
    categories.forEach { category ->
        Button(
            onClick = { onCategoryClick(category.categoryId) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Text(category.name) // <-- Simplemente usa el nombre pre-procesado.
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
        Text(stringResource(R.string.country_button_challenge_boss), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StudyTopics(topics: List<String>, viewModel: CountryViewModel = hiltViewModel()) {
    var isExpanded by remember { mutableStateOf(false) }

    if (topics.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column {
                // Fila del Título (sigue siendo clicable para expandir/colapsar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_information))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        // Usamos el nombre del guardián para el título del dossier
                        text = "INTEL: ${viewModel.uiState.collectAsState().value.guardianName.uppercase()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) stringResource(R.string.cd_collapse) else stringResource(R.string.cd_expand)
                    )
                }

                // Contenido animado y con scroll
                AnimatedVisibility(visible = isExpanded) {
                    // Usamos un Box con altura máxima para que el scroll se active solo si es necesario
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // Altura máxima antes de activar el scroll
                    ) {
                        // LazyColumn es performante y nos da el scroll automáticamente
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(topics.size) { index ->
                                // Composable para cada pista con su animación de entrada
                                AnimatedTopicItem(
                                    text = topics[index],
                                    index = index
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Un composable para una sola pista, con animación de entrada en cascada.
 */
@Composable
private fun AnimatedTopicItem(text: String, index: Int) {
    // Estado para controlar la visibilidad del item
    var isVisible by remember { mutableStateOf(false) }

    // La animación se dispara una sola vez cuando el item entra en la composición
    LaunchedEffect(Unit) {
        // El delay se basa en el índice del item en la lista, creando el efecto cascada
        delay(100L * index) // El espacio de tiempo entre lineas se hara mayor mietras mas pistas hayan
        isVisible = true
    }

    // Usamos AnimatedVisibility para el efecto de fade-in y deslizamiento
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                slideInHorizontally(
                    initialOffsetX = { -40 },
                    animationSpec = tween(durationMillis = 400)
                )
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "•",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace // Fuente de terminal
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace // Fuente de terminal
            )
        }
    }
}