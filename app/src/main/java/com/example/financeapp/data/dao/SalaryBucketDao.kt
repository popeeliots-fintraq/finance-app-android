package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financeapp.data.local.SalaryBucket
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the SalaryBucket entity.
 * This tracks the user's base salary and calculated disposable income.
 */
@Dao
interface SalaryBucketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: SalaryBucket): Long

    /**
     * Retrieves the most recent SalaryBucket record.
     * This is typically used to get the current baseline salary and projection data.
     */
    @Query("SELECT * FROM salary_buckets ORDER BY timestamp DESC LIMIT 1")
    fun getCurrentSalaryBucket(): Flow<SalaryBucket?>

    /**
     * Used to query historical salary information if needed for trend analysis.
     */
    @Query("SELECT * FROM salary_buckets ORDER BY timestamp DESC")
    fun getAllSalaryBuckets(): Flow<List<SalaryBucket>>
}
