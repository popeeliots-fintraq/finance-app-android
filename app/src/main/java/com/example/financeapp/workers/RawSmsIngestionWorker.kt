package com.example.financeapp.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.api.ApiService
import com.example.financeapp.data.model.RawSmsIn
import com.example.financeapp.data.model.RawSmsOut
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.Response

/**
 * Worker responsible for taking the raw SMS data and sending it to the backend ingestion service.
 * HiltWorker is used to inject dependencies (like ApiService).
 */
@HiltWorker
class RawSmsIngestionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Retrieve data from InputData using centralized constants
        val rawText = inputData.getString(KEY_RAW_SMS_TEXT)
        val timestamp = inputData.getLong(KEY_SMS_TIMESTAMP, 0L)
        val userIdString = inputData.getString(KEY_USER_ID)
        val userId = userIdString?.toIntOrNull()

        if (rawText.isNullOrEmpty() || timestamp == 0L || userId == null) {
            // Log for debugging: missing data
            return Result.failure()
        }

        val smsIn = RawSmsIn(
            userId = userId,
            rawText = rawText,
            sourceType = "ANDROID_SMS_LISTENER",
            localTimestamp = timestamp
        )

        return try {
            val response: Response<RawSmsOut> = apiService.ingestRawSms(smsIn)

            if (response.isSuccessful) {
                // Log for debugging: successful ingestion
                Result.success()
            } else {
                // Log non-successful response error
                Result.retry()
            }
        } catch (e: Exception) {
            // Log network or serialization error
            Result.retry()
        }
    }
}
