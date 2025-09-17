package com.akrubastudios.playquizgames.core

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.akrubastudios.playquizgames.R
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Define los efectos de sonido disponibles en la aplicación.
 * Usar un enum previene errores y centraliza la gestión de los sonidos.
 */
enum class SoundEffect(val resourceId: Int) {
    CORRECT_ANSWER(R.raw.sfx_correct_answer),
    INCORRECT_ANSWER(R.raw.sfx_incorrect_answer)
    // Aquí añadiremos más sonidos en el futuro (ej. BUTTON_CLICK)
}

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundEffect, Int>()

    // Variables para almacenar las preferencias del usuario
    private var isSfxEnabled = true
    private var sfxVolume = 1.0f

    init {
        // Leemos las preferencias del usuario al iniciar
        scope.launch {
            isSfxEnabled = settingsRepository.sfxEnabledFlow.first()
            sfxVolume = settingsRepository.sfxVolumeFlow.first()
        }
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        // Configuramos el SoundPool para que sea adecuado para juegos
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Permitimos hasta 5 sonidos simultáneos
            .setAudioAttributes(audioAttributes)
            .build()

        // Pre-cargamos todos los sonidos definidos en el enum
        SoundEffect.values().forEach { sound ->
            val soundId = soundPool?.load(context, sound.resourceId, 1)
            if (soundId != null) {
                soundMap[sound] = soundId
            }
        }

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d("SoundManager", "Sonido $sampleId cargado correctamente.")
            } else {
                Log.e("SoundManager", "Error al cargar el sonido $sampleId. Estado: $status")
            }
        }
    }
    /**
     * Reproduce un efecto de sonido si los SFX están habilitados.
     */
    fun playSound(sound: SoundEffect) {
        if (!isSfxEnabled) return

        val soundId = soundMap[sound]
        if (soundId != null) {
            // Parámetros: soundID, leftVolume, rightVolume, priority, loop, rate
            soundPool?.play(soundId, sfxVolume, sfxVolume, 1, 0, 1.0f)
        } else {
            Log.w("SoundManager", "El sonido ${sound.name} no se ha cargado todavía.")
        }
    }

    /**
     * Actualiza el estado de habilitado/deshabilitado de los SFX y guarda la preferencia.
     */
    fun setSfxEnabled(isEnabled: Boolean) {
        isSfxEnabled = isEnabled
        scope.launch {
            settingsRepository.saveSfxEnabled(isEnabled)
        }
    }

    /**
     * Actualiza el volumen de los SFX y guarda la preferencia.
     */
    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
        scope.launch {
            settingsRepository.saveSfxVolume(sfxVolume)
        }
    }
}