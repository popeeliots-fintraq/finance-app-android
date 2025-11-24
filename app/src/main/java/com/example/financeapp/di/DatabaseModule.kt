package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.BuildConfig
import com.example.financeapp.data.db.AppDatabase
import com.example.financeapp.data.dao.SmsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

/**
 * Hilt module to provide the singleton instances of the Room Database and its DAOs,
 * incorporating the SQLCipher encryption setup for the AppDatabase.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        // --- SQLCipher Setup: Reads passphrase from BuildConfig and creates the factory ---
        val passphrase: ByteArray = SQLiteDatabase.getBytes(BuildConfig.DB_PASSPHRASE.toCharArray())
        val factory = SupportFactory(passphrase)
        // --------------------------------------------------------------------------------

        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "sms_database" // Must match the name used in AppDatabase's companion object
        )
        .fallbackToDestructiveMigration()
        .openHelperFactory(factory) // Apply the SQLCipher factory for encryption
        .build()
    }

    @Provides
    fun provideSmsDao(database: AppDatabase): SmsDao {
        return database.smsDao()
    }
    
    // You would also provide SalaryBucketDao and LeakBucketDao here
    /*
    @Provides
    fun provideSalaryBucketDao(database: AppDatabase): SalaryBucketDao {
        return database.salaryBucketDao()
    }

    @Provides
    fun provideLeakBucketDao(database: AppDatabase): LeakBucketDao {
        return database.leakBucketDao()
    }
    */
}
