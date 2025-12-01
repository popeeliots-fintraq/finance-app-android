package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.fintraq.app.BuildConfig
import com.fintraq.app.data.local.AppDatabase
import com.fintraq.app.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabasePassphrase(): String =
        BuildConfig.DB_PASSPHRASE

    @Provides
    @Singleton
    fun provideSupportFactory(passphrase: String): SupportFactory {
        val bytes = SQLiteDatabase.getBytes(passphrase.toCharArray())
        return SupportFactory(bytes)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportFactory
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fintraq_database"
        )
            .fallbackToDestructiveMigration()
            .openHelperFactory(supportFactory)
            .build()
    }

    @Provides fun provideSmsDao(db: AppDatabase): SmsDao = db.smsDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideSalaryBucketDao(db: AppDatabase): SalaryBucketDao = db.salaryBucketDao()
    @Provides fun provideLeakBucketDao(db: AppDatabase): LeakBucketDao = db.leakBucketDao()
}
