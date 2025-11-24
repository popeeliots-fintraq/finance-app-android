package com.example.financeapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.model.LocalSmsRecord

/**
 * Data Access Object for LocalSmsRecord management.
 * This replaces the older RawSmsDao.
 */
@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LocalSmsRecord): Long

    @Query("SELECT * FROM local_sms_records WHERE localId = :localId")
    suspend fun getLocalSmsRecordById(localId: Long): LocalSmsRecord?

    @Query("SELECT * FROM local_sms_records ORDER BY timestamp DESC")
    fun getAllRecords(): kotlinx.coroutines.flow.Flow<List<LocalSmsRecord>>

    // Method required by SmsProcessingWorker
    suspend fun updateIngestionStatus(localId: Long, processed: Boolean, backendId: String?) {
        updateRecordStatus(localId, processed, backendId)
    }

    // Internal update method for status change
    @Query("UPDATE local_sms_records SET processed = :processed, backendRefId = :backendId WHERE localId = :localId")
    suspend fun updateRecordStatus(localId: Long, processed: Boolean, backendId: String?)

    @Update
    suspend fun updateRecord(record: LocalSmsRecord)
}
