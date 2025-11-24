package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financeapp.data.local.LeakBucket
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the LeakBucket entity.
 * This tracks categorized spending that is identified as a "leak" by the system.
 */
@Dao
interface LeakBucketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: LeakBucket): Long

    /**
     * Retrieves all leak records for a specific month/year.
     * This is key for generating the "Leakage Bucket View" dashboard.
     */
    @Query("SELECT * FROM leak_buckets WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    fun getLeaksForMonth(month: Int, year: Int): Flow<List<LeakBucket>>

    /**
     * Retrieves a summary of all existing leak records, ordered by date.
     */
    @Query("SELECT * FROM leak_buckets ORDER BY timestamp DESC")
    fun getAllLeaks(): Flow<List<LeakBucket>>

    /**
     * Calculates the total leakage amount for a given period.
     */
    @Query("SELECT SUM(leakAmount) FROM leak_buckets WHERE month = :month AND year = :year")
    suspend fun getTotalLeakageForMonth(month: Int, year: Int): Double?
}
