package com.akrubastudios.playquizgames.core

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.akrubastudios.playquizgames.domain.Country
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Gestor centralizado de precarga de imágenes.
 * Se encarga de precargar imágenes de países de forma inteligente y progresiva.
 * Mantiene un registro de qué imágenes ya fueron precargadas en esta sesión.
 */
@Singleton
class PrecacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) {
    // Scope propio para operaciones de precarga en background
    private val precacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Registro de URLs ya precargadas en esta sesión
    private val precachedUrls = mutableSetOf<String>()

    // Lock para acceso thread-safe al Set
    private val lock = Any()

    /**
     * CAPA 1: Precarga inmediata del país seleccionado y sus vecinos.
     * Se ejecuta de forma síncrona (blocking) para garantizar que esté listo.
     */
    suspend fun precacheCountryAndNeighbors(
        countryId: String,
        allCountries: List<Country>
    ) = coroutineScope {
        val targetCountry = allCountries.find { it.countryId == countryId }
        if (targetCountry == null) {
            Log.e("PrecacheManager", "❌ País no encontrado: $countryId")
            return@coroutineScope
        }

        // Lista de países a precargar: el país objetivo + sus vecinos
        val countriesToPrecache = mutableListOf(targetCountry)
        targetCountry.neighbors.forEach { neighborId ->
            allCountries.find { it.countryId == neighborId }?.let { countriesToPrecache.add(it) }
        }

        val urls = countriesToPrecache
            .map { it.backgroundImageUrl }
            .filter { it.isNotBlank() && !isAlreadyPrecached(it) }

        if (urls.isEmpty()) {
            Log.d("PrecacheManager", "✅ País y vecinos ya precargados: $countryId")
            return@coroutineScope
        }

        Log.d("PrecacheManager", "🎯 CAPA 1: Precargando país $countryId + ${urls.size - 1} vecinos")

        val jobs = urls.map { url ->
            async(Dispatchers.IO) {
                val success = precacheSingleImage(url)
                if (success) markAsPrecached(url)
                success
            }
        }
        jobs.awaitAll()

        Log.d("PrecacheManager", "✅ CAPA 1 completada para: $countryId")
    }

    /**
     * CAPA 2: Precarga en background de todos los países del continente.
     * Se ejecuta de forma asíncrona sin bloquear.
     */
    fun precacheContinentInBackground(
        continentId: String,
        allCountries: List<Country>
    ) {
        precacheScope.launch {
            val continentCountries = allCountries.filter { it.continentId == continentId }
            val urls = continentCountries
                .map { it.backgroundImageUrl }
                .filter { it.isNotBlank() && !isAlreadyPrecached(it) }

            if (urls.isEmpty()) {
                Log.d("PrecacheManager", "✅ Continente ya precargado: $continentId")
                return@launch
            }

            Log.d("PrecacheManager", "🌍 CAPA 2: Precargando ${urls.size} países del continente $continentId en background")

            urls.forEach { url ->
                val success = precacheSingleImage(url)
                if (success) markAsPrecached(url)
            }

            Log.d("PrecacheManager", "✅ CAPA 2 completada para continente: $continentId")
        }
    }

    /**
     * Precarga una sola imagen usando Coil.
     */
    private suspend fun precacheSingleImage(imageUrl: String): Boolean {
        if (imageUrl.isBlank()) return false

        return suspendCancellableCoroutine { continuation ->
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .listener(
                    onSuccess = { _, result ->
                        Log.d("PrecacheManager", "✅ Precargada desde ${result.dataSource}: ${imageUrl.takeLast(30)}")
                        if (continuation.isActive) continuation.resume(true)
                    },
                    onError = { _, result ->
                        Log.e("PrecacheManager", "❌ Error al precargar: ${result.throwable}")
                        if (continuation.isActive) continuation.resume(false)
                    }
                )
                .build()

            imageLoader.enqueue(request)

            continuation.invokeOnCancellation {
                // Coil maneja la cancelación automáticamente
            }
        }
    }

    /**
     * Verifica si una URL ya fue precargada en esta sesión.
     */
    private fun isAlreadyPrecached(url: String): Boolean {
        synchronized(lock) {
            return precachedUrls.contains(url)
        }
    }

    /**
     * Marca una URL como precargada.
     */
    private fun markAsPrecached(url: String) {
        synchronized(lock) {
            precachedUrls.add(url)
        }
    }

    /**
     * Limpia el registro de precarga (útil para testing o reinicio de sesión).
     */
    fun clearPrecacheRegistry() {
        synchronized(lock) {
            precachedUrls.clear()
        }
        Log.d("PrecacheManager", "🗑️ Registro de precarga limpiado")
    }
}