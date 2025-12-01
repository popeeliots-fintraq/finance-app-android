package com.example.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

import com.fintraq.app.data.dao.LeakBucketDao
import com.fintraq.app.data.dao.SalaryBucketDao
import com.fintraq.app.data.dao.TransactionDao
import com.fintraq.app.data.dao.SmsDao

import com.fintraq.app.data.model.LeakBucket
import com.fintraq.app.data.model.SalaryBucket
import com.fintraq.app.data.model.TransactionEntity
import com.fintraq.app.data.model.SmsEntity

/**
 * Central encrypted Room database for Fin-Traq.
 */
@Database(
    entities = [
        LeakBucket::class,
        SalaryBucket::class,
        TransactionEntity::class,
        SmsEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leakBucketDao(): LeakBucketDao
    abstract fun salaryBucketDao(): SalaryBucketDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smsDao(): SmsDao
}
