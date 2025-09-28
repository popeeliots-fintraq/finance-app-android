package com.example.financeapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportFactory

@Database(entities = [SmsData::class], version = 1, exportSchema = false)
abstract class SmsDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao

    companion object {
        @Volatile
        private var INSTANCE: SmsDatabase? = null
        // private val passphrase = SupportFactory(BuildConfig.DB_PASSPHRASE.toByteArray()) // Commented out for android build.

        fun getDatabase(context: Context): SmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmsDatabase::class.java,
                    "sms_database"
                )
                    .fallbackToDestructiveMigration()
                    .openHelperFactory(passphrase)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
