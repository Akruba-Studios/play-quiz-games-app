package com.akrubastudios.playquizgames.ui.screens.game

import android.util.Log
import androidx.lifecycle.ViewModel
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: QuizRepository
) : ViewModel() {

    // El bloque init se ejecuta tan pronto como se crea el ViewModel.
    // Lo usamos para probar que todo está conectado.
    init {
        loadLevel()
    }

    private fun loadLevel() {
        // Le pedimos al repositorio que nos dé nuestro nivel de prueba.
        val level = repository.getLevel("logos_level_1")

        // Usamos Log.d para imprimir un mensaje de depuración.
        // Esto nos permitirá verificar que la carga de datos funciona.
        if (level != null) {
            Log.d("GameViewModel", "Nivel cargado exitosamente: ${level.levelName}")
            Log.d("GameViewModel", "Número de preguntas: ${level.questions.size}")
        } else {
            Log.d("GameViewModel", "Error al cargar el nivel.")
        }
    }
}