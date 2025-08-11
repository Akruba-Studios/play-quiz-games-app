package com.akrubastudios.playquizgames.data.repository

import com.akrubastudios.playquizgames.domain.Country
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GameDataRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    // Esta función obtiene TODOS los documentos de la colección 'countries'
    suspend fun getCountryList(): List<Country> {
        return try {
            val snapshot = db.collection("countries").get().await()
            snapshot.toObjects(Country::class.java) // Convierte todos los documentos a una lista de objetos Country
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Devuelve una lista vacía si hay un error
        }
    }
}