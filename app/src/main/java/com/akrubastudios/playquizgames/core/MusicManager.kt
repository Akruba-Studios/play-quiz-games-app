package com.akrubastudios.playquizgames.core

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class MusicTrack(val resourceId: Int) { // Control 1-MM
    MAP(R.raw.music_map),
    GAME(R.raw.music_game),
    BOSS(R.raw.music_boss),
    NONE(-1)
}

@Singleton
class MusicManager @Inject constructor(
    @ApplicationContext private val context: Context, // <-- Inyectamos el Context
    private val settingsRepository: SettingsRepository, // <-- Inyectamos el Repositorio
    private val lifecycleOwner: LifecycleOwner
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: MusicTrack = MusicTrack.NONE
    private var isMusicEnabled = true // <-- Guardaremos aquí la preferencia del usuario
    private var currentVolume = 1.0f
    private val scope = CoroutineScope(Dispatchers.Main)
    private var fadeJob: Job? = null

    init {
        // Al iniciar, leemos la preferencia del usuario desde DataStore
        scope.launch {
            isMusicEnabled = settingsRepository.musicPreferenceFlow.first()
            currentVolume = settingsRepository.musicVolumeFlow.first()
        }
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            // Se llama cuando la app pasa a primer plano
            override fun onStart(owner: LifecycleOwner) {
                if (isMusicEnabled) resume()
            }
            // Se llama cuando la app pasa a segundo plano
            override fun onStop(owner: LifecycleOwner) {
                pause()
            }
        })
    }

    /**
     * Reproduce una pista de música.
     * Si ya está sonando la misma, no hace nada.
     * Si hay otra sonando, la detiene y reproduce la nueva.
     */
    fun play(track: MusicTrack) {
        if (!isMusicEnabled || currentTrack == track) return

        fadeJob?.cancel() // <-- Cancelamos cualquier fundido anterior
        fadeJob = scope.launch {
            if (mediaPlayer?.isPlaying == true) {
                fadeOut() // <-- Primero, bajamos el volumen de la pista actual
            }
            // Si la nueva pista no es NINGUNA, simplemente nos detenemos.
            if (track == MusicTrack.NONE) {
                currentTrack = MusicTrack.NONE
            } else {
                startNewTrack(track) // <-- Luego, iniciamos la nueva pista con fade in
            }
        }
    }

    private fun startNewTrack(track: MusicTrack) {
        mediaPlayer = MediaPlayer.create(context, track.resourceId).apply {
            isLooping = true
            setVolume(0f, 0f) // <-- Empezamos con volumen 0
            start()
        }
        currentTrack = track
        fadeIn() // <-- Subimos el volumen gradualmente
    }

    fun stop() {
        // Ahora, stop también usará el fundido para ser suave
        if (currentTrack == MusicTrack.NONE) return
        fadeJob?.cancel()
        fadeJob = scope.launch {
            fadeOut()
            currentTrack = MusicTrack.NONE
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun resume() {
        if (mediaPlayer?.isPlaying == false && currentTrack != MusicTrack.NONE) {
            mediaPlayer?.start()
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        scope.launch {
            settingsRepository.saveMusicPreference(enabled)
        }
        if (enabled) {
            resume()
        } else {
            pause()
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(currentVolume, currentVolume)
        scope.launch {
            settingsRepository.saveMusicVolume(currentVolume)
        }
    }

    /**
     * Reduce suavemente el volumen de la música a un nivel bajo (ej. 20%).
     * Se usa cuando un sonido ambiental, como la lluvia, necesita ser el protagonista.
     */
    fun duckVolume() {
        fadeJob?.cancel() // Cancela cualquier animación de volumen en curso
        fadeJob = scope.launch {
            if (mediaPlayer == null) return@launch
            // El volumen objetivo es el 20% del volumen actual del usuario
            val targetVolume = currentVolume * 0.2f

            // Animamos desde el volumen actual hasta el volumen objetivo
            for (i in 100 downTo 20 step 5) {
                val volume = (i / 100f) * currentVolume
                try {
                    mediaPlayer?.setVolume(volume, volume)
                } catch (e: IllegalStateException) {
                    // Ignorar si el mediaplayer se libera a mitad de la animación
                }
                delay(25) // Animación rápida
            }
            // Aseguramos que el volumen final sea exacto
            mediaPlayer?.setVolume(targetVolume, targetVolume)
        }
    }

    /**
     * Restaura suavemente el volumen de la música a su nivel original.
     * Se llama cuando el sonido ambiental termina.
     */
    fun restoreVolume() {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            if (mediaPlayer == null) return@launch
            // Animamos desde el volumen bajo actual hasta el volumen completo del usuario
            for (i in 20..100 step 5) {
                val volume = (i / 100f) * currentVolume
                try {
                    mediaPlayer?.setVolume(volume, volume)
                } catch (e: IllegalStateException) {
                    // Ignorar
                }
                delay(25)
            }
            // Aseguramos que el volumen final sea el correcto
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        }
    }

    // --- INICIO DE LAS NUEVAS FUNCIONES DE FUNDIDO ---
    private fun fadeIn() {
        scope.launch {
            for (i in 0..100 step 10) { // Pasos de 10 para un fundido más rápido
                val volume = (i / 100f) * currentVolume
                mediaPlayer?.setVolume(volume, volume)
                delay(50) // 50ms entre cada paso
            }
        }
    }

    private suspend fun fadeOut() {
        if (mediaPlayer == null) return
        // El fade out empieza desde el volumen actual
        val startVolume = mediaPlayer?.let { mp ->
            try {
                val audioAttributes = mp.javaClass.getDeclaredField("mVolume")
                audioAttributes.isAccessible = true
                audioAttributes.getFloat(mp)
            } catch (e: Exception) {
                currentVolume
            }
        } ?: currentVolume

        for (i in 100 downTo 0 step 10) {
            val volume = (i / 100f) * startVolume
            try {
                mediaPlayer?.setVolume(volume, volume)
            } catch (e: IllegalStateException) {
                // Ignorar
            }
            delay(50)
        }
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}