package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financeapp.data.local.entity.RawTransactionEntity

/**
 * Data Access Object for managing RawTransactionEntity records.
 * These are the initial records of raw SMS data ingested from the device.
 */
@Dao
interface RawTransactionDao {

    /**
     * Inserts a raw transaction record into the database.
     * Used immediately after an SMS is captured by the Broadcast Receiver.
     * @return The row ID of the inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawTransaction(entity: RawTransactionEntity): Long

    /**
     * Retrieves all raw SMS records that have not yet been successfully sent to the backend.
     * This is crucial for the worker to retry failed ingestions.
     */
    @Query("SELECT * FROM raw_transactions WHERE ingestionStatus = 'PENDING' OR ingestionStatus = 'FAILED' ORDER BY localTimestamp ASC")
    suspend fun getUnsentRawTransactions(): List<RawTransactionEntity>

    /**
     * Updates the ingestion status of a specific raw transaction after a worker attempt.
     */
    @Query("""
        UPDATE raw_transactions 
        SET ingestionStatus = :newStatus
        WHERE id = :id
    """)
    suspend fun updateIngestionStatus(id: Long, newStatus: String)
    
    /**
     * Marks a transaction as successfully SENT and clears it if the backend processing is complete.
     * Note: For Fin-Traq V2, we might keep SENT records as an audit trail.
     */
    @Query("""
        UPDATE raw_transactions 
        SET ingestionStatus = 'SENT'
        WHERE id = :id
    """)
    suspend fun markAsSent(id: Long)
    
    // Optional: Get by ID for debugging
    @Query("SELECT * FROM raw_transactions WHERE id = :id")
    suspend fun getRawTransactionById(id: Long): RawTransactionEntity?
}
