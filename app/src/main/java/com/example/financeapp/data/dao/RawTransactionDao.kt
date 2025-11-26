package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financeapp.data.local.entity.RawTransactionEntity

@Dao
interface RawTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawTransaction(transaction: RawTransactionEntity): Long

    @Query("SELECT * FROM raw_transactions WHERE ingestionStatus = 'PENDING'")
    suspend fun getPendingTransactions(): List<RawTransactionEntity>

    @Query("UPDATE raw_transactions SET ingestionStatus = :status WHERE id = :id")
    suspend fun updateIngestionStatus(id: Long, status: String)
}
