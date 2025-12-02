package com.example.financeapp.data.dao

import androidx.room.*
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
