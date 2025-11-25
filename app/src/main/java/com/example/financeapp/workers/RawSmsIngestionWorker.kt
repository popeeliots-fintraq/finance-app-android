package com.example.financeapp.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * The initial worker triggered by the SMS Broadcast Receiver.
 * Its job is to enqueue the specialized RawSmsIngestionWorker to handle the API call
 * in a robust manner.
 */
class SmsProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val rawText = inputData.getString(KEY_RAW_SMS_TEXT)
        val timestamp = inputData.getLong(KEY_SMS_TIMESTAMP, 0L)
        val userId = inputData.getString(KEY_USER_ID)

        if (rawText.isNullOrEmpty() || timestamp == 0L || userId.isNullOrEmpty()) {
            return Result.failure()
        }

        // Build the request for the ingestion worker
        val ingestionWorkRequest = OneTimeWorkRequestBuilder<RawSmsIngestionWorker>()
            .setInputData(
                workDataOf(
                    KEY_RAW_SMS_TEXT to rawText,
                    KEY_SMS_TIMESTAMP to timestamp,
                    KEY_USER_ID to userId
                )
            )
            .addTag("raw-sms-ingestion")
            .build()

        // Enqueue the ingestion worker, replacing any existing queued work with the same name
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "raw-sms-ingestion-unique",
            ExistingWorkPolicy.REPLACE,
            ingestionWorkRequest
        )

        return Result.success()
    }
}
