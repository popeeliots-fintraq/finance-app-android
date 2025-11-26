package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.dao.SalaryBucketDao
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.dao.TransactionDao
import com.example.financeapp.data.repository.FinanceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide the FinanceRepository instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFinanceRepository(
        apiService: ApiService,
        smsDao: SmsDao,
        // Dependency required to fix the original error
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
