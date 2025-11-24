package com.example.financeapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.example.financeapp.BuildConfig // Assuming BuildConfig is available for DB_PASSPHRASE
import com.example.financeapp.data.local.SalaryBucket
import com.example.financeapp.data.local.LeakBucket
import com.example.financeapp.data.local.SalaryBucketDao
import com.example.financeapp.data.local.LeakBucketDao
import com.example.financeapp.data.model.LocalSmsRecord
import com.example.financeapp.data.dao.SmsDao

/**
 * The main Room Database definition for the application, using SQLCipher for encryption.
 * It combines the core finance concepts (Salary/Leak Buckets) with the Local SMS Records.
 */
@Database(
    entities = [LocalSmsRecord::class, SalaryBucket::class, LeakBucket::class], 
    version = 4, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao
    abstract fun salaryBucketDao(): SalaryBucketDao
    abstract fun leakBucketDao(): LeakBucketDao

    // NOTE: Keeping the companion object structure for reference, but Hilt will use the injection method.
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // SQLCipher Initialization
                val passphrase: ByteArray = SQLiteDatabase.getBytes(BuildConfig.DB_PASSPHRASE.toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_database"
                )
                    .fallbackToDestructiveMigration() // Use this only during development
                    .openHelperFactory(factory)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
