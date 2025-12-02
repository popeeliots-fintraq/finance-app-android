package com.example.financeapp.data.dao

import androidx.room.*
import com.example.financeapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isLeak = 1 ORDER BY timestampMillis DESC")
    fun getLeakTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE category = :category AND type = :type")
    suspend fun getAmountByCategoryAndType(category: String, type: String): Double?

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
