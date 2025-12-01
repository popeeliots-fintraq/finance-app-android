package com.example.financeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the TransactionEntity.
 * Provides methods for CRUD operations on financial transactions.
 */
@Dao
interface TransactionDao {
    /**
     * Inserts a single transaction into the database. If it exists, it replaces it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    /**
     * Inserts a list of transactions (used after bulk SMS parsing or backend sync).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    /**
     * Retrieves all transactions, ordered from newest to oldest.
     */
    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Retrieves all transactions classified as 'Leak'. This is essential for the Leakage Bucket View.
     */
    @Query("SELECT * FROM transactions WHERE isLeak = 1 ORDER BY timestampMillis DESC")
    fun getLeakTransactions(): Flow<List<TransactionEntity>>

    /**
     * Retrieves the sum of all transaction amounts for a specific category and type (e.g., total debit spending on 'FOOD').
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE category = :category AND type = :type")
    suspend fun getAmountByCategoryAndType(category: String, type: String): Double?

    /**
     * Clears all transaction data from the table.
     */
    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
