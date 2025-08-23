package com.akrubastudios.playquizgames.core

import android.util.Log
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _languageStateFlow = MutableStateFlow(Locale.getDefault().language)
    val languageStateFlow: StateFlow<String> = _languageStateFlow.asStateFlow()

    init {
        scope.launch {
            settingsRepository.languagePreferenceFlow.collect { savedLanguage ->
                Log.d("LanguageDebug", "[PASO 4] LanguageManager: Flow ha emitido un nuevo valor -> '$savedLanguage'")
                _languageStateFlow.value = savedLanguage
            }
        }
    }

    fun setLanguage(languageCode: String) {
        Log.d("LanguageDebug", "[PASO 2] LanguageManager: setLanguage llamado con '$languageCode'")
        scope.launch {
            settingsRepository.saveLanguagePreference(languageCode)
        }
    }
}