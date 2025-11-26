package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.data.dao.RawTransactionDao
import com.example.financeapp.data.local.entity.RawTransactionEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * A background worker responsible for ingesting raw SMS data received from the Broadcast Receiver
 * into the local encrypted database.
 */
@HiltWorker
class RawSmsIngestionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rawTransactionDao: RawTransactionDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(SmsWorkerConstants.LOG_TAG, "RawSmsIngestionWorker started.") 

        // 1. Retrieve data passed from the Broadcast Receiver
        val sender = inputData.getString(SmsWorkerConstants.KEY_SMS_SENDER)
        val body = inputData.getString(SmsWorkerConstants.KEY_SMS_BODY)
        val timestamp = inputData.getLong(SmsWorkerConstants.KEY_SMS_TIMESTAMP, 0L)

        if (sender.isNullOrBlank() || body.isNullOrBlank() || timestamp == 0L) {
            Log.e(SmsWorkerConstants.LOG_TAG, "Invalid input data received.") 
            return Result.failure()
        }

        try {
            // 2. Create the Entity - Note the use of 'rawText' and 'userId' to match the entity
            val entity = RawTransactionEntity(
                id = 0L, 
                userId = "default_user_id", // Placeholder for user ID
                sender = sender,
                rawText = body, 
                ingestionStatus = "PENDING", 
                localTimestamp = System.currentTimeMillis(),
                smsTimestamp = timestamp, 
                uniqueSmsId = java.util.UUID.randomUUID().toString()
            )

            // 3. Save to the local encrypted database
            val rowId = rawTransactionDao.insertRawTransaction(entity)
            
            if (rowId > 0) {
                Log.i(SmsWorkerConstants.LOG_TAG, "Raw SMS successfully saved to DB. Row ID: $rowId")
                return Result.success()
            } else {
                Log.e(SmsWorkerConstants.LOG_TAG, "Failed to insert raw SMS into DB.")
                return Result.retry()
            }
        } catch (e: Exception) {
            Log.e(SmsWorkerConstants.LOG_TAG, "Database error during SMS ingestion: ${e.message}", e)
            return Result.retry()
        }
    }
}
