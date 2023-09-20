package com.example.bobrarium_v2.firebase

import com.example.bobrarium_v2.firebase.auth.AuthRepository
import com.example.bobrarium_v2.firebase.auth.AuthRepositoryImpl
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepository
import com.example.bobrarium_v2.firebase.chat.FirebaseChatRepositoryImpl
import com.example.bobrarium_v2.firebase.user.UserRepository
import com.example.bobrarium_v2.firebase.user.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesFirebaseAuth() = Firebase.auth


    @Provides
    @Singleton
    fun providesRepositoryImpl(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun providesFirebaseDatabase() = Firebase.database

    @Provides
    @Singleton
    fun providesFirebaseStorage() = Firebase.storage

    @Provides
    @Singleton
    fun providesChatRepositoryImpl(database: FirebaseDatabase, storage: FirebaseStorage, auth: FirebaseAuth): FirebaseChatRepository {
        return FirebaseChatRepositoryImpl(database, storage, auth)
    }

    @Provides
    @Singleton
    fun providesUserRepositoryImpl(database: FirebaseDatabase, storage: FirebaseStorage): UserRepository {
        return UserRepositoryImpl(database, storage)
    }
}