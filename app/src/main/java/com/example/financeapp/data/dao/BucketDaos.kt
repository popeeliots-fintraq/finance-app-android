package com.example.financeapp.data.dao

import androidx.room.*
import com.example.financeapp.data.local.entity.LeakBucket
import com.example.financeapp.data.local.entity.SalaryBucket

@Dao
interface SalaryBucketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: SalaryBucket)

    @Update
    suspend fun update(bucket: SalaryBucket)

    @Query("SELECT * FROM salary_buckets")
    suspend fun getAllBuckets(): List<SalaryBucket>
}

@Dao
interface LeakBucketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: LeakBucket)

    @Update
    suspend fun update(bucket: LeakBucket)

    @Query("SELECT * FROM leak_buckets")
    suspend fun getAllBuckets(): List<LeakBucket>

    @Query("SELECT * FROM leak_buckets WHERE bucketName = :name")
    suspend fun getBucketByName(name: String): LeakBucket?
}
