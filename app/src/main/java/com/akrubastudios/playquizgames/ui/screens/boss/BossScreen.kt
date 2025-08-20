package com.akrubastudios.playquizgames.ui.screens.boss

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@Composable
fun BossScreen(
    navController: NavController,
    viewModel: BossViewModel = hiltViewModel()
) {
    KeepScreenOn()
    val uiState by viewModel.uiState.collectAsState()
    val gameResult by viewModel.gameResult.collectAsState()

    // Navega a la pantalla de resultados cuando el juego termina.
    LaunchedEffect(gameResult) {
        gameResult?.let { result ->
            // --- ENVIAR SCORE REQUEST DIRECTAMENTE DESDE AQUÍ ---
            // Esto es necesario porque el BossViewModel no tiene dependencias de Firebase
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
            // ----------------------------------------------------
            val isVictory = result.starsEarned == 3
            val route = Routes.RESULT_SCREEN
                .replace("{score}", result.score.toString())
                .replace("{totalQuestions}", result.totalQuestions.toString())
                .replace("{correctAnswers}", result.correctAnswers.toString())
                .replace("{starsEarned}", result.starsEarned.toString())
                .replace("{levelId}", viewModel.levelId)
                .replace("{countryId}", viewModel.countryId)
                // Dificultad es fija para los jefes
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
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BossHeader(
                name = uiState.levelName,
                health = uiState.bossHealth,
                mistakes = uiState.playerMistakes,
                maxMistakes = uiState.maxMistakes
            )
            Spacer(Modifier.height(16.dp))

            uiState.currentQuestion?.let { question ->
                QuestionImage(imageUrl = question.imageUrl)
                Text(
                    text = question.questionText_es,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                AnswerSlots(
                    correctAnswer = question.correctAnswer,
                    userAnswer = uiState.userAnswer,
                    onClear = { viewModel.clearUserAnswer() }
                )
                LetterBank(
                    hintLetters = uiState.generatedHintLetters,
                    usedIndices = uiState.usedLetterIndices,
                    // La dificultad es "principiante" para la UI para que se bloqueen las letras,
                    // pero la lógica de recompensa es de difícil.
                    difficulty = "principiante",
                    onLetterClick = { letter, index -> viewModel.onLetterClick(letter, index) }
                )
            }
        }
    }
}

@Composable
private fun BossHeader(name: String, health: Float, mistakes: Int, maxMistakes: Int) {
    val animatedHealth by animateFloatAsState(targetValue = health, label = "BossHealth")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedHealth },
            modifier = Modifier.fillMaxWidth().height(12.dp),
            strokeCap = StrokeCap.Round,
            color = Color.Red,
            trackColor = Color.Red.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(8.dp))
        Row {
            (1..maxMistakes).forEach { i ->
                Icon(
                    imageVector = if (i <= maxMistakes - mistakes) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Vida",
                    tint = Color.Magenta
                )
            }
        }
    }
}