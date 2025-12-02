package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.dao.RawTransactionDao
import com.example.financeapp.data.dao.SalaryBucketDao
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
        // The repository now requires all DAOs related to the new "Bucket" structure
        apiService: ApiService,
        smsDao: SmsDao, // Keeping the original SMS DAO
        rawTransactionDao: RawTransactionDao, // New requirement
        salaryBucketDao: SalaryBucketDao, // New requirement
        leakBucketDao: LeakBucketDao // New requirement
    ): FinanceRepository = FinanceRepository(
        apiService, 
        smsDao, 
        rawTransactionDao, 
        salaryBucketDao, 
        leakBucketDao // Assuming the FinanceRepository constructor has been updated to accept these
    )
}
