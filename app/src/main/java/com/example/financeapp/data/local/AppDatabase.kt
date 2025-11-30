package com.example.financeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.dao.SalaryBucketDao
import com.example.financeapp.data.dao.TransactionDao
import com.example.financeapp.data.dao.SmsDao

import com.example.financeapp.data.model.LeakBucket
import com.example.financeapp.data.model.SalaryBucket
import com.example.financeapp.data.model.TransactionEntity
import com.example.financeapp.data.model.SmsEntity

/**
 * Central encrypted Room database for Fin-Traq.
 */
@Database(
    entities = [
        LeakBucket::class,
        SalaryBucket::class,
        TransactionEntity::class,
        SmsEntity::class    // ✅ CRITICAL: ADDED
    ],
    version = 3,  // bumped after schema correction
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leakBucketDao(): LeakBucketDao
    abstract fun salaryBucketDao(): SalaryBucketDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smsDao(): SmsDao   // ✅ CRITICAL: ADDED

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            passphrase: String
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                SQLiteDatabase.loadLibs(context)

                val factory = SupportFactory(
                    SQLiteDatabase.getBytes(passphrase.toCharArray())
                )

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fintraq_database"
                )
                    .fallbackToDestructiveMigration()
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
