package com.akrubastudios.playquizgames

import android.app.Application
import com.akrubastudios.playquizgames.performance.OceanConfigManager
import dagger.hilt.android.HiltAndroidApp
import com.google.android.gms.ads.MobileAds

@HiltAndroidApp
class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar el gestor de configuración oceánica
        // Esto se hace una sola vez al iniciar la app
        OceanConfigManager.getInstance(this)

        // Inicializa el SDK de AdMob
        MobileAds.initialize(this) {}
    }
}