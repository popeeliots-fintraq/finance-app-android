package com.example.financeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
// CRITICAL IMPORTS: Assuming these entities and DAOs are defined elsewhere in the project
import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.dao.SalaryBucketDao
import com.example.financeapp.data.dao.TransactionDao
import com.example.financeapp.BuildConfig 
// Assuming LeakBucket, SalaryBucket, and TransactionEntity are defined within this package or explicitly imported

/**
 * Room Database for the encrypted storage of all Fin-Traq data.
 *
 * @property entities Includes all existing bucket entities and the new TransactionEntity.
 * @property version Incremented to 2 due to the addition of the TransactionEntity table.
 */
@Database(
    entities = [
        LeakBucket::class,
        SalaryBucket::class,
        TransactionEntity::class // NEW: For granular transaction history
    ],
    version = 2, // CRITICAL: Incremented from 1 to 2 for the schema change
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // Existing DAOs
    abstract fun leakBucketDao(): LeakBucketDao
    abstract fun salaryBucketDao(): SalaryBucketDao

    // NEW DAO for transaction history
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Creates or retrieves the encrypted Room database instance using SQLCipher.
         *
         * @param context Application context.
         * @param passphrase The encryption key (DB_PASSPHRASE from BuildConfig).
         */
        fun getDatabase(context: Context, passphrase: String): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // 1. Load SQLCipher native libraries
                SQLiteDatabase.loadLibs(context)

                // 2. Create the encryption factory using the passphrase
                // We use the passphrase parameter here, as the Hilt module will handle the BuildConfig injection
                val factory = SupportFactory(
                    SQLiteDatabase.getBytes(passphrase.toCharArray())
                )

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fintraq_database" // Using your existing database name
                )
                    // Allows Room to rebuild the database if no migrations are provided (safe for development)
                    .fallbackToDestructiveMigration()
                    // 3. Apply the SQLCipher encryption factory
                    .openHelperFactory(factory)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
