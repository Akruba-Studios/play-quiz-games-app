package com.akrubastudios.playquizgames.ui.screens.game

import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb // O el icono que prefieras
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.akrubastudios.playquizgames.Routes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.akrubastudios.playquizgames.core.AdManager
import com.akrubastudios.playquizgames.ui.components.KeepScreenOn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.akrubastudios.playquizgames.R
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalView
import com.akrubastudios.playquizgames.ui.components.AppAlertDialog
import com.akrubastudios.playquizgames.ui.components.DialogText
import com.akrubastudios.playquizgames.ui.components.DialogTitle
import com.akrubastudios.playquizgames.ui.components.GemIcon
import com.akrubastudios.playquizgames.ui.components.GemIconDarkGold
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akrubastudios.playquizgames.core.MusicTrack
import com.akrubastudios.playquizgames.ui.components.getButtonTextColor
import com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent
import com.akrubastudios.playquizgames.ui.theme.GoldAccent
import com.akrubastudios.playquizgames.ui.theme.LightGray
import com.akrubastudios.playquizgames.ui.theme.PureWhite
import com.akrubastudios.playquizgames.ui.theme.SkyBlue
import kotlinx.coroutines.launch
import kotlin.math.max
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.akrubastudios.playquizgames.ui.components.GemsIndicator

