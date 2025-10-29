// file:///.../SmsDao.kt

package com.example.financeapp.data.dao // NOTE: Changed package name for organization

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.model.LocalSmsRecord // 🚨 CRITICAL FIX: Import new model

@Dao
interface SmsDao {
    
    // Inserts the raw SMS audit record. Use REPLACE to handle potential retries gracefully.
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Changed to REPLACE from IGNORE for safer audit logging
    suspend fun insert(record: LocalSmsRecord): Long // 🚨 CRITICAL FIX: Update parameter type
    
    // Kept the generic update method for existing Salary/Leak buckets if needed, but the worker uses a specific query for SMS audit.
    // suspend fun update(smsData: SmsData) // REMOVED: Replaced by specific update below if needed
    
    // V2: Function for updating the status after successful backend ingestion
    @Query("UPDATE sms_audit_log SET isProcessed = :processed, backendRawId = :backendId WHERE id = :localId")
    suspend fun updateIngestionStatus(localId: Long, processed: Boolean, backendId: String? = null): Int
    
    // V2: Query to get all unprocessed records for potential background retry
    @Query("SELECT * FROM sms_audit_log WHERE isProcessed = 0")
    suspend fun getAllUnprocessedRecords(): List<LocalSmsRecord>

}
