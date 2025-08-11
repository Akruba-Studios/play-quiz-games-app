package com.akrubastudios.playquizgames.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    private var interstitialAd: InterstitialAd? = null
    private var adShowCounter = 0
    private const val AD_FREQUENCY = 3

    fun loadInterstitialAd(context: Context) {
        // Para producción, usa tu ID real de anuncio
        // val adUnitId = "ca-app-pub-4912059647400974~TU_AD_UNIT_ID"

        // Para pruebas en dispositivo real:
        val adUnitId = "ca-app-pub-3940256099942544/1033173712"

        Log.d(TAG, "Cargando anuncio intersticial...")

        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "✅ Anuncio cargado exitosamente.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "❌ Error al cargar anuncio:")
                    Log.e(TAG, "   Código: ${adError.code}")
                    Log.e(TAG, "   Mensaje: ${adError.message}")

                    // En emulador siempre falla, en dispositivo real funciona
                    if (adError.code == 0) {
                        Log.w(TAG, "   ⚠️ Posible problema de emulador - prueba en dispositivo real")
                    }
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity) {
        adShowCounter++
        Log.d(TAG, "Intento de mostrar anuncio número: $adShowCounter")

        if (interstitialAd == null) {
            Log.w(TAG, "Anuncio no disponible - precargando...")
            loadInterstitialAd(activity)
            return
        }

        if (adShowCounter % AD_FREQUENCY == 0) {
            Log.d(TAG, "Mostrando anuncio.")

            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Anuncio cerrado por el usuario.")
                    interstitialAd = null
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Error al mostrar anuncio: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Anuncio mostrado exitosamente.")
                }
            }

            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "No es momento de mostrar anuncio (frecuencia).")
        }
    }
}