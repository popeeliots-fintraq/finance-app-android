// file:///.../SmsDatabase.kt

package com.example.financeapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.example.financeapp.data.local.SalaryBucket
import com.example.financeapp.data.local.LeakBucket
import com.example.financeapp.data.local.SalaryBucketDao
import com.example.financeapp.data.local.LeakBucketDao
import com.example.financeapp.data.model.LocalSmsRecord // 🚨 CRITICAL FIX: Import new model
import com.example.financeapp.data.dao.SmsDao // 🚨 CRITICAL FIX: Import DAO from new package

// 🚨 CRITICAL FIXES:
// 1. Replaced SmsData::class with LocalSmsRecord::class in entities.
// 2. Increment version to 4 for the schema change.
@Database(entities = [LocalSmsRecord::class, SalaryBucket::class, LeakBucket::class], version = 4, exportSchema = false)
abstract class SmsDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao
    abstract fun salaryBucketDao(): SalaryBucketDao
    abstract fun leakBucketDao(): LeakBucketDao

    companion object {
        @Volatile
        private var INSTANCE: SmsDatabase? = null

        fun getDatabase(context: Context): SmsDatabase {
            return INSTANCE ?: synchronized(this) {
                // Ensure the BuildConfig is accessible for the passphrase
                // If this is causing issues, replace BuildConfig.DB_PASSPHRASE with a secure alternative.
                val passphrase: ByteArray = SQLiteDatabase.getBytes(BuildConfig.DB_PASSPHRASE.toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmsDatabase::class.java,
                    "sms_database"
                )
                    // Keep destructive migration during rapid development
                    .fallbackToDestructiveMigration() 
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
