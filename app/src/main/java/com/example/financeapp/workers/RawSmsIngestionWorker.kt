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
import java.util.UUID

/**
 * A background worker responsible for ingesting raw SMS data received from the Broadcast Receiver
 * into the local encrypted database.
 * This is the first step in the V2 frictionless flow (Data Capturing).
 */
@HiltWorker
class RawSmsIngestionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rawTransactionDao: RawTransactionDao,
    // Add repository dependencies here once defined, for potential API interaction later.
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(SmsWorkerConstants.LOG_TAG, "RawSmsIngestionWorker started.")

        // 1. Retrieve data passed from the Broadcast Receiver
        val sender = inputData.getString(SmsWorkerConstants.KEY_SMS_SENDER)
        val body = inputData.getString(SmsWorkerConstants.KEY_SMS_BODY)
        val timestamp = inputData.getLong(SmsWorkerConstants.KEY_SMS_TIMESTAMP, 0L)

        // Basic validation
        if (sender.isNullOrBlank() || body.isNullOrBlank() || timestamp == 0L) {
            Log.e(SmsWorkerConstants.LOG_TAG, "Invalid input data received.")
            return Result.failure()
        }

        try {
            // 2. Create the Entity
            val entity = RawTransactionEntity(
                id = 0L, // Room auto-generates this ID
                sender = sender,
                smsBody = body,
                ingestionStatus = "PENDING", // Ready to be sent to the backend
                localTimestamp = System.currentTimeMillis(),
                smsTimestamp = timestamp, // Original timestamp from the SMS
                uniqueSmsId = UUID.randomUUID().toString() // Unique ID for deduplication
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
            // Use retry in case of transient database/storage issues
            return Result.retry()
        }
    }
}
