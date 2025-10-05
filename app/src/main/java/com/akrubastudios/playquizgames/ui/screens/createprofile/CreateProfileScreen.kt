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
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.foundation.background
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import coil.request.ImageRequest
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.AppConstants
import com.akrubastudios.playquizgames.ui.components.ScreenBackground
import com.akrubastudios.playquizgames.ui.components.TextWithBorder
import com.akrubastudios.playquizgames.ui.theme.DeepNavy
import com.akrubastudios.playquizgames.ui.theme.LightGray

@OptIn(ExperimentalMaterial3Api::class) // Control 3-CPS
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
        ScreenBackground(
            backgroundUrl = AppConstants.ONBOARDING_BACKGROUND_URL,
            imageAlpha = 0.6f,  // 1.0f - 100% opaca, la imagen se verá con toda su fuerza
            scrimAlpha = 0.75f   // 0.7 - 70% opaco en el velo
        ) {
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextWithBorder(
                            text = stringResource(id = R.string.create_profile_title),
                            style = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                            borderColor = Color.White,
                            borderWidth = 4f
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // Avatar
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (uiState.useGoogleData) uiState.googlePhotoUrl else uiState.selectedAvatarUrl)
                                .crossfade(true)
                                .error(R.drawable.logo_splash) // Si falla la carga, se muestra el logo
                                .build()
                        ),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .border( // <-- AÑADE ESTE BLOQUE
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!uiState.useGoogleData) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TextWithBorder(
                                text = stringResource(id = R.string.create_profile_avatar_title),
                                style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                                borderColor = Color.White,
                                borderWidth = 3f
                            )
                        }
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
                        TextWithBorder(
                            text = stringResource(id = R.string.create_profile_use_google_data),
                            style = LocalTextStyle.current, // Usa el estilo por defecto del entorno
                            borderColor = Color.White,
                            borderWidth = 3f
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de texto para el nombre de usuario
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.create_profile_username_label),
                                fontWeight = FontWeight.Bold // <-- PONE EL LABEL EN NEGRITA
                            )
                        },
                        singleLine = true,
                        enabled = !uiState.useGoogleData, // Se deshabilita si el checkbox está marcado
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.error != null,
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold // <-- PONE EL TEXTO ESCRITO EN NEGRITA
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            // Fondo cuando está habilitado y no enfocado
                            unfocusedContainerColor = LightGray,
                            // Fondo cuando está habilitado y enfocado
                            focusedContainerColor = LightGray,
                            // Fondo cuando está deshabilitado
                            disabledContainerColor = LightGray.copy(alpha = 0.5f),
                            // Color del texto
                            focusedTextColor = DeepNavy,
                            unfocusedTextColor = DeepNavy,
                            disabledTextColor = DeepNavy.copy(alpha = 0.5f),
                            // Color del borde
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
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
}