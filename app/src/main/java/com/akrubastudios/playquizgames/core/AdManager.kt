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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    private var interstitialAd: InterstitialAd? = null
    private var adShowCounter = 0
    private const val AD_FREQUENCY = 3
    // Variable para almacenar el anuncio bonificado cargado
    private var rewardedAd: RewardedAd? = null

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

    /**
     * Carga un anuncio bonificado en memoria.
     * Debe llamarse al iniciar la pantalla donde se mostrará el anuncio.
     */
    fun loadRewardedAd(context: Context) {
        // ID de unidad de anuncio de prueba para anuncios bonificados.
        // NUNCA uses anuncios reales en modo de desarrollo.
        val adUnitId = "ca-app-pub-3940256099942544/5224354917"

        Log.d(TAG, "Cargando anuncio bonificado...")

        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(TAG, "✅ Anuncio bonificado cargado exitosamente.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    Log.e(TAG, "❌ Error al cargar anuncio bonificado:")
                    Log.e(TAG, "   Código: ${adError.code}")
                    Log.e(TAG, "   Mensaje: ${adError.message}")
                }
            }
        )
    }

    /**
     * Muestra el anuncio bonificado si está cargado.
     *
     * @param activity La Activity actual sobre la cual se mostrará el anuncio.
     * @param onRewardGranted Una función callback que se ejecutará SOLAMENTE
     * si el usuario ve el video completo y gana la recompensa.
     */
    fun showRewardedAd(activity: Activity, onRewardGranted: () -> Unit) {
        if (rewardedAd == null) {
            Log.w(TAG, "Anuncio bonificado no disponible. Intentando precargar...")
            // Si no hay un anuncio, lo cargamos para la próxima vez.
            loadRewardedAd(activity)
            // Aquí podrías notificar a la UI que no hay anuncio disponible.
            return
        }

        // Configura los callbacks para los eventos del anuncio
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            // Se llama cuando el usuario cierra el anuncio.
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Anuncio bonificado cerrado por el usuario.")
                // Un anuncio bonificado solo se puede mostrar una vez.
                // Lo ponemos en null y cargamos el siguiente para la próxima vez.
                rewardedAd = null
                loadRewardedAd(activity)
            }

            // Se llama si hubo un error al mostrar el anuncio.
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Error al mostrar anuncio bonificado: ${adError.message}")
                rewardedAd = null
            }

            // Se llama justo cuando el anuncio se muestra.
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Anuncio bonificado mostrado exitosamente.")
            }
        }

        // Muestra el anuncio y maneja la recompensa.
        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "Recompensa ganada. Tipo: ${rewardItem.type}, Cantidad: ${rewardItem.amount}")
            // Ejecutamos la función que nos pasaron para otorgar la recompensa.
            onRewardGranted()
        }
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