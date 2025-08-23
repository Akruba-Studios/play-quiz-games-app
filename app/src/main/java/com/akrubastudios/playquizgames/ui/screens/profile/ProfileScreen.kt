package com.akrubastudios.playquizgames.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.akrubastudios.playquizgames.domain.PlayerLevelManager
import com.akrubastudios.playquizgames.ui.components.PlayerLevelIndicator
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSignOut: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Escucha el evento de cierre de sesión del ViewModel
    LaunchedEffect(Unit) {
        viewModel.signOutEvent.collect {
            onSignOut()
        }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.user == null || uiState.levelInfo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.profile_error_loading))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(
                    name = uiState.user?.displayName ?: stringResource(R.string.default_player_name),
                    imageUrl = uiState.user?.photoUrl
                )
                Spacer(modifier = Modifier.height(24.dp))
                PlayerLevelIndicator(
                    levelInfo = uiState.levelInfo!!,
                    boostCount = uiState.user?.unassignedPcBoosts ?: 0
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                StatisticsCard(
                    totalXp = uiState.user?.totalXp ?: 0,
                    conquered = uiState.user?.conqueredCountries?.size ?: 0,
                    dominated = uiState.user?.dominatedCountries?.size ?: 0
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ActionsCard(
                    onSignOutClick = { showSignOutDialog = true },
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }

    // Diálogo de confirmación para cerrar sesión
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.profile_sign_out_dialog_title)) },
            text = { Text(stringResource(R.string.profile_sign_out_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.signOut()
                    showSignOutDialog = false
                }) {
                    Text(stringResource(R.string.dialog_button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(name: String, imageUrl: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatisticsCard(totalXp: Long, conquered: Int, dominated: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.profile_stats_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            StatisticRow(icon = Icons.Default.EmojiEvents, label = stringResource(R.string.profile_stats_total_xp), value = formatNumber(totalXp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            StatisticRow(icon = Icons.Default.Flag, label = stringResource(R.string.profile_stats_conquered), value = conquered.toString())
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            StatisticRow(icon = Icons.Default.Celebration, label = stringResource(R.string.profile_stats_dominated), value = dominated.toString())
        }
    }
}

@Composable
private fun ActionsCard(
    onSignOutClick: () -> Unit,
    onSettingsClick: () -> Unit // <-- NUEVO PARÁMETRO
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.profile_account_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_button_settings))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.profile_button_sign_out))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.app_version), style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun StatisticRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatNumber(number: Long): String {
    return NumberFormat.getNumberInstance(Locale.getDefault()).format(number)
}