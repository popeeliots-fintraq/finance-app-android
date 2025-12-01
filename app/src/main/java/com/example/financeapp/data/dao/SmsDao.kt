package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.local.entity.SmsEntity // ⬅️ CRITICAL: Changed import to SmsEntity

/**
 * Data Access Object for SmsEntity management.
 */
@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // ⬅️ CRITICAL: Using SmsEntity here
    suspend fun insert(record: SmsEntity): Long 

    // ⬅️ CRITICAL: Using SmsEntity in queries
    @Query("SELECT * FROM sms_entity WHERE localId = :localId")
    suspend fun getLocalSmsRecordById(localId: Long): SmsEntity?

    @Query("SELECT * FROM sms_entity ORDER BY timestamp DESC")
    fun getAllRecords(): kotlinx.coroutines.flow.Flow<List<SmsEntity>>

    // Method used by SmsProcessingWorker
    suspend fun updateIngestionStatus(localId: Long, processed: Boolean, backendId: String?) {
        updateRecordStatus(localId, processed, backendId)
    }

    // This query is now pointing to the correct table name (default Room naming)
    @Query("UPDATE sms_entity SET processed = :processed, backendRefId = :backendId WHERE localId = :localId")
    suspend fun updateRecordStatus(localId: Long, processed: Boolean, backendId: String?)

    @Update
    // ⬅️ CRITICAL: Using SmsEntity here
    suspend fun updateRecord(record: SmsEntity)
}
