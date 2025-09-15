package com.akrubastudios.playquizgames.ui.screens.game

import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akrubastudios.playquizgames.ui.components.getButtonTextColor
import com.akrubastudios.playquizgames.ui.theme.DarkGoldAccent
import com.akrubastudios.playquizgames.ui.theme.GoldAccent
import com.akrubastudios.playquizgames.ui.theme.PureWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    navController: NavController
) {
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
            QuestionImage(imageUrl = uiState.currentQuestion!!.imageUrl)

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
            Box(modifier = Modifier.weight(1f)) {
                LetterBank(
                    hintLetters = uiState.generatedHintLetters,
                    usedIndices = uiState.usedLetterIndices,
                    difficulty = uiState.difficulty, // Borrar para habilitar letras bloqueadas en Modo Dificil
                    onLetterClick = { letter, index -> // <-- MODIFICADO
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
    // Row apila los elementos horizontalmente.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
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
    // Contenedor cuadrado estandarizado
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(182.dp) // Altura caja invisible externa de la imagen
            .padding(horizontal = 16.dp, vertical = 5.dp), // vertical 5dp, espacio arriba y abajo de la imagen
        contentAlignment = Alignment.Center
    ) {
        // Contenedor interno cuadrado con efectos
        Box(
            modifier = Modifier
                .size(180.dp) // Cuadrado fijo (continene la imagen dentro)
                // 1. FONDO con el color y la forma deseados.
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
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
                contentScale = ContentScale.Fit // Mantiene proporciones, cabe dentro del contenedor
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

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            shadow = Shadow(
                color = Color(0xFF000000).copy(alpha = 0.25f),
                offset = Offset(1f, 1f),
                blurRadius = 2f
            )
        ),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .alpha(alpha)
            .scale(scale)
    )
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
    // Animaciones
    val scale by animateFloatAsState(
        targetValue = if (showCorrectAnimation) 1.2f else 1.0f,
        animationSpec = tween(300),
        label = "scale"
    )

    val offsetX by animateFloatAsState(
        targetValue = if (showIncorrectAnimation) {
            // Alternamos entre -10 y 10 para crear el shake
            if ((System.currentTimeMillis() / 100) % 2 == 0L) -10f else 10f
        } else 0f,
        animationSpec = tween(100),
        label = "shake"
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
            .scale(scale)
            .offset(x = if (showClearAnimation) clearOffsetX.dp else offsetX.dp)
            .alpha(if (showClearAnimation) clearAlpha else 1.0f)
            .clickable { onClear() },
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally), // Espacio ENTRE palabras
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- INICIO DE LA LÓGICA DE AGRUPAMIENTO ---

        // 1. Dividimos la respuesta correcta en palabras.
        val words = correctAnswer.split(' ')

        // 2. Iteramos sobre cada palabra.
        // Determinar el tamaño global para todas las palabras
        val hasLongWord = words.any { it.length > 8 }
        val globalSlotSize = if (hasLongWord) 30.dp else 40.dp // 30.dp y 40.dp, son los tamaños de las casillas respuestas

        words.forEach { word ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { // 6.dp Espacio entre letras
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
    onLetterClick: (Char, Int) -> Unit, // Una función que se llamará cuando se toque una letra
    modifier: Modifier = Modifier
) {
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 7
    ) {
        // Creamos un botón por cada letra de nuestras pistas
        hintLetters.forEachIndexed { index, letter ->
            LetterButton(
                letter = letter,
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
}

@Composable
fun LetterButton(
    letter: Char,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp),
        enabled = enabled
    ) {
        Text(text = letter.toString().uppercase(), fontSize = 18.sp)
    }
}

@Composable
fun AnimatedTimer(
    remainingTime: Long,
    timerExplosion: Boolean,
    modifier: Modifier = Modifier
) {
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
            .size(80.dp)
            .offset(x = offsetX.dp)
            .scale(explosionScale)
            .alpha(explosionAlpha),
        contentAlignment = Alignment.Center
    ) {
        // Círculo de progreso
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(80.dp),
            color = if (timerExplosion) Color(0xFFF44336) else circleColor, // Rojo en explosión
            strokeWidth = if (timerExplosion) 12.dp else 6.dp, // Más grueso en explosión
            trackColor = circleColor.copy(alpha = 0.2f)
        )

        // Número del timer
        Text(
            text = remainingTime.toString(),
            style = MaterialTheme.typography.headlineLarge,
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = stringResource(R.string.game_top_bar_question, currentQuestionIndex + 1, totalQuestions),
            style = MaterialTheme.typography.labelMedium,
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        val layout = inflater.inflate(
                            android.R.layout.simple_list_item_1,
                            null
                        )
                        val textView = layout.findViewById<TextView>(android.R.id.text1)
                        textView.text = context.getString(R.string.toast_no_more_fun_facts)
                        textView.setTextColor(android.graphics.Color.WHITE) // Texto blanco
                        textView.setBackgroundColor(android.graphics.Color.parseColor("#AA000000")) // Negro translúcido
                        textView.setPadding(24, 16, 24, 16)

                        val toast = Toast(context)
                        toast.duration = Toast.LENGTH_SHORT
                        toast.view = layout
                        toast.show()
                    }
                },
                enabled = true,
                modifier = Modifier
                    .size(24.dp)
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
            AnimatedGemsIndicator(
                gems = currentGems,
                hasGems = currentGems > 0,
                onClick = onGemsClick
            )
        }
    }
}

@Composable
fun QuestionCircle(
    index: Int,
    currentIndex: Int,
    result: Boolean?,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
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
            .size(16.dp)
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
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Fila del XP con trofeo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🏆",
                    fontSize = 16.sp
                )
                Text(
                    text = "$score XP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Fila de la dificultad con icono
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (difficulty == "principiante") "🪶" else "🔥",
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(
                        if (difficulty == "principiante") R.string.difficulty_beginner else R.string.difficulty_hard
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun AnimatedGemsIndicator(
    gems: Int,
    hasGems: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemsAnimation")

    // Animación de pulso (escala)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (hasGems) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Animación de brillo (alpha)
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = if (hasGems) 0.6f else 0.5f,
        targetValue = if (hasGems) 1f else 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    // NUEVA ANIMACIÓN: Cambio de color cada 2 segundos
    val colorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing), // Tiempo animacion total = 4 Segundos
            repeatMode = RepeatMode.Restart
        ),
        label = "colorPhase"
    )

    // Determinar si debe ser rojo (últimos 1000ms de cada ciclo de 3000ms)
    val isRedPhase = colorPhase > 0.75f // 75% del tiempo cambio de color (3 de 4 segundos)

    Card(
        modifier = Modifier.clickable(
            enabled = hasGems,
            onClick = onClick
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .scale(pulseScale)
                .alpha(shimmerAlpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                imageVector = if (isRedPhase) GemIconDarkGold else GemIcon,
                contentDescription = "Gems",
                modifier = Modifier.size(22.dp), // Tamaño del Icono
            )
            Text(
                text = "$gems",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
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

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Text("Vista Previa de la Pantalla de Juego")
}