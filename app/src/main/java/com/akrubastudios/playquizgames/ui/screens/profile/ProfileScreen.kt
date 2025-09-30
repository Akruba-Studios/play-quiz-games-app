package com.akrubastudios.playquizgames.ui.screens.profile

import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.DialogButtonText
import com.akrubastudios.playquizgames.ui.components.DialogText
import com.akrubastudios.playquizgames.ui.components.DialogTitle
import com.akrubastudios.playquizgames.ui.components.GemsBalanceIndicator
import com.akrubastudios.playquizgames.ui.components.GemsIndicator
import com.akrubastudios.playquizgames.ui.components.getButtonTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
@OptIn(ExperimentalMaterial3Api::class) // Control: 1-PS
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSignOut: () -> Unit,
    onSettingsClick: () -> Unit,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        viewModel.signOutEvent.collect {
            onSignOut()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.musicManager.play(MusicTrack.MAP)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showGemsTutorialDialog by remember { mutableStateOf(false) }

    // Escucha el evento de cierre de sesión del ViewModel
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    // --- LÓGICA DE ANIMACIÓN (SIN CAMBIOS, PERO AHORA SABEMOS DÓNDE SE APLICARÁ) ---
    val baseCardColor = MaterialTheme.colorScheme.surfaceContainer
    val highlightCardColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    var animationPlayed by remember { mutableStateOf(false) }
    var cardColor by remember { mutableStateOf(baseCardColor) }
    val animatedCardColor by animateColorAsState(
        targetValue = cardColor,
        animationSpec = tween(durationMillis = 1000),
        label = "MilestoneCardColorAnimation" // Cambiamos la etiqueta para claridad
    )
    var scaleAnimation by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scaleAnimation,
        animationSpec = tween(durationMillis = 1000),
        label = "ScaleAnimation"
    )
    LaunchedEffect(Unit) { // CAMBIAR de uiState.triggerMilestoneAnimation a Unit
        Log.d("MILESTONE_ANIM", "🔥 ENTRANDO - trigger: ${uiState.triggerMilestoneAnimation}, played: $animationPlayed")

        if (uiState.triggerMilestoneAnimation && !animationPlayed) {
            Log.d("MILESTONE_ANIM", "🚀 INICIANDO ANIMACIÓN")
            animationPlayed = true
            viewModel.resetMilestoneAnimation() // RESETEAR AQUÍ para que no interfiera

            try {
                Log.d("MILESTONE_ANIM", "🔄 PULSO 1 - START")
                cardColor = highlightCardColor
                scaleAnimation = 1.08f
                Log.d("MILESTONE_ANIM", "⏰ Delay 1...")
                delay(1500)
                Log.d("MILESTONE_ANIM", "⏰ Delay 1 OK")

                cardColor = baseCardColor
                scaleAnimation = 1f
                Log.d("MILESTONE_ANIM", "⏰ Delay 2...")
                delay(1500)
                Log.d("MILESTONE_ANIM", "⏰ Delay 2 OK - PULSO 1 COMPLETADO")

                Log.d("MILESTONE_ANIM", "🔄 PULSO 2 - START")
                cardColor = highlightCardColor
                scaleAnimation = 1.08f
                delay(1500)

                cardColor = baseCardColor
                scaleAnimation = 1f
                delay(1500)
                Log.d("MILESTONE_ANIM", "✅ TODO TERMINADO")

            } catch (e: Exception) {
                Log.e("MILESTONE_ANIM", "❌ ERROR: $e")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(64.dp),
                title = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.profile_title))
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // Aplicar padding
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.user == null || uiState.levelInfo == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // Aplicar padding
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.profile_error_loading))
            }
        } else {
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val horizontalPadding = remember(screenWidth) {
                when {
                    screenWidth < 340.dp -> 8.dp      // Pantallas muy angostas
                    screenWidth < 370.dp -> 10.dp     // Pantallas angostas
                    else -> 10.dp                     // Pantallas normales
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // Aplicar padding
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    val imageSize = remember(screenWidth) {
                        when {
                            screenWidth < 340.dp -> 80.dp
                            screenWidth < 370.dp -> 90.dp
                            else -> 100.dp
                        }
                    }
                    ProfileHeader(
                        name = uiState.user?.displayName
                            ?: stringResource(R.string.default_player_name),
                        imageUrl = uiState.user?.photoUrl,
                        imageSize = imageSize
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val spacerWidth = remember(screenWidth) {
                        when {
                            screenWidth < 340.dp -> 8.dp
                            screenWidth < 370.dp -> 12.dp
                            else -> 15.dp
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Indicador de Nivel (usamos un Box con peso para que ocupe el espacio disponible)
                        Box(modifier = Modifier.weight(1f)) {
                            uiState.levelInfo?.let { levelInfo ->
                                PlayerLevelIndicator(
                                    levelInfo = levelInfo,
                                    boostCount = uiState.user?.unassignedPcBoosts ?: 0
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(spacerWidth)) // Espacio entre playerlevelindicator y gemsindicator

                        // Indicador de Gemas
                        GemsIndicator(
                            gems = uiState.user?.gems ?: 0,
                            onClick = { showGemsTutorialDialog = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    StatisticsCard(
                        totalXp = uiState.user?.totalXp ?: 0,
                        conquered = uiState.user?.conqueredCountries?.size ?: 0,
                        dominated = uiState.user?.dominatedCountries?.size ?: 0,
                        cardPadding = horizontalPadding
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    uiState.nextMilestone?.let { milestone ->
                        // --- LA ANIMACIÓN SE APLICA AQUÍ, EN LA TARJETA DE HITOS ---
                        MilestoneCard(
                            milestone = milestone,
                            cardColor = animatedCardColor, // <-- APLICA EL COLOR ANIMADO
                            scale = animatedScale,
                            cardPadding = horizontalPadding
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    ActionsCard(
                        onSignOutClick = { showSignOutDialog = true },
                        onSettingsClick = onSettingsClick,
                        onLibraryClick = { navController.navigate(Routes.FUN_FACT_LIBRARY_SCREEN) },
                        isLibraryEnabled = uiState.user?.masteredLevelIds?.isNotEmpty() ?: false,
                        cardPadding = horizontalPadding
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para cerrar sesión
    if (showSignOutDialog) {
        AppAlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { DialogTitle(text = stringResource(R.string.profile_sign_out_dialog_title)) },
            text = { DialogText(text = stringResource(R.string.profile_sign_out_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.signOut()
                    showSignOutDialog = false
                }) {
                    DialogButtonText(text = stringResource(R.string.dialog_button_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    DialogButtonText(text = stringResource(R.string.dialog_button_cancel))
                }
            }
        )
    }
    if (showGemsTutorialDialog) {
        AppAlertDialog(
            onDismissRequest = { showGemsTutorialDialog = false },
            title = stringResource(R.string.gems_tutorial_title),
            text = stringResource(R.string.gems_tutorial_message),
            confirmButtonText = stringResource(R.string.dialog_button_ok)
        )
    }
}

@Composable
private fun ProfileHeader(name: String, imageUrl: String?, imageSize: Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto de Perfil",
            modifier = Modifier
                .size(imageSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatisticsCard(totalXp: Long, conquered: Int, dominated: Int, cardPadding: Dp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
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
    onSettingsClick: () -> Unit, // <-- NUEVO PARÁMETRO
    onLibraryClick: () -> Unit,
    isLibraryEnabled: Boolean,
    cardPadding: Dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(stringResource(R.string.profile_account_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    if (isLibraryEnabled) {
                        // Si está habilitado, navega a la biblioteca.
                        onLibraryClick()
                    } else {
                        // Si no, muestra nuestro Toast personalizado.
                        val inflater = LayoutInflater.from(context)
                        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

                        val textView = layout.findViewById<TextView>(R.id.toast_text)
                        textView.text = context.getString(R.string.library_unlock_toast)

                        Toast(context).apply {
                            duration = Toast.LENGTH_LONG // Duración larga para que se pueda leer bien
                            view = layout
                            show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Hacemos que el botón PAREZCA deshabilitado cambiando sus colores.
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isLibraryEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isLibraryEnabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    stringResource(R.string.profile_button_library),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.profile_button_settings),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.profile_button_sign_out),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.app_version),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
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

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    modifier: Modifier = Modifier,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    scale: Float = 1f,
    cardPadding: Dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(cardPadding)) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Creamos una fila por cada meta dentro del hito.
            milestone.goals.forEach { goal ->
                GoalProgressRow(goal = goal)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun GoalProgressRow(goal: GoalProgress) {
    val progress = (goal.current.toFloat() / goal.target.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "GoalProgressAnimation")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = goal.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${goal.current} / ${goal.target}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            strokeCap = StrokeCap.Round
        )
    }
}