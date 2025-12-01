package com.example.financeapp.di

import com.fintraq.app.data.api.ApiService
import com.fintraq.app.data.dao.SmsDao
import com.fintraq.app.data.repository.FinanceRepository
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
    ): FinanceRepository {
        return FinanceRepository(apiService, smsDao)
    }
}
