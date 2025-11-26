package com.example.financeapp.data.repository

import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.dao.SalaryBucketDao
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.dao.TransactionDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The core data repository managing all data sources (local Room database and remote API).
 */
@Singleton
class FinanceRepository @Inject constructor(
    private val apiService: ApiService,
    private val smsDao: SmsDao,
    // Dependency must match the provider in RepositoryModule
    private val transactionDao: TransactionDao,
    private val salaryBucketDao: SalaryBucketDao,
    private val leakBucketDao: LeakBucketDao
) {
    // --- Public API for the Repository ---

    /**
     * Retrieves the data stream for all transactions.
     */
    fun getAllTransactions() = transactionDao.getAllTransactions()
}
