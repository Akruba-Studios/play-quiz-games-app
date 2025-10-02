package com.akrubastudios.playquizgames.ui.screens.createprofile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil.request.ImageRequest
import com.akrubastudios.playquizgames.R

@OptIn(ExperimentalMaterial3Api::class) // Control 1-CPS
@Composable
fun CreateProfileScreen(
    viewModel: CreateProfileViewModel = hiltViewModel(),
    onProfileCreated: () -> Unit
) {
    // Intercepta el botón "Atrás" del sistema y no hace nada.
    BackHandler(enabled = true) { /* No hacer nada */ }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onProfileCreated()
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.create_profile_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Avatar
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (uiState.useGoogleData) uiState.googlePhotoUrl else uiState.selectedAvatarUrl)
                        .crossfade(true)
                        .error(R.drawable.logo_splash)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.useGoogleData) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.create_profile_avatar_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        items(availableAvatars) { avatarUrl ->
                            val isSelected = uiState.selectedAvatarUrl == avatarUrl
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar Option",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable { viewModel.onAvatarSelected(avatarUrl) }
                                    .border(
                                        border = BorderStroke(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                        ),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onUseGoogleDataChange(!uiState.useGoogleData) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = uiState.useGoogleData,
                        onCheckedChange = { viewModel.onUseGoogleDataChange(it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.create_profile_use_google_data))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Campo de texto para el nombre de usuario
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    label = { Text(text = stringResource(id = R.string.create_profile_username_label)) },
                    singleLine = true,
                    enabled = !uiState.useGoogleData, // Se deshabilita si el checkbox está marcado
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null
                )
                if (uiState.error != null) {
                    Text(
                        text = stringResource(id = R.string.create_profile_error_empty_name),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de Continuar
                Button(
                    onClick = { viewModel.onContinueClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = stringResource(id = R.string.create_profile_button_continue))
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}