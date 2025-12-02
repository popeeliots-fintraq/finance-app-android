package com.example.financeapp.data.dao

import androidx.room.*
import com.example.financeapp.data.local.entity.LeakBucket

@Dao
interface LeakBucketDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bucket: LeakBucket)

    @Update
    suspend fun update(bucket: LeakBucket)

    @Query("SELECT * FROM leak_buckets")
    suspend fun getAllBuckets(): List<LeakBucket>

    @Query("SELECT * FROM leak_buckets WHERE bucketName = :name LIMIT 1")
    suspend fun getBucketByName(name: String): LeakBucket?
}
