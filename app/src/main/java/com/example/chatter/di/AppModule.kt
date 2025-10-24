package com.example.chatter.di

import android.content.Context
import com.example.chatter.data.remote.SupaBaseStorageClient
import com.example.chatter.data.repositories.AuthenticationRepository
import com.example.chatter.data.repositories.AuthenticationRepositoryImpl
import com.example.chatter.data.repositories.ChatRepository
import com.example.chatter.data.repositories.ChatRepositoryImpl
import com.example.chatter.data.repositories.UserRepository
import com.example.chatter.data.repositories.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun getFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun getFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Singleton
    @Provides
    fun getSupaBaseStorage(@ApplicationContext context: Context): SupaBaseStorageClient {
        return SupaBaseStorageClient(context = context)
    }

    @Singleton
    @Provides
    fun getFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()


    @Singleton
    @Provides
    fun provideAuthenticationRepository(firebaseAuth: FirebaseAuth): AuthenticationRepository {
        return AuthenticationRepositoryImpl(firebaseAuth)
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        realTimeDatabase: FirebaseDatabase,
        firebaseMessaging: FirebaseMessaging
    ): UserRepository {
        return UserRepositoryImpl(realTimeDatabase, firebaseMessaging)
    }

    @Singleton
    @Provides
    fun provideChatRepository(
        realTimeDatabase: FirebaseDatabase,
        firebaseAuth: FirebaseAuth,
        firebaseMessaging: FirebaseMessaging,
        supaBaseStorageClient: SupaBaseStorageClient,
        @ApplicationContext context: Context
    ): ChatRepository {
        return ChatRepositoryImpl(
            realTimeDatabase,
            firebaseAuth,
            firebaseMessaging,
            supaBaseStorageClient,
            context
        )
    }


}