@OptIn(ExperimentalMaterial3Api::class) // Control 1-GM
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    navController: NavController
) {
    DisposableEffect(Unit) {
        // Esta parte se ejecuta cuando la pantalla APARECE
        viewModel.musicManager.play(MusicTrack.GAME)

        // La cláusula onDispose se ejecuta cuando la pantalla DESAPARECE
        onDispose {
            viewModel.musicManager.stop() // <-- Detenemos la música por completo
        }
    }
    KeepScreenOn()

    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AdManager.loadInterstitialAd(context)
    }

    // LaunchedEffect se ejecuta cuando 'gameResult' cambia, pero no en la composición inicial.
    // Es la forma correcta de llamar a la navegación desde un Composable.
    LaunchedEffect(gameResult) {
        gameResult?.let { result ->
            val route = Routes.RESULT_SCREEN
                .replace("{score}", result.score.toString())
                .replace("{totalQuestions}", result.totalQuestions.toString())
                .replace("{correctAnswers}", result.correctAnswers.toString())
                .replace("{starsEarned}", result.starsEarned.toString())
                .replace("{levelId}", viewModel.levelIdForNav())
                .replace("{countryId}", viewModel.countryId)
                .replace("{difficulty}", viewModel.difficultyForNav())
                .replace("{isFromBossFight}", "false")
                .replace("{victory}", "false") // Irrelevante en partidas normales
                .replace("{pcGained}", result.pcGained.toString())
                .replace("{gemsGained}", result.gemsGained.toString())
                .replace("{categoryId}", viewModel.categoryIdForNav())
                .replace("{continentId}", viewModel.continentIdForNav())
                .replace("{origin}", viewModel.originForNav())
                .replace("{previousBestStars}", result.previousBestStars.toString())

            navController.navigate(route) {
                popUpTo(Routes.GAME_SCREEN) { inclusive = true }
            }
        }
    }

    // Column apila los elementos verticalmente.
    // Modifier.fillMaxSize() hace que ocupe toda la pantalla.
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente
    ) {
        if (uiState.isLoading) {
            // Mantenemos el indicador de carga mientras los datos no estén listos.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Reemplaza el texto con esta llamada
            TopBar(
                questionNumber = uiState.questionNumber,
                totalQuestions = uiState.totalQuestions,
                score = uiState.score,
                remainingTime = uiState.remainingTime,
                difficulty = uiState.difficulty,
                questionResults = uiState.questionResults,
                timerExplosion = uiState.timerExplosion,
                isFunFactButtonEnabled = !uiState.isFunFactUsedInRound || uiState.areFunFactsUnlockedForLevel,
                onFunFactClick = { viewModel.onFunFactClicked() },
                currentGems = uiState.currentGems,
                onGemsClick = { viewModel.openHelpsSheet() }
            )
            // Usamos !! porque en este punto, sabemos que currentQuestion no es null
            QuestionImage(
                imageUrl = uiState.currentQuestion!!.imageUrl,
                // Le damos un "peso" para que ocupe una parte proporcional del espacio vertical.
                modifier = Modifier
            )

            QuestionText(
                text = uiState.questionText,
                showTransition = uiState.questionTransition,
                remainingTime = uiState.remainingTime,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp) // Vertical 4dp, espacio arriba y abajo de la pregunta
            )

            AnswerSlots(
                correctAnswer = uiState.currentCorrectAnswer,
                userAnswer = uiState.userAnswer,
                onClear = { viewModel.clearUserAnswer() },
                showCorrectAnimation = uiState.showCorrectAnimation,
                showIncorrectAnimation = uiState.showIncorrectAnimation,
                showClearAnimation = uiState.showClearAnimation,
                revealedLetterPositions = uiState.revealedLetterPositions
            )
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val availableHeight = this@BoxWithConstraints.maxHeight
                val availableWidth = this@BoxWithConstraints.maxWidth

                LetterBank(
                    hintLetters = uiState.generatedHintLetters,
                    usedIndices = uiState.usedLetterIndices,
                    difficulty = uiState.difficulty,
                    availableHeight = availableHeight,
                    availableWidth = availableWidth,
                    onLetterClick = { letter, index ->
                        viewModel.onLetterClick(letter, index)
                    }
                )
            }
        }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        if (uiState.showHelpsSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeHelpsSheet() },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                // Contenido provisional para las ayudas
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally // Centra el contenido
                ) {
                    // 1. AÑADIMOS EL TÍTULO
                    Text(
                        text = stringResource(R.string.helps_menu_title),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    if (uiState.isProcessingHelp) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val revealLetterCost = GameViewModel.HELP_REVEAL_LETTER_COST_INITIAL + (uiState.revealLetterUses * GameViewModel.HELP_REVEAL_LETTER_COST_INCREMENT)
                        HelpItem(
                            icon = Icons.Default.VpnKey,
                            title = stringResource(R.string.help_item_reveal_letter_title),
                            description = stringResource(R.string.help_item_reveal_letter_description),
                            cost = revealLetterCost,
                            currentGems = uiState.currentGems,
                            isUsed = uiState.userAnswer.length >= uiState.currentCorrectAnswer.count { it.isLetter() },
                            onClick = { viewModel.useRevealLetterHelp() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        HelpItem(
                            icon = Icons.Default.Timer,
                            title = stringResource(R.string.help_item_extra_time_title),
                            description = stringResource(R.string.help_item_extra_time_description, GameViewModel.HELP_EXTRA_TIME_SECONDS),
                            cost = GameViewModel.HELP_EXTRA_TIME_COST,
                            currentGems = uiState.currentGems,
                            isUsed = uiState.isExtraTimeUsed,
                            onClick = { viewModel.useExtraTimeHelp() }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        if (uiState.showFunFactTutorialDialog) {
            AppAlertDialog(
                onDismissRequest = { viewModel.funFactTutorialShown() },
                title = stringResource(R.string.fun_fact_tutorial_title),
                text = stringResource(R.string.fun_fact_tutorial_message),
                confirmButtonText = stringResource(R.string.dialog_button_ok)
            )
        }

        // Añadimos el diálogo aquí, dentro del Column pero fuera del 'else'.
        if (uiState.showFunFactDialog) {
            AppAlertDialog(
                onDismissRequest = { /* No hacer nada para forzar el clic en la X */ },
                title = { DialogTitle(text = stringResource(R.string.fun_fact_title)) },
                text = { DialogText(text = uiState.currentFunFact) },
                confirmButton = {
                    val buttonIconColor = getButtonTextColor()
                    IconButton(onClick = { viewModel.onFunFactDialogDismissed() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            // Aplicamos el color al 'tint' del icono
                            tint = buttonIconColor
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TopBar(
    questionNumber: Int,
    totalQuestions: Int,
    score: Int,
    remainingTime: Long,
    difficulty: String,
    questionResults: List<Boolean?>,
    timerExplosion: Boolean = false,
    isFunFactButtonEnabled: Boolean,
    onFunFactClick: () -> Unit,
    currentGems: Int,
    onGemsClick: () -> Unit,
    modifier: Modifier = Modifier // Es una buena práctica aceptar un Modifier
) {

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // Log para detectar el screenWidth actual
    Log.d("TopBarZoom", "Detected screenWidth: $screenWidth")

    val topBarPadding = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 8.dp
            screenWidth < 370.dp -> 12.dp
            else -> 16.dp
        }
    }

    Log.d("TopBarPadding", "screenWidth: $screenWidth → padding: $topBarPadding")
    // Row apila los elementos horizontalmente.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(topBarPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lado izquierdo - Círculos de progreso
        QuestionProgressCircles(
            currentQuestionIndex = questionNumber - 1, // Convertir a 0-based
            questionResults = questionResults,
            totalQuestions = totalQuestions,
            isFunFactButtonEnabled = isFunFactButtonEnabled,
            onFunFactClick = onFunFactClick,
            currentGems = currentGems,
            onGemsClick = onGemsClick
        )

        // Centro - Timer
        AnimatedTimer(
            remainingTime = remainingTime,
            timerExplosion = timerExplosion
        )

        // Lado derecho - Card de Score y dificultad
        ScoreAndDifficultyCard(
            score = score,
            difficulty = difficulty
        )
    }
}

@Composable
fun QuestionImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val density = LocalDensity.current

    val imageContainerHeight = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 100.dp  // Zona crítica
            screenWidth < 370.dp -> 120.dp  // Zona transición
            else -> 150.dp                  // Zona normal
        }
    }
    Log.d("ImageContainer", "screenWidth: $screenWidth → containerHeight: $imageContainerHeight")

    // Contenedor cuadrado estandarizado
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 5.dp) // vertical 5dp, espacio arriba y abajo de la imagen
            .size(imageContainerHeight)
            .onSizeChanged { size ->
                val widthDp = with(density) { size.width.toDp() }
                val heightDp = with(density) { size.height.toDp() }
                Log.d("ImageContainer", "Final container size - width: $widthDp, height: $heightDp")
            },
        contentAlignment = Alignment.Center
    ) {
        // Contenedor interno cuadrado con efectos
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 1. FONDO con el color y la forma deseados.
                .background(
                    // Lógica condicional para el color:
                    color = if (isSystemInDarkTheme()) {
                        // Si el tema es oscuro, forzamos el fondo gris claro.
                        LightGray
                    } else {
                        // Si el tema es claro, usamos el color por defecto del tema.
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                // 2. BORDE que usa el color 'outline' de nuestro tema.
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                // 3. PADDING INTERNO para que la imagen no se pegue al borde.
                .padding(4.dp), // Padding interno para que la imagen no toque los bordes
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.cd_question_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun QuestionText(
    text: String,
    showTransition: Boolean,
    remainingTime: Long,
    modifier: Modifier = Modifier
) {
    // Fade cross transition (150ms)
    val alpha by animateFloatAsState(
        targetValue = if (showTransition) 0.0f else 1.0f,
        animationSpec = tween(150),
        label = "question_fade"
    )

    // Pulse sutil cuando quedan más de 10 segundos
    val scale by animateFloatAsState(
        targetValue = if (remainingTime > 10) {
            if ((System.currentTimeMillis() / 1500) % 2 == 0L) 1.0f else 1.02f
        } else 1.0f,
        animationSpec = tween(1500),
        label = "question_pulse"
    )

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val maxLines = 4 // cantidad de filas maximas que se pueden generar en las preguntas, despues se achican
    val initialFontSize = MaterialTheme.typography.titleLarge.fontSize.value
    val baseTextStyle = MaterialTheme.typography.titleLarge

    BoxWithConstraints(
        modifier = modifier
            .alpha(alpha)
            .scale(scale)
    ) {
        val maxWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx().toInt() }

        // Calculamos el fontSize óptimo
        val optimalFontSize = remember(text, maxWidthPx) {
            var currentFontSize = initialFontSize
            val minFontSize = 6f

            while (currentFontSize >= minFontSize) {
                val textStyle = baseTextStyle.copy(
                    fontSize = currentFontSize.sp,
                    fontWeight = FontWeight.Bold
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
                fontSize = optimalFontSize.sp,
                shadow = Shadow(
                    color = Color(0xFF000000).copy(alpha = 0.25f),
                    offset = Offset(1f, 1f),
                    blurRadius = 2f
                )
            ),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            maxLines = maxLines
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnswerSlots(
    correctAnswer: String,
    userAnswer: String,
    onClear: () -> Unit,
    showCorrectAnimation: Boolean = false,
    showIncorrectAnimation: Boolean = false,
    showClearAnimation: Boolean = false,
    revealedLetterPositions: Set<Int> = emptySet(),
    modifier: Modifier = Modifier
) {
    // Animación de Shake para INCORRECTO (se mantiene igual)
    val shakeOffsetX by animateFloatAsState(
        targetValue = if (showIncorrectAnimation) {
            if ((System.currentTimeMillis() / 100) % 2 == 0L) -10f else 10f
        } else 0f,
        animationSpec = tween(100),
        label = "shake_incorrect"
    )

    // NUEVA Animación de Salto para CORRECTO
    val jumpOffsetY by animateFloatAsState(
        targetValue = if (showCorrectAnimation) -10f else 0f, // Salta 10dp hacia arriba
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "jump_correct"
    )

    // Animaciones de clear (shake + fade)
    val clearOffsetX by animateFloatAsState(
        targetValue = if (showClearAnimation) {
            if ((System.currentTimeMillis() / 80) % 2 == 0L) -8f else 8f
        } else 0f,
        animationSpec = tween(80),
        label = "clear_shake"
    )

    val clearAlpha by animateFloatAsState(
        targetValue = if (showClearAnimation) 0.3f else 1.0f,
        animationSpec = tween(200),
        label = "clear_fade"
    )

    val userAnswerLetters = userAnswer
    var letterIndex = 0

    // El FlowRow es el contenedor principal que permite que las palabras salten de línea.
    FlowRow(
        modifier = modifier
            .padding(top = 12.dp, bottom = 20.dp) //top 12 espacio entre el texto pregunta y el answerslot; 20 dp, el espacio del answetslots hacia abajo
            .offset(
                x = if (showClearAnimation) clearOffsetX.dp else shakeOffsetX.dp,
                y = jumpOffsetY.dp // <-- APLICAMOS LA NUEVA ANIMACIÓN DE SALTO
            )
            .alpha(if (showClearAnimation) clearAlpha else 1.0f)
            .clickable { onClear() },
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally), // Espacio ENTRE palabras
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Dividimos la respuesta correcta en palabras.
        val words = correctAnswer.split(' ')
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp.dp
        // GameScreen AnswerSlots no tiene padding horizontal, así que usamos el ancho completo.
        val availableWidth = screenWidthDp
        val longestWord = words.maxByOrNull { it.length } ?: ""
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val (minSlotSize, maxSlotSize) = remember(screenWidth) {
            when {
                screenWidth < 340.dp -> Pair(26.dp, 35.dp)  // Más pequeño en zoom
                screenWidth < 370.dp -> Pair(28.dp, 38.dp)  // Intermedio
                else -> Pair(28.dp, 40.dp)                  // Actual
            }
        }
        val totalLetters = longestWord.length

        // El mismo cálculo que hicimos para BossScreen
        val globalSlotSize = if (totalLetters > 0) {
            val totalLetterGaps = (totalLetters - 1).coerceAtLeast(0)
            val letterSpacing = 6.dp
            val totalSpacing = letterSpacing * totalLetterGaps
            ((availableWidth - totalSpacing) / totalLetters).coerceIn(minSlotSize, maxSlotSize)
        } else {
            maxSlotSize // Un valor por defecto si no hay letras
        }

        words.forEach { word ->
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp) // 6.dp Espacio entre letras
            ) {
                word.forEach { _ ->
                    val charToShow = userAnswerLetters.getOrNull(letterIndex) ?: ' '
                    val isRevealed = revealedLetterPositions.contains(letterIndex)
                    AnswerSlot(
                        char = charToShow,
                        size = globalSlotSize,
                        showCorrectAnimation = showCorrectAnimation,
                        showIncorrectAnimation = showIncorrectAnimation,
                        isRevealed = isRevealed
                    )
                    letterIndex++
                }
            }
        }
    }
}

@Composable
fun AnswerSlot(
    char: Char,
    size: Dp = 40.dp,
    showCorrectAnimation: Boolean = false,
    showIncorrectAnimation: Boolean = false,
    isRevealed: Boolean = false
) {
    val borderColor = when {
        showCorrectAnimation -> Color(0xFF4CAF50) // Verde
        showIncorrectAnimation -> Color(0xFFF44336) // Rojo
        isRevealed -> GoldAccent
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = Modifier
            .size(size)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString().uppercase(),
            fontSize = if (size < 35.dp) 16.sp else 20.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class) // Necesario para FlowRow
@Composable
fun LetterBank(
    hintLetters: String,
    usedIndices: Set<Int>,
    difficulty: String, // Borrar para habilitar letras bloqueadas en Modo Dificil
    availableHeight: Dp,
    availableWidth: Dp,
    onLetterClick: (Char, Int) -> Unit, // Una función que se llamará cuando se toque una letra
    modifier: Modifier = Modifier
) {
    val buttonSize = remember(availableHeight, availableWidth, hintLetters.length) {
        calculateOptimalButtonSize(
            availableHeight = availableHeight,
            availableWidth = availableWidth,
            totalLetters = hintLetters.length
        ).also { size ->
            Log.d("ButtonSizeCalculation", "availableHeight: $availableHeight, availableWidth: $availableWidth")
            Log.d("ButtonSizeCalculation", "letters: ${hintLetters.length}, chosen size: $size")
            Log.d("ButtonCalc", "Container internal padding: 16dp")
            Log.d("ButtonCalc", "Real available height after padding: ${availableHeight - 16.dp}")
        }
    }

    // FlowRow es como una Row, pero si no caben los elementos,
    // los pasa a la siguiente línea automáticamente. Ideal para nuestro banco de letras.
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // <-- Movemos el padding exterior aquí
            .background( // 1. AÑADIMOS UN FONDO SUTIL
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .border( // 2. AÑADIMOS EL BORDE DEL TEMA
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp) // 3. AÑADIMOS UN PADDING INTERNO
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(2.dp), // Espacio vertical entre las filas del letterbank
        maxItemsInEachRow = 7
    ) {
        // Creamos un botón por cada letra de nuestras pistas
        hintLetters.forEachIndexed { index, letter ->
            LetterButton(
                letter = letter,
                buttonSize = buttonSize,
                // Si el modo es principiante Y el índice está en la lista de usados,
                // el botón se deshabilita.
                enabled = if (difficulty == "principiante") { // Reemplazar por esto: "enabled = usedIndices.contains(index).not()," para habilitar letras bloqueads en modo hard
                    usedIndices.contains(index).not()
                } else {
                    true
                },
                onClick = { onLetterClick(letter, index) }
            )
        }
    }
    Log.d("FlowRowDebug", "Total buttons created: ${hintLetters.length}")
    Log.d("FlowRowDebug", "Expected: ${hintLetters.length / 5} rows with mostly 5 buttons per row")
}

@Composable
fun LetterButton(
    letter: Char,
    buttonSize: Dp,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val fontSize = when {
        buttonSize >= 65.dp -> 24.sp
        buttonSize >= 55.dp -> 20.sp
        buttonSize >= 45.dp -> 18.sp
        else -> 16.sp
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(buttonSize),
        enabled = enabled,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = letter.toString().uppercase(),
            fontSize = fontSize,
            textAlign = TextAlign.Center // <- AGREGAR ESTA LÍNEA TAMBIÉN
        )
    }
}

@Composable
fun AnimatedTimer(
    remainingTime: Long,
    timerExplosion: Boolean,
    modifier: Modifier = Modifier
) {
    // Detectar el ancho de pantalla disponible
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Ajustar tamaño del timer según zoom de pantalla
    val timerSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 65.dp  // Zona crítica
            screenWidth < 370.dp -> 72.dp  // Zona transición
            else -> 80.dp                  // Zona normal
        }
    }

    Log.d("TimerSize", "screenWidth: $screenWidth → timerSize: $timerSize")

    // Ajustar stroke width proporcionalmente
    val strokeWidth = remember(timerSize) {
        when {
            timerSize <= 65.dp -> 5.dp
            timerSize <= 72.dp -> 5.5.dp
            else -> 6.dp
        }
    }

    val explosionStrokeWidth = remember(timerSize) {
        when {
            timerSize <= 65.dp -> 10.dp
            timerSize <= 72.dp -> 11.dp
            else -> 12.dp
        }
    }

    // Ajustar font size del número
    val fontSize = when {
        timerSize <= 65.dp -> MaterialTheme.typography.headlineMedium.fontSize
        timerSize <= 72.dp -> MaterialTheme.typography.headlineLarge.fontSize
        else -> MaterialTheme.typography.headlineLarge.fontSize
    }

    // Calculamos el progreso (0.0 = tiempo completo, 1.0 = sin tiempo)
    val progress = 1.0f - (remainingTime / 15.0f)

    // Color del círculo según el tiempo
    val circleColor = when {
        remainingTime > 10 -> Color(0xFF4CAF50) // Verde
        remainingTime > 5 -> Color(0xFFFF9800) // Naranja
        else -> Color(0xFFF44336) // Rojo
    }

    // Animación de shake
    val shakeIntensity = when {
        remainingTime <= 1 -> 12f // MÁS INTENSO
        remainingTime <= 3 -> 8f
        remainingTime <= 5 -> 4f
        else -> 0f
    }

    val offsetX by animateFloatAsState(
        targetValue = if (shakeIntensity > 0 && !timerExplosion) {
            if ((System.currentTimeMillis() / 80) % 2 == 0L) -shakeIntensity else shakeIntensity
        } else 0f,
        animationSpec = tween(80),
        label = "shake"
    )

    // Animación de explosión MÁS DRAMÁTICA
    val explosionScale by animateFloatAsState(
        targetValue = if (timerExplosion) 4.0f else 1.0f, // Más grande
        animationSpec = tween(800), // Más lenta
        label = "explosion_scale"
    )

    val explosionAlpha by animateFloatAsState(
        targetValue = if (timerExplosion) 0.0f else 1.0f,
        animationSpec = tween(800), // Más lenta
        label = "explosion_alpha"
    )

    Box(
        modifier = modifier
            .size(timerSize)
            .offset(x = offsetX.dp)
            .scale(explosionScale)
            .alpha(explosionAlpha),
        contentAlignment = Alignment.Center
    ) {
        // Círculo de progreso
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(timerSize),
            color = if (timerExplosion) Color(0xFFF44336) else circleColor, // Rojo en explosión
            strokeWidth = if (timerExplosion) explosionStrokeWidth else strokeWidth, // Más grueso en explosión
            trackColor = circleColor.copy(alpha = 0.2f)
        )

        // Número del timer
        Text(
            text = remainingTime.toString(),
            fontSize = fontSize,
            color = if (timerExplosion) Color(0xFFF44336) else circleColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuestionProgressCircles(
    currentQuestionIndex: Int, // 0-based
    questionResults: List<Boolean?>,
    totalQuestions: Int = 10,
    isFunFactButtonEnabled: Boolean,
    onFunFactClick: () -> Unit,
    currentGems: Int,
    onGemsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val circlesWidth = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 90.dp   // Zona crítica: más compacto
            screenWidth < 370.dp -> 110.dp  // Zona transición
            else -> 130.dp                  // Zona normal: ancho actual funcional
        }
    }
    Log.d("CirclesWidth", "screenWidth: $screenWidth → circlesWidth: $circlesWidth")

    val circleSpacing = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 4.dp     // Zona crítica
            screenWidth < 370.dp -> 5.dp     // Zona transición
            else -> 6.dp                     // Zona normal
        }
    }
    Log.d("CircleSpacing", "screenWidth: $screenWidth → circleSpacing: $circleSpacing")

    val buttonSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 20.dp    // Zona crítica
            screenWidth < 370.dp -> 22.dp    // Zona transición
            else -> 24.dp                    // Zona normal
        }
    }
    Log.d("ButtonSize", "screenWidth: $screenWidth → buttonSize: $buttonSize")

    val bottomRowSpacing = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 6.dp     // Zona crítica
            screenWidth < 370.dp -> 7.dp     // Zona transición
            else -> 8.dp                     // Zona normal
        }
    }
    Log.d("BottomRowSpacing", "screenWidth: $screenWidth → spacing: $bottomRowSpacing")

    Column( //modifier = modifier,
        modifier = modifier.width(circlesWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        AdaptiveQuestionText(
            questionIndex = currentQuestionIndex + 1,
            totalQuestions = totalQuestions,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // Si son 5 preguntas: una sola fila
        // Si son 10 preguntas: dos filas como antes
        if (totalQuestions <= 5) {
            // Una sola fila para 5 preguntas
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalQuestions) { index ->
                    QuestionCircle(
                        index = index,
                        currentIndex = currentQuestionIndex,
                        result = questionResults.getOrNull(index),
                        isActive = index == currentQuestionIndex
                    )
                }
            }
        } else {
            // Primera fila (círculos 0-4)
            Row(
                horizontalArrangement = Arrangement.spacedBy(circleSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    QuestionCircle(
                        index = index,
                        currentIndex = currentQuestionIndex,
                        result = questionResults.getOrNull(index),
                        isActive = index == currentQuestionIndex
                    )
                }
            }

            // Segunda fila (círculos 5-9)
            Row(
                horizontalArrangement = Arrangement.spacedBy(circleSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalQuestions - 5) { index ->
                    val actualIndex = index + 5
                    QuestionCircle(
                        index = actualIndex,
                        currentIndex = currentQuestionIndex,
                        result = questionResults.getOrNull(actualIndex),
                        isActive = actualIndex == currentQuestionIndex
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(bottomRowSpacing)
        ) {
            // 1. Animación de escala para el pulso.
            // Creamos una transición infinita para que el efecto sea constante.
            val infiniteTransition = rememberInfiniteTransition(label = "FunFactPulse")
            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.2f, // La escala máxima del pulso
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing), // Duración de un ciclo de pulso
                    repeatMode = RepeatMode.Reverse // Hace que vuelva a su tamaño original
                ),
                label = "FunFactScale"
            )

            // Determinamos la escala final: si está habilitado, usa la escala animada; si no, usa 1.0f.
            val finalScale = if (isFunFactButtonEnabled) animatedScale else 1.0f

            Spacer(modifier = Modifier.height(4.dp))

            val context = LocalContext.current

            IconButton(
                onClick = {
                    if (isFunFactButtonEnabled) {
                        onFunFactClick()
                    } else {
                        // Toast personalizado con fondo negro translúcido y texto blanco
                        val inflater = LayoutInflater.from(context)
                        val layout = inflater.inflate(R.layout.custom_toast_layout, null)

                        val textView = layout.findViewById<TextView>(R.id.toast_text)
                        textView.text = context.getString(R.string.toast_no_more_fun_facts)

                        Toast(context).apply {
                            duration = Toast.LENGTH_SHORT
                            view = layout
                            show()
                        }
                    }
                },
                enabled = true,
                modifier = Modifier
                    .size(buttonSize)
                    .scale(finalScale)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = stringResource(R.string.cd_fun_fact_button),
                    tint = if (isFunFactButtonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )
            }
            GemsIndicator(
                gems = currentGems,
                onClick = onGemsClick
            )
        }
    }
}

