package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.dao.TransactionDao 
import com.example.financeapp.data.dao.SalaryBucketDao
import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.repository.FinanceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide the singleton instance of the FinanceRepository.
 * This repository acts as the single source of truth for all data, coordinating
 * between the API service and the various Room Data Access Objects (DAOs).
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFinanceRepository(
        apiService: ApiService,
        smsDao: SmsDao,
        transactionDao: TransactionDao,
        salaryBucketDao: SalaryBucketDao,
        leakBucketDao: LeakBucketDao
    ): FinanceRepository {
        return FinanceRepository(
            apiService, 
            smsDao, 
            transactionDao, 
            salaryBucketDao, 
            leakBucketDao
        )
    }
}
