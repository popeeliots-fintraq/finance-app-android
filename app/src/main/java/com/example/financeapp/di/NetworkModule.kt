package com.example.financeapp.di

import android.content.Context
import androidx.room.Room
import com.example.financeapp.BuildConfig 
import com.example.financeapp.data.local.AppDatabase
import com.example.financeapp.data.dao.LeakageBucketDao
import com.example.financeapp.data.dao.RawTransactionDao
import com.example.financeapp.data.dao.UserSettingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase // <-- Critical SQLCipher Import
import net.sqlcipher.database.SupportFactory // <-- Critical SQLCipher Import
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabasePassphrase(): String {
        // Fallback or a default secure key structure.
        return if (BuildConfig.DEBUG) "fintraq_dev_key" else BuildConfig.DB_SECRET
    }

    @Provides
    @Singleton
    fun provideSupportFactory(passphrase: String): SupportFactory {
        val phrase = passphrase.toByteArray(Charsets.UTF_8)
        
        return SupportFactory(phrase, object : SQLiteDatabase.CustomDelegate() {})
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportFactory
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .openHelperFactory(supportFactory)
            .allowMainThreadQueries() 
            .build()
    }

    // --- DAO Providers ---

    @Provides
    fun provideRawTransactionDao(appDatabase: AppDatabase): RawTransactionDao {
        return appDatabase.rawTransactionDao()
    }

    @Provides
    fun provideLeakageBucketDao(appDatabase: AppDatabase): LeakageBucketDao {
        return appDatabase.leakageBucketDao()
    }

    @Provides
    fun provideUserSettingDao(appDatabase: AppDatabase): UserSettingDao {
        return appDatabase.userSettingDao()
    }
}