@Composable
private fun AdaptiveQuestionText(
    questionIndex: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val text = stringResource(R.string.game_top_bar_question, questionIndex, totalQuestions)

    val fontSize = remember(totalQuestions, screenWidth) {
        // Tamaño base más grande y mejor escalado
        val baseSize = when {
            totalQuestions >= 10 -> 12f
            totalQuestions >= 9 -> 14f
            else -> 16f
        }

        // Factor de escala basado en screenWidth
        val scaleFactor = when {
            screenWidth < 340.dp -> 0.70f    // Zona crítica
            screenWidth < 370.dp -> 0.85f    // Zona transición
            else -> 1.0f                     // Zona normal
        }

        // Aplicar el factor y permitir un rango más amplio
        val finalSize = (baseSize * scaleFactor).coerceIn(10f, 22f)

        Log.d("AdaptiveText", "screenWidth: $screenWidth, totalQuestions: $totalQuestions, baseSize: $baseSize, scaleFactor: $scaleFactor, finalSize: $finalSize")

        finalSize
    }

    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
fun QuestionCircle(
    index: Int,
    currentIndex: Int,
    result: Boolean?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val circleSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 12.dp    // Zona crítica
            screenWidth < 370.dp -> 14.dp    // Zona transición
            else -> 16.dp                    // Zona normal
        }
    }
    Log.d("CircleSize", "screenWidth: $screenWidth → circleSize: $circleSize")

    // Determinar color según el estado
    val circleColor = when {
        result == true -> Color(0xFF4CAF50) // Verde para correcta
        result == false -> Color(0xFFF44336) // Rojo para incorrecta
        isActive -> Color(0xFFFFD700) // Dorado para pregunta actual
        else -> Color(0xFFE0E0E0) // Gris para no respondida
    }

    // Animación de escala cuando se responde
    val scale by animateFloatAsState(
        targetValue = if (result != null && index < currentIndex) 1.1f else 1.0f,
        animationSpec = tween(300),
        label = "circle_scale"
    )

    // Animación de pulso para la pregunta actual
    val alpha by animateFloatAsState(
        targetValue = if (isActive) 0.7f else 1.0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "circle_pulse"
    )

    Box(
        modifier = modifier
            .size(circleSize)
            .scale(scale)
            .alpha(if (isActive) alpha else 1.0f)
            .background(
                color = circleColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Contenido vacío, solo necesitamos el círculo
    }
}

