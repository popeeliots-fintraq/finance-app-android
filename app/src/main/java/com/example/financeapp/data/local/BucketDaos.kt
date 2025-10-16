package com.example.financeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.local.SalaryBucket
import com.example.financeapp.data.local.LeakBucket

// DAO for the source of funds
@Dao
interface SalaryBucketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: SalaryBucket)

    @Update
    suspend fun update(bucket: SalaryBucket)

    @Query("SELECT * FROM Salary_buckets")
    suspend fun getAllBuckets(): List<SalaryBucket>
}

// DAO for the destination of funds (the leaks)
@Dao
interface LeakBucketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: LeakBucket)

    @Update
    suspend fun update(bucket: LeakBucket)

    @Query("SELECT * FROM Leak_buckets")
    suspend fun getAllBuckets(): List<LeakBucket>
    
    // A key query for Fintraq's vision!
    @Query("SELECT * FROM Leak_buckets WHERE bucketName = :name")
    suspend fun getBucketByName(name: String): LeakBucket?
}
