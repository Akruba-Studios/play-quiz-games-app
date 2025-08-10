package com.akrubastudios.playquizgames.ui.screens.game

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

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    // LaunchedEffect se ejecuta cuando 'gameResult' cambia, pero no en la composición inicial.
    // Es la forma correcta de llamar a la navegación desde un Composable.
    LaunchedEffect(gameResult) {
        gameResult?.let { result ->
            val route = Routes.RESULT_SCREEN
                .replace("{score}", result.score.toString())
                .replace("{totalQuestions}", result.totalQuestions.toString())
                .replace("{correctAnswers}", result.correctAnswers.toString())

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
                score = uiState.score
            )
            // Usamos !! porque en este punto, sabemos que currentQuestion no es null
            QuestionImage(imageUrl = uiState.currentQuestion!!.imageUrl)

            AnswerSlots(
                correctAnswer = uiState.currentQuestion!!.correctAnswer,
                userAnswer = uiState.userAnswer
            )
            LetterBank(
                hintLetters = uiState.currentQuestion!!.hintLetters,
                onLetterClick = { letter -> viewModel.onLetterClick(letter) }
            )
        }
    }
}

@Composable
fun TopBar(
    questionNumber: Int,
    totalQuestions: Int,
    score: Int,
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
        Text(text = "Pregunta: $questionNumber / $totalQuestions")
        Text(text = "Puntaje: $score")
    }
}

@Composable
fun QuestionImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Imagen de la pregunta",
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // Damos una altura fija a la imagen
            .padding(16.dp),
        contentScale = ContentScale.Fit // Asegura que la imagen se ajuste bien
    )
}

@Composable
fun AnswerSlots(
    correctAnswer: String,
    userAnswer: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre casillas
    ) {
        // Creamos una casilla por cada letra de la respuesta correcta
        correctAnswer.forEachIndexed { index, _ ->
            val charToShow = userAnswer.getOrNull(index) ?: ' '
            AnswerSlot(char = charToShow)
        }
    }
}

@Composable
fun AnswerSlot(char: Char) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString().uppercase(),
            fontSize = 20.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class) // Necesario para FlowRow
@Composable
fun LetterBank(
    hintLetters: String,
    onLetterClick: (Char) -> Unit, // Una función que se llamará cuando se toque una letra
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
        hintLetters.forEach { letter ->
            LetterButton(letter = letter, onClick = { onLetterClick(letter) })
        }
    }
}

@Composable
fun LetterButton(
    letter: Char,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = letter.toString().uppercase(), fontSize = 18.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Text("Vista Previa de la Pantalla de Juego")
}