package com.example.financeapp.data.repository

import android.util.Log
import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.SmsDao 
import com.example.financeapp.data.dao.TransactionDao // DAO is now correctly imported from data.dao
import com.example.financeapp.data.dao.SalaryBucketDao // DAO is now correctly imported from data.dao
import com.example.financeapp.data.dao.LeakBucketDao // DAO is now correctly imported from data.dao
import com.example.financeapp.data.local.SalaryBucket
import com.example.financeapp.data.model.LocalSmsRecord
import com.example.financeapp.data.local.TransactionEntity
import com.example.financeapp.data.local.LeakBucket
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for all data operations in Fin-Traq.
 * It coordinates data between the local Room DAOs and the remote ApiService.
 */
@Singleton
class FinanceRepository @Inject constructor(
    private val apiService: ApiService,
    private val smsDao: SmsDao,
    private val transactionDao: TransactionDao,
    private val salaryBucketDao: SalaryBucketDao,
    private val leakBucketDao: LeakBucketDao
) {
    private val TAG = "FinanceRepository"

    // --- SMS AUDIT FUNCTIONS ---
    suspend fun insertLocalSmsRecord(record: LocalSmsRecord): Long {
        return smsDao.insert(record)
    }

    fun getAllLocalSmsRecords(): Flow<List<LocalSmsRecord>> {
        return smsDao.getAllRecords()
    }

    // --- CORE V2 DATA BUCKET FUNCTIONS ---
    fun getCurrentSalaryBucket(): Flow<SalaryBucket?> {
        return salaryBucketDao.getCurrentSalaryBucket()
    }
    
    fun getLeaksForCurrentMonth(month: Int, year: Int): Flow<List<LeakBucket>> {
        return leakBucketDao.getLeaksForMonth(month, year)
    }

    // --- PROCESSED TRANSACTION FUNCTIONS ---
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }
    
    // --- API & DATA FETCHING ---
    suspend fun fetchLeakageView(reportingPeriod: String) {
        Log.d(TAG, "Placeholder: Fetching Leakage View for period: $reportingPeriod")
        // Future implementation will involve calling apiService and saving results to Room DAOs
    }
}
