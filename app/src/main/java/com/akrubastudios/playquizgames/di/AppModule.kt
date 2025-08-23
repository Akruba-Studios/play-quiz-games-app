package com.akrubastudios.playquizgames.di

import android.content.Context
import com.akrubastudios.playquizgames.core.LanguageManager
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.GameDataRepository
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import com.akrubastudios.playquizgames.data.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- PROVEEDORES DE SERVICIOS DE FIREBASE ---
    // Son las únicas herramientas de la nube que nuestra app necesita directamente.

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        // Reemplaza "us-central1" con tu región real si es diferente
        return Firebase.functions("us-central1")
    }

    // --- PROVEEDORES DE NUESTROS REPOSITORIOS ---

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        // Hilt proveerá el contexto de la aplicación para que SettingsRepository pueda usar DataStore.
        return SettingsRepository(context)
    }
    @Provides
    @Singleton
    fun provideQuizRepository(
        db: FirebaseFirestore
    ): QuizRepository {
        // QuizRepository ya no necesita Context porque no lee archivos locales
        return QuizRepository(db)
    }
    @Provides
    @Singleton
    fun provideGameDataRepository(
        db: FirebaseFirestore,
        functions: FirebaseFunctions // <-- AÑADE ESTA LÍNEA DE NUEVO
    ): GameDataRepository {
        return GameDataRepository(db, functions)
    }
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        gameDataRepository: GameDataRepository
    ): AuthRepository {
        return AuthRepository(auth, db, gameDataRepository)
    }
    @Provides
    @Singleton
    fun provideLanguageManager(
        settingsRepository: SettingsRepository
    ): LanguageManager {
        // Hilt ahora sabe que para crear un LanguageManager, primero debe crear
        // (o reutilizar) un SettingsRepository y pasárselo.
        return LanguageManager(settingsRepository)
    }
}