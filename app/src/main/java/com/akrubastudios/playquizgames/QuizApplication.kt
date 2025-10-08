package com.akrubastudios.playquizgames

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import com.google.android.gms.ads.MobileAds
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

@HiltAndroidApp
class QuizApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        // Inicializa el SDK de AdMob
        MobileAds.initialize(this) {}
    }

    // --- INICIO DEL CÓDIGO A AÑADIR ---
    /**
     * Esta función crea una instancia personalizada del ImageLoader de Coil
     * con optimizaciones para nuestra app.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Usa hasta el 25% de la RAM disponible para la caché en memoria
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05) // Usa hasta el 5% del espacio del disco para la caché
                    .build()
            }
            .okHttpClient {
                // El cambio más importante: Aumentamos el paralelismo
                OkHttpClient.Builder()
                    .dispatcher(Dispatcher().apply {
                        // Permite hasta 8 descargas simultáneas desde el mismo host
                        maxRequestsPerHost = 8
                    })
                    .build()
            }
            .respectCacheHeaders(false) // Ignora las cabeceras de caché, confía en nuestra política
            .build()
    }
}