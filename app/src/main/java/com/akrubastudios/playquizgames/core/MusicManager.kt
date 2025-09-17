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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class MusicTrack(val resourceId: Int) {
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
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        // Al iniciar, leemos la preferencia del usuario desde DataStore
        scope.launch {
            isMusicEnabled = settingsRepository.musicPreferenceFlow.first()
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
        // Si la música está desactivada por el usuario o si la pista que se pide es la que ya suena, no hacemos nada.
        if (!isMusicEnabled || currentTrack == track) return

        // Liberamos recursos del reproductor anterior, si existía.
        mediaPlayer?.release()
        mediaPlayer = null

        if (track == MusicTrack.NONE) {
            currentTrack = MusicTrack.NONE
            return
        }

        // Creamos un nuevo MediaPlayer con la pista solicitada.
        mediaPlayer = MediaPlayer.create(context, track.resourceId).apply {
            isLooping = true // Queremos que la música se repita
            start() // ¡La iniciamos!
        }
        // Guardamos la referencia de la pista que está sonando ahora.
        currentTrack = track
    }
    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrack = MusicTrack.NONE
    }

    private fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun resume() {
        // Solo reanudamos si había una pista sonando y no está ya en play
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
}