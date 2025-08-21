package com.akrubastudios.playquizgames.ui.screens.boss

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.ui.components.KeepScreenOn
import com.akrubastudios.playquizgames.ui.screens.game.AnswerSlots
import com.akrubastudios.playquizgames.ui.screens.game.LetterBank
import com.akrubastudios.playquizgames.ui.screens.game.QuestionImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Datos para las partículas de confeti
data class Particle(
    val x: Float,
    val y: Float,
    val velX: Float,
    val velY: Float,
    val color: Color,
    val size: Float,
    val life: Float
)

@Composable
fun BossScreen(
    navController: NavController,
    viewModel: BossViewModel = hiltViewModel()
) {
    KeepScreenOn()
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    // Navegación al final del juego
    LaunchedEffect(gameResult) {
        gameResult?.let { result ->
            // Enviar score request
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val db = FirebaseFirestore.getInstance()
                val scoreRequest = hashMapOf(
                    "userId" to uid,
                    "score" to result.score,
                    "starsEarned" to result.starsEarned,
                    "levelId" to viewModel.levelId,
                    "countryId" to viewModel.countryId,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("score_requests").add(scoreRequest)
            }

            val isVictory = result.starsEarned == 3
            val route = Routes.RESULT_SCREEN
                .replace("{score}", result.score.toString())
                .replace("{totalQuestions}", result.totalQuestions.toString())
                .replace("{correctAnswers}", result.correctAnswers.toString())
                .replace("{starsEarned}", result.starsEarned.toString())
                .replace("{levelId}", viewModel.levelId)
                .replace("{countryId}", viewModel.countryId)
                .replace("{difficulty}", "dificil")
                .replace("{isFromBossFight}", "true")
                .replace("{victory}", isVictory.toString())

            navController.navigate(route) {
                popUpTo(Routes.MAP_SCREEN)
            }
        }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFD4AF37))
        }
    } else {
        Box(Modifier.fillMaxSize()) {
            // Fondo dinámico por fases
            DynamicBackground(
                phase = uiState.currentPhase,
                shakeEffect = uiState.showShakeEffect
            )

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp), // ← REDUCIDO de 16dp a 8dp
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. HEADER COMPACTO (20% del espacio)
                Box(modifier = Modifier.fillMaxWidth().weight(0.2f)) {
                    EpicBossHeaderCompact(
                        guardianName = uiState.guardianTheme.name,
                        health = uiState.bossHealth,
                        mistakes = uiState.playerMistakes,
                        maxMistakes = uiState.maxMistakes,
                        phase = uiState.currentPhase
                    )
                }

                // 2. TIMER COMPACTO (5% del espacio)
                Box(modifier = Modifier.fillMaxWidth().weight(0.05f)) {
                    TimerDisplayCompact(
                        timeRemaining = uiState.timeRemaining,
                        phase = uiState.currentPhase,
                        isRunning = uiState.isTimerRunning
                    )
                }

                // 3. DIÁLOGO COMPACTO (8% del espacio)
                Box(modifier = Modifier.fillMaxWidth().weight(0.08f)) {
                    GuardianDialogueCompact(
                        dialogue = uiState.currentDialogue,
                        phase = uiState.currentPhase
                    )
                }

                // 4. CONTENIDO DE PREGUNTA (40% del espacio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f),
                    contentAlignment = Alignment.Center
                ) {
                    uiState.currentQuestion?.let { question ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Imagen muy compacta
                            QuestionImageCompact(imageUrl = question.imageUrl)

                            // Texto muy compacto
                            Text(
                                text = question.questionText_es,
                                style = MaterialTheme.typography.bodyMedium, // ← MÁS PEQUEÑO
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2, // ← MÁXIMO 2 LÍNEAS
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp, // ← TAMAÑO FIJO PEQUEÑO
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // AnswerSlots adaptables
                            AnswerSlotsCompact(
                                correctAnswer = question.correctAnswer,
                                userAnswer = uiState.userAnswer,
                                onClear = { viewModel.clearUserAnswer() }
                            )
                        }
                    }
                }

                // 5. BOTONES DE LETRAS (27% del espacio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.27f),
                    contentAlignment = Alignment.Center
                ) {
                    uiState.currentQuestion?.let { question ->
                        LetterBankCompact(
                            hintLetters = uiState.generatedHintLetters,
                            usedIndices = uiState.usedLetterIndices,
                            difficulty = "principiante",
                            onLetterClick = { letter, index ->
                                viewModel.onLetterClick(letter, index)
                            }
                        )
                    }
                }
            }

            // Overlay de transición de fase
            AnimatedVisibility(
                visible = uiState.isPhaseTransition,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                PhaseTransitionOverlay(
                    phase = uiState.currentPhase,
                    onDismiss = { viewModel.dismissPhaseTransition() }
                )
            }

            // Animación de victoria
            if (uiState.showVictoryAnimation) {
                VictorySequence(
                    guardianName = uiState.guardianTheme.name,
                    battleStats = uiState.battleStats
                )
            }
        }
    }
}

