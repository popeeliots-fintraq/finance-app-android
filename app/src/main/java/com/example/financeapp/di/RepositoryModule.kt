package com.example.financeapp.di

import com.example.financeapp.ApiService
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.repository.FinanceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFinanceRepository(
        apiService: ApiService,
        smsDao: SmsDao
    ): FinanceRepository = FinanceRepository(apiService, smsDao)
}
