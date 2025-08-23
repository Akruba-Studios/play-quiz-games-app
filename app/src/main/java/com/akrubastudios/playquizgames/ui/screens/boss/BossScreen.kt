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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.akrubastudios.playquizgames.Routes
import com.akrubastudios.playquizgames.ui.components.KeepScreenOn
import com.akrubastudios.playquizgames.ui.screens.game.QuestionImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.unit.times

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

// =====================================================
// COMPONENTES COMPACTOS REDISEÑADOS - SIN PESOS FIJOS
// =====================================================

@Composable
private fun BossHeaderFixed(
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = guardianName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedHealth },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                strokeCap = StrokeCap.Round,
                color = healthColor,
                trackColor = healthColor.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                (1..maxMistakes).forEach { i ->
                    Icon(
                        imageVector = if (i <= maxMistakes - mistakes)
                            Icons.Default.Favorite
                        else
                            Icons.Default.FavoriteBorder,
                        contentDescription = "Vida",
                        tint = Color.Magenta,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionImageFixed(imageUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 200.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Imagen de la pregunta",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun QuestionTextFixed(
    questionText: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = questionText,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            lineHeight = 30.sp,
            fontSize = 24.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnswerSlotsFixed(
    correctAnswer: String,
    userAnswer: String,
    onClear: () -> Unit
) {
    val userAnswerLetters = userAnswer
    var letterIndex = 0

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clickable { onClear() },
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally), // 18.dp Espacio entre palabras
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val words = correctAnswer.split(' ')

        // Determinar el tamaño global para todas las palabras
        val hasLongWord = words.any { it.length > 8 }
        val globalSlotSize = if (hasLongWord) 30.dp else 40.dp // 30.dp y 40.dp son los tamaños de casillas respuestas

        words.forEach { word ->

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { // 6.dp Espacio entre letras
                word.forEach { _ ->
                    val charToShow = userAnswerLetters.getOrNull(letterIndex) ?: ' '
                    Card(
                        modifier = Modifier.size(globalSlotSize),
                        colors = CardDefaults.cardColors(
                            containerColor = if (charToShow == ' ') Color.Gray.copy(alpha = 0.8f) else Color.Blue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = charToShow.toString().uppercase(),
                                color = Color.White,
                                fontSize = if (globalSlotSize < 35.dp) 14.sp else 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    letterIndex++
                }
            }
        }
    }
}

@Composable
private fun TimerAndDialogueRow(
    timeRemaining: Int,
    dialogue: String,
    phase: Int,
    isRunning: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timer
        if (isRunning) {
            val timerColor = when {
                timeRemaining > 15 -> Color.Green
                timeRemaining > 5 -> Color.Yellow
                else -> Color.Red
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = timerColor.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "⏰ ${timeRemaining}s",
                    color = timerColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Dialogue
        if (dialogue.isNotEmpty()) {
            val textColor = when (phase) {
                1 -> Color.White
                2 -> Color(0xFFFEF3C7)
                3 -> Color(0xFFFCA5A5)
                else -> Color.White
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = dialogue,
                    modifier = Modifier.padding(8.dp),
                    color = textColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LetterBankFixed(
    hintLetters: String,
    usedIndices: Set<Int>,
    difficulty: String,
    onLetterClick: (Char, Int) -> Unit
) {
    // Altura máxima disponible para el banco de letras
    val maxAvailableHeight = 280.dp

    // Calculamos columnas dinámicamente (tu lógica original)
    val columns = when {
        hintLetters.length <= 12 -> 4
        hintLetters.length <= 18 -> 5
        hintLetters.length <= 24 -> 6
        else -> 7
    }

    // Calculamos cuántas filas necesitamos
    val totalRows = (hintLetters.length + columns - 1) / columns

    // Calculamos el tamaño exacto que necesita cada botón
    val verticalSpacing = 6.dp
    val totalVerticalSpacing = (totalRows - 1) * verticalSpacing
    val contentPadding = 8.dp // 4dp arriba + 4dp abajo
    val availableHeightForButtons = maxAvailableHeight - totalVerticalSpacing - contentPadding
    val buttonSize = (availableHeightForButtons / totalRows).coerceAtLeast(28.dp).coerceAtMost(50.dp)

    // Dividimos las letras en filas
    val letterRows = hintLetters.chunked(columns)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxAvailableHeight)
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letterRows.forEachIndexed { rowIndex, rowLetters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowLetters.forEachIndexed { colIndex, letter ->
                    val globalIndex = rowIndex * columns + colIndex
                    val isUsed = usedIndices.contains(globalIndex)

                    Button(
                        onClick = { if (!isUsed) onLetterClick(letter, globalIndex) },
                        modifier = Modifier.size(buttonSize),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUsed) Color.Gray else Color.Blue,
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = !isUsed,
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = (buttonSize.value * 0.32f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
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

    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeEffect) 20f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "shake"
    )

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

// =====================================================
// COMPONENTE PRINCIPAL REDISEÑADO - SIN WEIGHTS FIJOS
// =====================================================

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

            // Contenido principal con scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HEADER DEL BOSS
                BossHeaderFixed(
                    guardianName = uiState.guardianTheme.name,
                    health = uiState.bossHealth,
                    mistakes = uiState.playerMistakes,
                    maxMistakes = uiState.maxMistakes,
                    phase = uiState.currentPhase
                )

                // 2. CONTENIDO DE LA PREGUNTA
                uiState.currentQuestion?.let { question ->
                    // Imagen de la pregunta
                    // QuestionImageFixed(imageUrl = question.imageUrl)

                    // Texto de la pregunta
                    QuestionTextFixed(questionText = question.questionText_es)

                    // Casillas de respuesta - SIN SCROLL HORIZONTAL
                    AnswerSlotsFixed(
                        correctAnswer = uiState.currentCorrectAnswer,
                        userAnswer = uiState.userAnswer,
                        onClear = { viewModel.clearUserAnswer() }
                    )

                    // Timer y diálogo en fila
                    TimerAndDialogueRow(
                        timeRemaining = uiState.timeRemaining,
                        dialogue = uiState.currentDialogue,
                        phase = uiState.currentPhase,
                        isRunning = uiState.isTimerRunning
                    )

                    // Banco de letras - SIN SCROLL
                    LetterBankFixed(
                        hintLetters = uiState.generatedHintLetters,
                        usedIndices = uiState.usedLetterIndices,
                        difficulty = "principiante",
                        onLetterClick = { letter, index ->
                            viewModel.onLetterClick(letter, index)
                        }
                    )
                }

                // Espaciado adicional para el último elemento
                Spacer(Modifier.height(32.dp))
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
// COMPONENTES ADICIONALES - NO MODIFICADOS
// =====================================================

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