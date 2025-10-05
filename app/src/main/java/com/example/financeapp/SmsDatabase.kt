package com.example.financeapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [SmsData::class], version = 1, exportSchema = false)
abstract class SmsDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao

    companion object {
        @Volatile
        private var INSTANCE: SmsDatabase? = null

        fun getDatabase(context: Context): SmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase: ByteArray = SQLiteDatabase.getBytes(BuildConfig.DB_PASSPHRASE.toCharArray())
                val factory = SupportFactory(passphrase)

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
