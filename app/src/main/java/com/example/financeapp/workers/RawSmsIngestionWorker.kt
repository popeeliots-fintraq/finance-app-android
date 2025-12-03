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

@HiltWorker
class RawSmsIngestionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rawTransactionDao: RawTransactionDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RawSmsIngestionWorker", "Worker started.")

        val sender = inputData.getString(SmsWorkerConstants.KEY_SMS_SENDER)
        val body = inputData.getString(SmsWorkerConstants.KEY_SMS_BODY)
        val timestamp = inputData.getLong(SmsWorkerConstants.KEY_SMS_TIMESTAMP, 0L)

        if (sender.isNullOrBlank() || body.isNullOrBlank() || timestamp == 0L) {
            Log.e("RawSmsIngestionWorker", "Invalid input data received.")
            return Result.failure()
        }

        return try {
            val entity = RawTransactionEntity(
                id = 0L,
                userId = "default_user_id",
                sender = sender,
                rawText = body,
                ingestionStatus = "PENDING",
                localTimestamp = System.currentTimeMillis(),
                smsTimestamp = timestamp,
                uniqueSmsId = UUID.randomUUID().toString()
            )

            val rowId = rawTransactionDao.insertRawTransaction(entity)

            if (rowId > 0) {
                Log.i("RawSmsIngestionWorker", "Raw SMS saved. Row ID: $rowId")
                Result.success()
            } else {
                Log.e("RawSmsIngestionWorker", "Failed to insert raw SMS into DB.")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e("RawSmsIngestionWorker", "DB error: ${e.message}", e)
            Result.retry()
        }
    }
}
