package com.example.financeapp // Use your root package

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// This assumes your SalaryBucket.kt is accessible from this package
@Dao
interface SalaryBucketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: SalaryBucket)

    @Update
    suspend fun update(bucket: SalaryBucket)

    @Query("SELECT * FROM salary_buckets")
    suspend fun getAllBuckets(): List<SalaryBucket>
}

// This assumes your LeakBucket.kt is accessible from this package
@Dao
interface LeakBucketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: LeakBucket)

    @Update
    suspend fun update(bucket: LeakBucket)

    @Query("SELECT * FROM leak_buckets")
    suspend fun getAllBuckets(): List<LeakBucket>
    
    // A key query for your Leakage Bucket View
    @Query("SELECT * FROM leak_buckets WHERE bucketName = :name")
    suspend fun getBucketByName(name: String): LeakBucket?
}
