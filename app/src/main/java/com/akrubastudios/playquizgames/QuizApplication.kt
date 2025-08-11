package com.akrubastudios.playquizgames

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.google.android.gms.ads.MobileAds

@HiltAndroidApp
class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa el SDK de AdMob
        MobileAds.initialize(this) {}
    }
}