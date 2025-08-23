package com.akrubastudios.playquizgames.di

import android.content.Context
import androidx.datastore.core.DataStore // <-- NUEVA IMPORTACIÓN
import androidx.datastore.preferences.core.Preferences // <-- NUEVA IMPORTACIÓN
import androidx.datastore.preferences.preferencesDataStore // <-- NUEVA IMPORTACIÓN
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
import kotlinx.coroutines.CoroutineScope // <-- NUEVA IMPORTACIÓN
import kotlinx.coroutines.Dispatchers // <-- NUEVA IMPORTACIÓN
import kotlinx.coroutines.SupervisorJob // <-- NUEVA IMPORTACIÓN
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Definimos la instancia de DataStore aquí, de forma centralizada.
private const val USER_PREFERENCES_NAME = "user_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- PROVEEDORES DE SERVICIOS DE FIREBASE ---
    // Son las únicas herramientas de la nube que nuestra app necesita directamente.

    // --- PROVEEDOR DE DATASTORE ---
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return appContext.dataStore
    }

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
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        // Ahora inyectamos la instancia de DataStore directamente.
        return SettingsRepository(dataStore)
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
    fun provideLanguageManager(settingsRepository: SettingsRepository): LanguageManager {
        return LanguageManager(settingsRepository)
    }
}