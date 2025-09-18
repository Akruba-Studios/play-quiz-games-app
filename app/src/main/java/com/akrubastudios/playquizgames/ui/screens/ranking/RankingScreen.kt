package com.akrubastudios.playquizgames.ui.screens.ranking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.akrubastudios.playquizgames.ui.screens.ranking.RankedUserUiItem
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import java.text.NumberFormat
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    viewModel: RankingViewModel = hiltViewModel(),
    navController: NavController
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // Usamos Scaffold para tener una ranura para el bottomBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cd_back)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },
        // La fila del jugador actual va en el bottomBar
        bottomBar = {
            if (!uiState.isLoading && uiState.currentUserRank != null) {
                CurrentUserRankRow(rankData = uiState.currentUserRank!!)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // <-- Usamos el padding del Scaffold
        ) {
            Text(
                text = stringResource(R.string.ranking_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    items(uiState.rankingList) { user ->
                        RankedUserItem(user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun RankedUserItem(user: RankedUserUiItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${user.rank}.", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(16.dp))
            AsyncImage(
                model = user.photoUrl,
                contentDescription = stringResource(R.string.cd_profile_picture_of, user.displayName),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.displayName, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.player_level, user.level),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold // Hacemos el nivel negrita
                    )
                    Text(
                        text = " - ${user.totalXp} XP",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
@Composable
private fun CurrentUserRankRow(
    rankData: CurrentUserRankData,
    modifier: Modifier = Modifier
) {
    val levelInfo = PlayerLevelManager.calculateLevelInfo(rankData.totalXp)
    // Esta lógica asume que el "progreso" es cuánto has avanzado en tu nivel actual.
    // No es perfecto, pero es visualmente funcional.
    val progressToNext = if (rankData.xpToNext != null && rankData.xpToNext > 0) {
        val xpInLevel = (rankData.totalXp - levelInfo.currentLevelThresholdXp).toFloat()
        val totalXpForLevel = (levelInfo.nextLevelThresholdXp - levelInfo.currentLevelThresholdXp).toFloat()
        if (totalXpForLevel > 0) (xpInLevel / totalXpForLevel).coerceIn(0f, 1f) else 0f
    } else {
        1f // El jugador es el #1
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ranking_your_position),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${rankData.rank}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.player_level, levelInfo.level),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.getDefault()).format(rankData.totalXp)} XP",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (rankData.xpToNext != null && rankData.xpToNext > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressToNext },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${NumberFormat.getNumberInstance(Locale.getDefault()).format(rankData.xpToNext)} ${stringResource(R.string.ranking_xp_to_next)}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}