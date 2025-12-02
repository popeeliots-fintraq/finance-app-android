package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.data.local.AppDatabase
import com.example.financeapp.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // <<< HERE IS YOUR PASSPHRASE >>>
    private const val DATABASE_PASSPHRASE = "fintraq_secure_key_123"

    @Provides
    @Singleton
    fun provideSupportFactory(): SupportFactory {
        val bytes = net.sqlcipher.database.SQLiteDatabase.getBytes(
            DATABASE_PASSPHRASE.toCharArray()
        )
        return SupportFactory(bytes)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportFactory
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "fintraq_database"
    )
        .fallbackToDestructiveMigration()
        .openHelperFactory(supportFactory)
        .build()

    @Provides fun provideSmsDao(db: AppDatabase): SmsDao = db.smsDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideSalaryBucketDao(db: AppDatabase): SalaryBucketDao = db.salaryBucketDao()
    @Provides fun provideLeakBucketDao(db: AppDatabase): LeakBucketDao = db.leakBucketDao()
    @Provides fun provideRawTransactionDao(db: AppDatabase): RawTransactionDao = db.rawTransactionDao()
}
