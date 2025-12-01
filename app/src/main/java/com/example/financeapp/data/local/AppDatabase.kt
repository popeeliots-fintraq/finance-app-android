package com.example.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.financeapp.data.dao.*
import com.example.financeapp.data.model.*

/**
 * Central encrypted Room database for Fin-Traq.
 */
@Database(
    entities = [
        LeakBucket::class,
        SalaryBucket::class,
        TransactionEntity::class,
        SmsEntity::class,
        RawTransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leakBucketDao(): LeakBucketDao
    abstract fun salaryBucketDao(): SalaryBucketDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smsDao(): SmsDao
    abstract fun rawTransactionDao(): RawTransactionDao
}
