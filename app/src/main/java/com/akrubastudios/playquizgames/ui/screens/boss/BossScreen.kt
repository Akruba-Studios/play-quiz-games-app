package com.akrubastudios.playquizgames.ui.screens.boss

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Diamond
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
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.unit.times
import kotlin.math.max

import androidx.compose.ui.res.stringResource
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.ui.components.GemsBalanceIndicator

import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.ui.components.GemIcon
import com.akrubastudios.playquizgames.ui.components.GemIconDarkGold
import com.akrubastudios.playquizgames.ui.components.GemsIndicator
import com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent
import com.akrubastudios.playquizgames.ui.theme.DeepNavy
import com.akrubastudios.playquizgames.ui.theme.GoldAccent
import com.akrubastudios.playquizgames.ui.theme.LightGray
import com.akrubastudios.playquizgames.ui.theme.SkyBlue

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
    phase: Int,
    currentGems: Int,
    onGemsClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val cardPadding = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 12.dp    // Zona crítica
            screenWidth < 370.dp -> 14.dp    // Zona transición
            else -> 16.dp                    // Zona normal (actual)
        }
    }

    val heartSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 24.dp    // Zona crítica
            screenWidth < 370.dp -> 26.dp    // Zona transición
            else -> 28.dp                    // Zona normal (actual)
        }
    }

    val progressBarHeight = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 10.dp    // Zona crítica
            screenWidth < 370.dp -> 11.dp    // Zona transición
            else -> 12.dp                    // Zona normal (actual)
        }
    }

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
        modifier = Modifier.fillMaxWidth().padding(horizontal = cardPadding),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                GemsIndicator(
                    gems = currentGems,
                    onClick = onGemsClick,
                    forceTheme = lightColorScheme(),
                    maxScaleFactor = 1.1f // Valor más pequeño para evitar overflow
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedHealth },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(progressBarHeight),
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
                        contentDescription = stringResource(R.string.cd_life),
                        tint = Color.Red,
                        modifier = Modifier.size(heartSize)
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
            contentDescription = stringResource(R.string.cd_question_image_boss),
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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val initialFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 16f      // Zona crítica (era 20f)
            screenWidth < 370.dp -> 18f      // Zona transición
            else -> 20f                      // Zona normal (actual)
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxLines = 4 // cantidad de filas maximas que se pueden generar en las preguntas, despues se achican
    val baseTextStyle = MaterialTheme.typography.bodyLarge

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DarkGoldAccent),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(16.dp)
        ) {
            val maxWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx().toInt() }

            // Calculamos el fontSize óptimo
            val optimalFontSize = remember(questionText, maxWidthPx) {
                var currentFontSize = initialFontSize
                val minFontSize = 6f // Tamaño mínimo aceptado

                Log.d("DEBUG", "Calculando para: ${questionText.take(50)}...")
                Log.d("DEBUG", "fontSize inicial: $currentFontSize")

                while (currentFontSize >= minFontSize) {
                    val textStyle = baseTextStyle.copy(
                        fontSize = currentFontSize.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 30.sp
                    )

                    val textLayoutResult = textMeasurer.measure(
                        text = questionText,
                        style = textStyle,
                        constraints = Constraints(maxWidth = maxWidthPx)
                    )

                    Log.d("DEBUG", "fontSize: $currentFontSize, líneas: ${textLayoutResult.lineCount}")

                    if (textLayoutResult.lineCount <= maxLines) {
                        break
                    }

                    currentFontSize *= 0.70f
                }

                max(currentFontSize, minFontSize)
            }

            Text(
                text = questionText,
                style = baseTextStyle.copy(
                    fontSize = optimalFontSize.sp,
                    lineHeight = 30.sp
                ),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = maxLines
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnswerSlotsFixed(
    correctAnswer: String,
    userAnswer: String,
    revealedLetterPositions: Set<Int>,
    onClear: () -> Unit,
    showClearAnimation: Boolean
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val (minSlotSize, maxSlotSize) = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> Pair(26.dp, 35.dp)
            screenWidth < 370.dp -> Pair(28.dp, 38.dp)
            else -> Pair(28.dp, 40.dp)
        }
    }

    // La lógica de animación no cambia
    val clearOffsetX by animateFloatAsState(
        targetValue = if (showClearAnimation) {
            if ((System.currentTimeMillis() / 80) % 2 == 0L) -8f else 8f
        } else 0f,
        animationSpec = tween(80),
        label = "boss_clear_shake"
    )
    val clearAlpha by animateFloatAsState(
        targetValue = if (showClearAnimation) 0.3f else 1.0f,
        animationSpec = tween(200),
        label = "boss_clear_fade"
    )

    // La lógica de cálculo de tamaño dinámico se queda
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val availableWidth = screenWidthDp
    val words = correctAnswer.split(' ')
    val longestWord = words.maxByOrNull { it.length } ?: ""

    // 2. Todos los cálculos ahora se basan en la longitud de esa palabra.
    val totalLetters = longestWord.length
    if (totalLetters > 0) { // Evitar división por cero si la respuesta está vacía
        val totalLetterGaps = (totalLetters - 1).coerceAtLeast(0)
        val letterSpacing = 6.dp
        val wordSpacing = 18.dp
        val totalSpacing = letterSpacing * totalLetterGaps

        // 3. El tamaño se calcula para que la palabra MÁS LARGA quepa.
        val globalSlotSize = ((availableWidth - totalSpacing) / totalLetters).coerceIn(minSlotSize, maxSlotSize)

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = clearOffsetX.dp)
                .alpha(clearAlpha)
                .padding(vertical = 12.dp)
                .clickable { onClear() },
            horizontalArrangement = Arrangement.spacedBy(wordSpacing, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var letterIndex = 0
            val userAnswerLetters = userAnswer

            words.forEach { word ->
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()), // <-- AÑADE ESTA LÍNEA
                    horizontalArrangement = Arrangement.spacedBy(letterSpacing)
                ) {
                    word.forEach { _ ->
                        val charToShow = userAnswerLetters.getOrNull(letterIndex) ?: ' '
                        val isRevealedLetter = revealedLetterPositions.contains(letterIndex)

                        // --- INICIO DEL CÓDIGO RESTAURADO ---
                        // Este es tu código original del Card, 100% intacto.
                        // Lo único que cambia es que 'globalSlotSize' ahora es dinámico.
                        Card(
                            modifier = Modifier.size(globalSlotSize),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            border = if (charToShow == ' ') BorderStroke(
                                1.dp,
                                GoldAccent
                            ) else null,
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    charToShow == ' ' -> LightGray
                                    isRevealedLetter -> Color(0xFF4CAF50) // Verde para letras reveladas
                                    else -> SkyBlue // Letras Normales
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = charToShow.toString().uppercase(),
                                    color = if (charToShow == ' ') Color.Transparent else Color.White,
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
                AdaptiveDialogueText(
                    text = dialogue,
                    color = textColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun AdaptiveDialogueText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxLines = 1 // Máximo 1 línea
    val initialFontSize = 12f // Tamaño inicial
    val baseTextStyle = MaterialTheme.typography.bodyMedium

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx().toInt() }

        val optimalFontSize = remember(text, maxWidthPx) {
            var currentFontSize = initialFontSize
            val minFontSize = 6f // Tamaño mínimo

            while (currentFontSize >= minFontSize) {
                val textStyle = baseTextStyle.copy(
                    fontSize = currentFontSize.sp,
                    fontWeight = FontWeight.Medium
                )

                val textLayoutResult = textMeasurer.measure(
                    text = text,
                    style = textStyle,
                    constraints = Constraints(maxWidth = maxWidthPx)
                )

                if (textLayoutResult.lineCount <= maxLines) {
                    break
                }

                currentFontSize *= 0.80f
            }

            max(currentFontSize, minFontSize)
        }

        Text(
            text = text,
            style = baseTextStyle.copy(
                fontSize = optimalFontSize.sp
            ),
            color = color,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = maxLines
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterBankFixed(
    hintLetters: String,
    usedIndices: Set<Int>,
    difficulty: String, // No se usa pero lo mantenemos por consistencia
    availableHeight: Dp,
    availableWidth: Dp,
    onLetterClick: (Char, Int) -> Unit
) {
    val buttonSize = remember(availableHeight, availableWidth, hintLetters.length) {
        calculateOptimalButtonSizeBoss(
            availableHeight = availableHeight,
            availableWidth = availableWidth,
            totalLetters = hintLetters.length
        ).also { size ->
            Log.d("BossButtonSize", "Question: availableHeight=$availableHeight, availableWidth=$availableWidth, letters=${hintLetters.length}, buttonSize=$size")
        }
    }
    // 1. Box como contenedor principal con altura FIJA y estilo
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .verticalScroll(rememberScrollState()) // borrar el log y luego poner la coma aqui ),

            .onSizeChanged { size -> // ojo borrar todo esto despues
                Log.d("LetterBankReal", "Real height used: ${size.height}px")
            }, // <-- AQUÍ

        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        maxItemsInEachRow = 7 // Mantenemos el límite máximo
    ) {
        hintLetters.forEachIndexed { index, letter ->
            val isUsed = usedIndices.contains(index)
            Button(
                onClick = { if (!isUsed) onLetterClick(letter, index) },
                modifier = Modifier
                    .padding(horizontal = 2.dp) // <-- AGREGAR ESTA LÍNEA
                    .size(buttonSize),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUsed) Color.Gray else SkyBlue,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isUsed,
                contentPadding = PaddingValues(2.dp)
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 18.sp,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossScreen(
    navController: NavController,
    viewModel: BossViewModel = hiltViewModel()
) {
    DisposableEffect(Unit) {
        // Se ejecuta cuando la pantalla APARECE
        viewModel.musicManager.play(MusicTrack.BOSS)

        // Se ejecuta cuando la pantalla DESAPARECE
        onDispose {
            viewModel.musicManager.stop()
        }
    }
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
                    "timestamp" to System.currentTimeMillis(),
                    "correctAnswers" to result.correctAnswers,
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
                .replace("{pcGained}", "0")
                .replace("{gemsGained}", "0")
                .replace("{categoryId}", "boss") // Valor dummy, ya que no pertenece a una categoría
                .replace("{continentId}", "boss") // Valor dummy
                .replace("{origin}", "bossfight") // Origen específico para este flujo
                .replace("{previousBestStars}", "0") // No aplica, ponemos 0

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
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. HEADER DEL BOSS
                BossHeaderFixed(
                    guardianName = uiState.guardianTheme.name,
                    health = uiState.bossHealth,
                    mistakes = uiState.playerMistakes,
                    maxMistakes = uiState.maxMistakes,
                    phase = uiState.currentPhase,
                    currentGems = uiState.currentGems,
                    onGemsClick = { viewModel.openHelpsSheet() }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2. CONTENIDO DE LA PREGUNTA
                uiState.currentQuestion?.let { question ->
                    // Imagen de la pregunta
                    // QuestionImageFixed(imageUrl = question.imageUrl)

                    // Texto de la pregunta
                    QuestionTextFixed(
                        questionText = uiState.questionText
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Espacio reducido

                    // Casillas de respuesta - SIN SCROLL HORIZONTAL
                    AnswerSlotsFixed(
                        correctAnswer = uiState.currentCorrectAnswer,
                        userAnswer = uiState.userAnswer,
                        revealedLetterPositions = uiState.revealedLetterPositions,
                        onClear = { viewModel.clearUserAnswer() },
                        showClearAnimation = uiState.showClearAnimation
                    )

                    // Timer y diálogo en fila
                    TimerAndDialogueRow(
                        timeRemaining = uiState.timeRemaining,
                        dialogue = uiState.currentDialogue,
                        phase = uiState.currentPhase,
                        isRunning = uiState.isTimerRunning
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Espacio reducido

                    // Banco de letras
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val availableHeight = this@BoxWithConstraints.maxHeight

                        LetterBankFixed(
                            hintLetters = uiState.generatedHintLetters,
                            usedIndices = uiState.usedLetterIndices,
                            difficulty = "principiante",
                            availableHeight = availableHeight,
                            availableWidth = this@BoxWithConstraints.maxWidth,
                            onLetterClick = { letter, index ->
                                viewModel.onLetterClick(letter, index)
                            }
                        )
                    }
                }
                // Si no hay pregunta, mostramos un Spacer para centrar el Header
                if (uiState.currentQuestion == null) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            // Efecto spray para respuestas
            // Efecto spray verde
            AnimatedVisibility(
                visible = uiState.showCorrectEffect,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(500))
            ) {
                SprayEffectOverlay(isCorrect = true)
            }

            // Efecto spray rojo
            AnimatedVisibility(
                visible = uiState.showIncorrectEffect,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(500))
            ) {
                SprayEffectOverlay(isCorrect = false)
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
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        if (uiState.showHelpsSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeHelpsSheet() },
                sheetState = sheetState,
                containerColor = Color.Black.copy(alpha = 0.8f)
            ) {
                HelpsContent(
                    uiState = uiState,
                    onExtraTimeClick = { viewModel.useExtraTimeHelp() },
                    onRemoveLettersClick = { viewModel.useRemoveLettersHelp() },
                    onRevealLetterClick = { viewModel.useRevealLetterHelp() },
                    onShowHintClick = { viewModel.useShowHintHelp() }
                )
            }
        }
        // PENDIENTE PERSOLINAR COLORES VALIDANDO SI USARA APPALERTDIALOG
        if (uiState.showFunFactDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onFunFactDialogDismissed() },
                containerColor = Color.Black.copy(alpha = 0.8f),
                title = {
                    Text(
                        text = stringResource(R.string.fun_fact_title),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = uiState.currentFunFact,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    IconButton(onClick = { viewModel.onFunFactDialogDismissed() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = Color.White
                        )
                    }
                }
            )
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
        2 -> stringResource(R.string.boss_phase_transition_2)
        3 -> stringResource(R.string.boss_phase_transition_3)
        else -> stringResource(R.string.boss_phase_transition_default)
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
                    stringResource(R.string.boss_victory_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.boss_victory_subtitle, guardianName),
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
                            stringResource(R.string.boss_battle_stats_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            stringResource(R.string.boss_battle_stats_time, formatBattleTime(battleStats.totalTime)),
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Text(
                            stringResource(R.string.boss_battle_stats_accuracy, battleStats.accuracy.toInt()),
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Text(
                            stringResource(R.string.boss_battle_stats_streak, battleStats.longestStreak),
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

@Composable
private fun HelpsContent(
    uiState: BossState,
    onExtraTimeClick: () -> Unit,
    onRemoveLettersClick: () -> Unit,
    onRevealLetterClick: () -> Unit,
    onShowHintClick: () -> Unit
) {
    if (uiState.isProcessingHelp) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(min = 400.dp, max = 550.dp) // Altura específica para mostrar ~3.3 items
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.helps_menu_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )

                // Timer prominente en rojo
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⏰ ${uiState.timeRemaining}s",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            HelpItem(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.help_item_extra_time_title),
                description = stringResource(R.string.help_item_extra_time_description, BossViewModel.HELP_EXTRA_TIME_SECONDS),
                cost = BossViewModel.HELP_EXTRA_TIME_COST,
                currentGems = uiState.currentGems,
                isUsed = uiState.isExtraTimeUsed,
                onClick = onExtraTimeClick
            )
            Spacer(modifier = Modifier.height(12.dp)) // Espacio entre items

            // Ayuda 2: Eliminar Letras
            HelpItem(
                icon = Icons.Default.DeleteSweep,
                title = stringResource(R.string.help_item_remove_letters_title),
                description = stringResource(R.string.help_item_remove_letters_description),
                cost = BossViewModel.HELP_REMOVE_LETTERS_COST,
                currentGems = uiState.currentGems,
                isUsed = uiState.isRemoveLettersUsed,
                onClick = onRemoveLettersClick
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Ayuda 3. Mostrar Letras. Calculamos el costo dinámicamente
            val revealLetterCost = BossViewModel.HELP_REVEAL_LETTER_COST_INITIAL + (uiState.revealLetterUses * BossViewModel.HELP_REVEAL_LETTER_COST_INCREMENT)

            HelpItem(
                icon = Icons.Default.VpnKey,
                title = stringResource(R.string.help_item_reveal_letter_title),
                description = stringResource(R.string.help_item_reveal_letter_description),
                cost = revealLetterCost,
                currentGems = uiState.currentGems,
                // La ayuda se considera "usada" para deshabilitarla si ya no quedan letras por revelar.
                isUsed = uiState.revealLetterUses >= uiState.currentCorrectAnswer.count { it.isLetter() },
                onClick = onRevealLetterClick
            )
            // Ayuda 4. mostrar pistas
            Spacer(modifier = Modifier.height(12.dp))

            HelpItem(
                icon = Icons.Default.Lightbulb,
                title = stringResource(R.string.help_item_show_hint_title),
                description = stringResource(R.string.help_item_show_hint_description),
                cost = BossViewModel.HELP_SHOW_HINT_COST,
                currentGems = uiState.currentGems,
                isUsed = uiState.isShowHintUsed,
                onClick = onShowHintClick
            )
            Spacer(modifier = Modifier.height(32.dp)) // Espacio extra para asegurar visibilidad
        }
    }
}

@Composable
private fun HelpItem(
    icon: ImageVector,
    title: String,
    description: String,
    cost: Int,
    currentGems: Int,
    isUsed: Boolean,
    onClick: () -> Unit
) {
    val canAfford = currentGems >= cost
    val isEnabled = !isUsed && canAfford

    val cardBackgroundColor = if (isEnabled) {
        DeepNavy // Azul oscuro para tarjetas activas
    } else {
        Color.DarkGray.copy(alpha = 0.5f) // Gris oscuro translúcido para inactivas
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF00BCD4)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
            Button(
                onClick = onClick,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGoldAccent,
                    contentColor = Color.White,
                    disabledContainerColor = DarkGoldAccent.copy(alpha = 0.5f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = cost.toString())
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Diamond, contentDescription = stringResource(R.string.cd_gems), modifier = Modifier.size(16.dp))
                }
            }
        }
        if (isUsed) {
            Text(
                text = stringResource(R.string.help_item_used_label),
                modifier = Modifier.align(Alignment.End).padding(end = 12.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SprayEffectOverlay(isCorrect: Boolean) {
    val color = if (isCorrect) Color.Green else Color.Red

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Crear múltiples gradientes radiales para efecto spray
        val gradients = listOf(
            // Esquina superior izquierda
            Pair(Offset(0f, 0f), size.width * 0.4f),
            // Esquina superior derecha
            Pair(Offset(size.width, 0f), size.width * 0.4f),
            // Esquina inferior izquierda
            Pair(Offset(0f, size.height), size.width * 0.4f),
            // Esquina inferior derecha
            Pair(Offset(size.width, size.height), size.width * 0.4f),
            // Bordes centrales
            Pair(Offset(centerX, 0f), size.width * 0.3f), // Top
            Pair(Offset(centerX, size.height), size.width * 0.3f), // Bottom
            Pair(Offset(0f, centerY), size.height * 0.3f), // Left
            Pair(Offset(size.width, centerY), size.height * 0.3f) // Right
        )

        gradients.forEach { (center, radius) ->
            val brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.6f),
                    color.copy(alpha = 0.3f),
                    color.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            )

            drawRect(brush = brush)
        }
    }
}
// Calculos para el Letterbankfixed
private fun calculateOptimalButtonSizeBoss(
    availableHeight: Dp,
    availableWidth: Dp,
    totalLetters: Int
): Dp {
    // Constantes del layout
    val buttonHorizontalPadding = 4.dp
    val totalButtonPadding = buttonHorizontalPadding * 2 // 8dp por botón
    val containerHorizontalPadding = 8.dp * 2 // Cambiar de 16.dp * 2 a 8.dp * 2
    val verticalSpacing = 6.dp // BossScreen usa 6dp vs 2dp de GameScreen

    // Ancho efectivo disponible para botones
    val effectiveWidth = availableWidth - containerHorizontalPadding

    // Definir rangos de tamaño
    val minButtonSize = 36.dp
    val maxButtonSize = 68.dp

    // Encontrar el tamaño óptimo
    return findOptimalSizeBoss(
        effectiveWidth = effectiveWidth,
        availableHeight = availableHeight,
        totalLetters = totalLetters,
        totalButtonPadding = totalButtonPadding,
        verticalSpacing = verticalSpacing,
        minSize = minButtonSize,
        maxSize = maxButtonSize
    )
}

private fun findOptimalSizeBoss(
    effectiveWidth: Dp,
    availableHeight: Dp,
    totalLetters: Int,
    totalButtonPadding: Dp,
    verticalSpacing: Dp,
    minSize: Dp,
    maxSize: Dp
): Dp {
    val realAvailableHeight = availableHeight - 16.dp
    var currentSize = maxSize
    val sizeStep = 2.dp

    while (currentSize >= minSize) {
        val totalButtonWidth = currentSize + totalButtonPadding
        val buttonsPerRow = ((effectiveWidth / totalButtonWidth) * 0.90).toInt().coerceAtLeast(1)
        val totalRows = (totalLetters + buttonsPerRow - 1) / buttonsPerRow
        val totalHeight = (currentSize * totalRows) + (verticalSpacing * (totalRows - 1))

        if (totalHeight <= realAvailableHeight) {
            return currentSize
        }

        currentSize -= sizeStep
    }

    return minSize
}