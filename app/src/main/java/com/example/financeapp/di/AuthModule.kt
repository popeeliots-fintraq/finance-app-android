package com.example.financeapp.di

import com.example.financeapp.auth.SecureTokenStore
import com.example.financeapp.auth.ITokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideTokenStore(
        @ApplicationContext context: Context
    ): SecureTokenStore = SecureTokenStore(context)
}
