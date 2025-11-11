package com.example.financeapp.data.repository

import com.example.financeapp.data.local.TransactionDao
import com.example.financeapp.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository layer for managing transaction data.
 * This class abstracts the data source (Room database) from the rest of the application.
 *
 * @param transactionDao The Data Access Object for transaction CRUD operations.
 */
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    /**
     * Retrieves all transactions from the local database, ordered by newest first.
     * Used for the main transaction listing/view.
     */
    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    /**
     * Retrieves all transactions that have been flagged as a "Leak".
     * Essential for the Leakage Bucket View, the core of Fin-Traq V2.
     */
    fun getLeakTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getLeakTransactions()
    }

    /**
     * Inserts a list of parsed and (potentially) categorized transactions into the database.
     * This method is called after SMS parsing or backend synchronization.
     */
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>) {
        transactionDao.insertAll(transactions)
    }

    /**
     * Retrieves the total sum spent for a specific category and transaction type (e.g., total debits for "FOOD").
     * Useful for running initial business logic calculations.
     */
    suspend fun getCategoryTotal(category: String, type: String): Double {
        return transactionDao.getAmountByCategoryAndType(category, type) ?: 0.0
    }

    /**
     * Clears all transactions from the database.
     * Should be used sparingly, primarily for data reset or development purposes.
     */
    suspend fun clearAllTransactions() {
        transactionDao.clearAll()
    }

    // NOTE: Future methods will include updateTransaction (to set 'isLeak' flag)
    // and methods for interacting with the BackendRepository for categorization.
}