// =====================================================
// 2. COMPONENTES COMPACTOS - AÑADIR AL FINAL DE BossScreen.kt
// =====================================================

@Composable
private fun EpicBossHeaderCompact(
    guardianName: String,
    health: Float,
    mistakes: Int,
    maxMistakes: Int,
    phase: Int
) {
    val animatedHealth by animateFloatAsState(
        targetValue = health,
        animationSpec = tween(durationMillis = 800),
        label = "BossHealth"
    )

    val healthColor = when {
        animatedHealth > 0.7f -> Color.Green
        animatedHealth > 0.3f -> Color.Yellow
        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp) // ← MÁS PEQUEÑO
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp) // ← REDUCIDO
        ) {
            Text(
                text = guardianName,
                style = MaterialTheme.typography.titleMedium, // ← MÁS PEQUEÑO
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp, // ← REDUCIDO
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { animatedHealth },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp), // ← MÁS PEQUEÑO
                strokeCap = StrokeCap.Round,
                color = healthColor,
                trackColor = healthColor.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(4.dp))

            Row {
                (1..maxMistakes).forEach { i ->
                    Icon(
                        imageVector = if (i <= maxMistakes - mistakes)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Vida",
                        tint = Color.Magenta,
                        modifier = Modifier.size(18.dp) // ← MÁS PEQUEÑO
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerDisplayCompact(
    timeRemaining: Int,
    phase: Int,
    isRunning: Boolean
) {
    if (!isRunning) return

    val timerColor = when {
        timeRemaining > 15 -> Color.Green
        timeRemaining > 5 -> Color.Yellow
        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = timerColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "⏰ ${timeRemaining}s",
            color = timerColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp, // ← MÁS PEQUEÑO
            modifier = Modifier.padding(6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GuardianDialogueCompact(
    dialogue: String,
    phase: Int
) {
    if (dialogue.isEmpty()) return

    val textColor = when (phase) {
        1 -> Color.White
        2 -> Color(0xFFFEF3C7)
        3 -> Color(0xFFFCA5A5)
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = dialogue,
            modifier = Modifier.padding(6.dp),
            color = textColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            fontSize = 10.sp, // ← MÁS PEQUEÑO
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuestionImageCompact(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Imagen de la pregunta",
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // ← MUY PEQUEÑO PERO VISIBLE
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun AnswerSlotsCompact(
    correctAnswer: String,
    userAnswer: String,
    onClear: () -> Unit
) {
    // Usar LazyRow para respuestas muy largas
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(correctAnswer.length) { index ->
            val char = if (index < userAnswer.length) userAnswer[index] else ' '
            Card(
                modifier = Modifier
                    .size(28.dp), // ← MÁS PEQUEÑO
                colors = CardDefaults.cardColors(
                    containerColor = if (char == ' ') Color.Gray else Color.Blue
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char.toString(),
                        color = Color.White,
                        fontSize = 14.sp, // ← MÁS PEQUEÑO
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterBankCompact(
    hintLetters: String,
    usedIndices: Set<Int>,
    difficulty: String,
    onLetterClick: (Char, Int) -> Unit
) {
    // Grid compacto que siempre quepa
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 35.dp), // ← BOTONES MÁS PEQUEÑOS
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items(hintLetters.length) { index ->
            val letter = hintLetters[index]
            val isUsed = usedIndices.contains(index)

            Button(
                onClick = { if (!isUsed) onLetterClick(letter, index) },
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUsed) Color.Gray else Color.Blue,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isUsed,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 12.sp, // ← MÁS PEQUEÑO
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DynamicBackground(
    phase: Int,
    shakeEffect: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundTransition")

    // Shake effect
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeEffect) 20f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "shake"
    )

    // Pulsing effect para fase 3
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (phase == 3) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val backgroundBrush = when (phase) {
        1 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E3A8A).copy(alpha = 0.7f),
                Color(0xFF3B82F6).copy(alpha = 0.5f)
            )
        )
        2 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF59E0B).copy(alpha = pulseAlpha),
                Color(0xFFEF4444).copy(alpha = 0.6f)
            )
        )
        3 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFDC2626).copy(alpha = pulseAlpha),
                Color(0xFF7F1D1D).copy(alpha = 0.8f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = shakeOffset.dp, y = shakeOffset.dp)
            .background(backgroundBrush)
    )
}

@Composable
private fun EpicBossHeader(
    guardianName: String,
    health: Float,
    mistakes: Int,
    maxMistakes: Int,
    phase: Int
) {
    val animatedHealth by animateFloatAsState(
        targetValue = health,
        animationSpec = tween(durationMillis = 800),
        label = "BossHealth"
    )

    val healthColor = when {
        animatedHealth > 0.7f -> Color.Green
        animatedHealth > 0.3f -> Color.Yellow
        else -> Color.Red
    }

    val pulseScale by rememberInfiniteTransition(label = "healthPulse").animateFloat(
        initialValue = 1f,
        targetValue = if (phase == 3) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = guardianName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedHealth },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                strokeCap = StrokeCap.Round,
                color = healthColor,
                trackColor = healthColor.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(8.dp))

            Row {
                (1..maxMistakes).forEach { i ->
                    Icon(
                        imageVector = if (i <= maxMistakes - mistakes)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Vida",
                        tint = Color.Magenta,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuardianDialogue(
    dialogue: String,
    phase: Int
) {
    if (dialogue.isNotEmpty()) {
        val textColor = when (phase) {
            1 -> Color.White
            2 -> Color(0xFFFEF3C7)
            3 -> Color(0xFFFCA5A5)
            else -> Color.White
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "💬 $dialogue",
                modifier = Modifier.padding(12.dp),
                color = textColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PhaseTransitionOverlay(
    phase: Int,
    onDismiss: () -> Unit
) {
    val transitionText = when (phase) {
        2 -> "⚡ EL GUARDIÁN SE AGITA ⚡"
        3 -> "🔥 ¡FASE FINAL DE FURIA! 🔥"
        else -> "⭐ NUEVA FASE ⭐"
    }

    val backgroundColor = when (phase) {
        2 -> Color(0xFFF59E0B).copy(alpha = 0.9f)
        3 -> Color(0xFFDC2626).copy(alpha = 0.9f)
        else -> Color.Black.copy(alpha = 0.8f)
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000L)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = transitionText,
                modifier = Modifier.padding(24.dp),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VictorySequence(
    guardianName: String,
    battleStats: BattleStats
) {
    val particles = remember { generateVictoryParticles() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        // Partículas de confeti
        VictoryParticles(particles = particles)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD4AF37).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🏆 ¡VICTORIA ÉPICA! 🏆",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "¡Has dominado al $guardianName!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Estadísticas de batalla
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 ESTADÍSTICAS DE BATALLA",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "⏱️ Tiempo: ${formatBattleTime(battleStats.totalTime)}",
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Text(
                            text = "🎯 Precisión: ${battleStats.accuracy.toInt()}%",
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Text(
                            text = "🔥 Racha máxima: ${battleStats.longestStreak}",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerDisplay(
    timeRemaining: Int,
    phase: Int,
    isRunning: Boolean
) {
    if (!isRunning) return

    val timerColor = when {
        timeRemaining > 15 -> Color.Green
        timeRemaining > 5 -> Color.Yellow
        else -> Color.Red
    }

    val pulseScale by rememberInfiniteTransition(label = "timerPulse").animateFloat(
        initialValue = 1f,
        targetValue = if (timeRemaining <= 5) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (timeRemaining <= 5) pulseScale else 1f),
        colors = CardDefaults.cardColors(
            containerColor = timerColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timer, // Si no tienes este icon, usa cualquier otro
                contentDescription = "Timer",
                tint = timerColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "⏰ ${timeRemaining}s",
                color = timerColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun VictoryParticles(particles: List<Particle>) {
    var animatedParticles by remember { mutableStateOf(particles) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 3000L) {
            animatedParticles = animatedParticles.map { particle ->
                particle.copy(
                    x = particle.x + particle.velX,
                    y = particle.y + particle.velY,
                    life = particle.life - 0.02f
                )
            }.filter { it.life > 0f }
            kotlinx.coroutines.delay(16L) // ~60fps
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animatedParticles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.life),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

private fun generateVictoryParticles(): List<Particle> {
    val colors = listOf(
        Color(0xFFD4AF37), // Oro
        Color(0xFFFFD700), // Amarillo dorado
        Color(0xFFFFA500), // Naranja
        Color(0xFFFF6B6B), // Rosa
        Color(0xFF4ECDC4), // Turquesa
        Color(0xFF45B7D1)  // Azul
    )

    return (0..50).map {
        val angle = Random.nextFloat() * 2 * Math.PI
        val speed = Random.nextFloat() * 5 + 2

        Particle(
            x = Random.nextFloat() * 1000,
            y = Random.nextFloat() * 1000,
            velX = (cos(angle) * speed).toFloat(),
            velY = (sin(angle) * speed).toFloat(),
            color = colors.random(),
            size = Random.nextFloat() * 8 + 4,
            life = 1f
        )
    }
}

private fun formatBattleTime(timeMs: Long): String {
    val seconds = (timeMs / 1000) % 60
    val minutes = (timeMs / (1000 * 60)) % 60
    return if (minutes > 0) {
        "${minutes}m ${seconds}s"
    } else {
        "${seconds}s"
    }
}