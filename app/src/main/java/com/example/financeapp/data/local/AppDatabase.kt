// AppDatabase.kt
package com.example.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// NOTE: You will need to import your other entities (e.g., SalaryBucket) here as well.
// Assuming your SalaryBucket.kt is also in this package.

// 🚨 CRITICAL: List all your entities here.
@Database(
    entities = [
        LeakBucket::class,
        SalaryBucket::class // Assuming this is the name of your other entity
    ],
    version = 1, // Start at version 1
    exportSchema = true 
)
abstract class AppDatabase : RoomDatabase() {

    // 🚨 CRITICAL: Expose all your DAOs here.
    abstract fun leakBucketDao(): LeakBucketDao
    abstract fun salaryBucketDao(): SalaryBucketDao

    // This companion object handles singleton creation of the database instance.
    companion object {
        // Marks the field as immediately visible to other threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Placeholder function for database creation/retrieval
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fintraq_database"
                )
                // Since you are using SQLCipher, the final setup will be slightly more complex
                // (using net.zetetic:android-database-sqlcipher:4.5.4) which we will add later.
                // For now, let's just make Kapt compile the structure.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