@Composable
fun ScoreAndDifficultyCard(
    score: Int,
    difficulty: String,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val cardFontSize = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 10.sp    // Zona crítica
            screenWidth < 370.dp -> 12.sp    // Zona transición
            else -> 14.sp                    // Zona normal
        }
    }

    val cardPadding = remember(screenWidth) {
        when {
            screenWidth < 340.dp -> 8.dp     // Zona crítica
            screenWidth < 370.dp -> 10.dp    // Zona transición
            else -> 12.dp                    // Zona normal
        }
    }
    Log.d("ScoreCard", "screenWidth: $screenWidth → fontSize: $cardFontSize, padding: $cardPadding")

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "🏆", fontSize = cardFontSize)
                Text(
                    text = "$score XP",
                    fontSize = cardFontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (difficulty == "principiante") "🪶" else "🔥",
                    fontSize = cardFontSize,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(
                        if (difficulty == "principiante") R.string.difficulty_beginner else R.string.difficulty_hard
                    ),
                    fontSize = cardFontSize,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HelpItem( // COPIADO Y ADAPTADO
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary // Usamos el color del tema
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold) // El color se hereda
                Text(text = description, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = onClick,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGoldAccent, // Mantenemos el dorado
                    contentColor = PureWhite,
                    disabledContainerColor = DarkGoldAccent.copy(alpha = 0.5f),
                    disabledContentColor = PureWhite.copy(alpha = 0.5f)
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
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// Nueva función de cálculo bidimensional
private fun calculateOptimalButtonSize(
    availableHeight: Dp,
    availableWidth: Dp,
    totalLetters: Int
): Dp {
    // Constantes del layout
    val buttonHorizontalPadding = 4.dp
    val totalButtonPadding = buttonHorizontalPadding * 2 // 8dp por botón
    val containerHorizontalPadding = 8.dp * 2 // 16dp total del container
    val verticalSpacing = 2.dp

    // Ancho efectivo disponible para botones
    val effectiveWidth = availableWidth - containerHorizontalPadding

    // Definir rangos de tamaño
    val minButtonSize = 38.dp
    val maxButtonSize = 68.dp

    Log.d("ButtonCalc", "effectiveWidth: $effectiveWidth, totalLetters: $totalLetters")

    // Encontrar el tamaño óptimo probando diferentes configuraciones
    val optimalSize = findOptimalSize(
        effectiveWidth = effectiveWidth,
        availableHeight = availableHeight,
        totalLetters = totalLetters,
        totalButtonPadding = totalButtonPadding,
        verticalSpacing = verticalSpacing,
        minSize = minButtonSize,
        maxSize = maxButtonSize
    )

    Log.d("ButtonCalc", "Final optimal size: $optimalSize")
    return optimalSize
}

private fun findOptimalSize(
    effectiveWidth: Dp,
    availableHeight: Dp,
    totalLetters: Int,
    totalButtonPadding: Dp,
    verticalSpacing: Dp,
    minSize: Dp,
    maxSize: Dp
): Dp {
    val realAvailableHeight = availableHeight - 16.dp
    // Probar tamaños desde el máximo hacia el mínimo
    var currentSize = maxSize
    val sizeStep = 2.dp

    while (currentSize >= minSize) {
        val totalButtonWidth = currentSize + totalButtonPadding
        val buttonsPerRow = ((effectiveWidth / totalButtonWidth) * 0.90).toInt().coerceAtLeast(1) // 0.90 Semiconservador
        val totalRows = (totalLetters + buttonsPerRow - 1) / buttonsPerRow // Ceiling division
        val totalHeight = (currentSize * totalRows) + (verticalSpacing * (totalRows - 1))

        Log.d("ButtonCalc", "Testing size: $currentSize")
        Log.d("ButtonCalc", "  - buttonsPerRow: $buttonsPerRow")
        Log.d("ButtonCalc", "  - buttonsPerRow: $buttonsPerRow (with 0.90 factor)")
        Log.d("ButtonCalc", "  - totalHeight: $totalHeight vs realAvailable: $realAvailableHeight")

        if (totalHeight <= realAvailableHeight) {
            Log.d("ButtonCalc", "  - ✓ Fits! Using size: $currentSize")
            return currentSize
        }

        currentSize -= sizeStep
    }

    Log.d("ButtonCalc", "  - Using minimum size: $minSize")
    return minSize
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Text("Vista Previa de la Pantalla de Juego")
}