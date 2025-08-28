package com.akrubastudios.playquizgames.ui.screens.game

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.akrubastudios.playquizgames.R

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
                difficulty = uiState.difficulty
            )
            // Usamos !! porque en este punto, sabemos que currentQuestion no es null
            QuestionImage(imageUrl = uiState.currentQuestion!!.imageUrl)

            Text(
                text = uiState.questionText,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )

            AnswerSlots(
                correctAnswer = uiState.currentCorrectAnswer,
                userAnswer = uiState.userAnswer,
                onClear = { viewModel.clearUserAnswer() },
                showCorrectAnimation = uiState.showCorrectAnimation,
                showIncorrectAnimation = uiState.showIncorrectAnimation
            )
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
}

@Composable
fun TopBar(
    questionNumber: Int,
    totalQuestions: Int,
    score: Int,
    remainingTime: Long,
    difficulty: String,
    modifier: Modifier = Modifier // Es una buena práctica aceptar un Modifier
) {
    // Row apila los elementos horizontalmente.
    Row(
        modifier = modifier
            .fillMaxWidth() // Ocupa todo el ancho
            .padding(16.dp), // Añade un margen
        horizontalArrangement = Arrangement.SpaceBetween, // Espacia los elementos
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.game_top_bar_question, questionNumber, totalQuestions))
        Text(
            text = remainingTime.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = if (remainingTime <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(text = stringResource(R.string.game_top_bar_score, score))
            // Mostramos la dificultad actual con un estilo más pequeño.
            Text(
                text = difficulty.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun QuestionImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = stringResource(R.string.cd_question_image),
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // Damos una altura fija a la imagen
            .padding(16.dp),
        contentScale = ContentScale.Fit // Asegura que la imagen se ajuste bien
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

    val userAnswerLetters = userAnswer
    var letterIndex = 0

    // El FlowRow es el contenedor principal que permite que las palabras salten de línea.
    FlowRow(
        modifier = modifier
            .padding(vertical = 24.dp)
            .scale(scale)
            .offset(x = offsetX.dp)
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
                    AnswerSlot(
                        char = charToShow,
                        size = globalSlotSize,
                        showCorrectAnimation = showCorrectAnimation,
                        showIncorrectAnimation = showIncorrectAnimation
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
    showIncorrectAnimation: Boolean = false
) {
    val borderColor = when {
        showCorrectAnimation -> Color(0xFF4CAF50) // Verde
        showIncorrectAnimation -> Color(0xFFF44336) // Rojo
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
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 7 // Intentará poner 7 letras por fila
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

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Text("Vista Previa de la Pantalla de Juego")
}