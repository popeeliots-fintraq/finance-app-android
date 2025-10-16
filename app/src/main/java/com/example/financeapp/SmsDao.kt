package com.example.financeapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(smsData: SmsData): Long
    @Update
    suspend fun update(smsData: SmsData)
}
