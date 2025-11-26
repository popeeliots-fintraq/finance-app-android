package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.BuildConfig
import com.example.financeapp.data.db.AppDatabase
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.dao.SalaryBucketDao
import com.example.financeapp.data.dao.LeakBucketDao
import com.example.financeapp.data.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module to provide the singleton instances of the encrypted Room Database and its DAOs.
 * It uses the SQLCipher library for robust encryption based on a key in BuildConfig.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Helper method to safely provide the database passphrase
    @Provides
    fun provideDatabasePassphrase(): String {
        // Use the secure key from BuildConfig (V2 standard)
        return BuildConfig.DB_PASSPHRASE
    }

    // Provides the SupportFactory required by Room to handle SQLCipher
    @Provides
    @Singleton
    fun provideSupportFactory(passphrase: String): SupportFactory {
        val phrase = SQLiteDatabase.getBytes(passphrase.toCharArray())
        // Note: Using a simplified CustomDelegate approach for maximum compatibility
        return SupportFactory(phrase)
    }

    // Provides the encrypted AppDatabase singleton
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportFactory
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sms_database" // Must match AppDatabase name
        )
            .fallbackToDestructiveMigration()
            // Apply the SQLCipher factory for encryption
            .openHelperFactory(supportFactory) 
            .build()
    }

    // --- DAO Providers ---
    // All DAOs required by the FinanceRepository are now provided here.

    @Provides
    fun provideSmsDao(database: AppDatabase): SmsDao {
        return database.smsDao()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideSalaryBucketDao(database: AppDatabase): SalaryBucketDao {
        return database.salaryBucketDao()
    }

    @Provides
    fun provideLeakBucketDao(database: AppDatabase): LeakBucketDao {
        return database.leakBucketDao()
    }
}
