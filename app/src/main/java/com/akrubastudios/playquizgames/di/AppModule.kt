package com.akrubastudios.playquizgames.di

import android.content.Context
import com.akrubastudios.playquizgames.data.repository.AuthRepository
import com.akrubastudios.playquizgames.data.repository.QuizRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideQuizRepository(
        db: FirebaseFirestore,
        @ApplicationContext context: Context
    ): QuizRepository {
        return QuizRepository(db, context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore
    ): AuthRepository {
        return AuthRepository(auth, db)
    }
}