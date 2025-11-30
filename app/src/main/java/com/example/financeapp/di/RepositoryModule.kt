package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.*
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
