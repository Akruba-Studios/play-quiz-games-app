package com.akrubastudios.playquizgames.data.repository

import com.akrubastudios.playquizgames.domain.Country
import com.akrubastudios.playquizgames.domain.User
import com.akrubastudios.playquizgames.domain.UserCountryProgress
import com.google.firebase.auth.FirebaseAuth
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

    // Obtiene un único documento de la colección 'countries'
    suspend fun getCountry(countryId: String): Country? {
        return try {
            db.collection("countries").document(countryId).get().await()
                .toObject(Country::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Obtiene el progreso del usuario actual para un país específico
    suspend fun getUserProgressForCountry(countryId: String): UserCountryProgress? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val progressDocId = "${uid}_${countryId}"
        return try {
            db.collection("user_country_progress").document(progressDocId).get().await()
                .toObject(UserCountryProgress::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Obtiene el documento del usuario actual desde la colección 'users'
    suspend fun getUserData(): User? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return try {
            db.collection("users").document(uid).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}