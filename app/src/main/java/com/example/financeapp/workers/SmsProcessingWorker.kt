package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.financeapp.api.ApiService 
// CRITICAL FIX: Changed to SmsDao and LocalSmsRecord
import com.example.financeapp.data.dao.SmsDao 
import com.example.financeapp.data.model.RawSmsIn 
import com.example.financeapp.data.model.RawSmsOut 
import com.example.financeapp.data.model.LocalSmsRecord 

/**
 * Worker responsible for sending raw SMS data to the backend for processing and categorization.
 * Uses Hilt for dependency injection (ApiService, SmsDao).
 */
@HiltWorker
class SmsProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService, // Injected dependency
    private val smsDao: SmsDao // CRITICAL FIX: Now injecting SmsDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SmsProcessingWorker"
        const val INPUT_DATA_KEY_SMS_ID = "sms_id"
        // Defined the required source type as per the DTO comment
        private const val SOURCE_TYPE = "ANDROID_SMS_LISTENER" 
    }

    override suspend fun doWork(): Result {
        val smsId = inputData.getLong(INPUT_DATA_KEY_SMS_ID, -1L)
        if (smsId == -1L) {
            Log.e(TAG, "Missing SMS ID in input data.")
            return Result.failure()
        }
        
        // 1. Fetch raw SMS data from the local database
        // CRITICAL FIX: Using the new DAO method and Entity
        val localSmsRecord = smsDao.getLocalSmsRecordById(smsId) 
        if (localSmsRecord == null) {
            Log.e(TAG, "Local SMS audit record not found for ID: $smsId")
            return Result.failure()
        }

        // 2. Prepare DTO for network call using the new structure
        // CRITICAL FIX: Mapping LocalSmsRecord fields to RawSmsIn fields
        val rawSmsIn = RawSmsIn(
            userId = localSmsRecord.userId, 
            rawText = localSmsRecord.messageBody, // Maps messageBody -> rawText
            sourceType = SOURCE_TYPE, 
            localTimestamp = localSmsRecord.timestamp // Maps timestamp -> localTimestamp
        )

        try {
            // 3. Call the API to ingest the raw SMS
            // The API requires an Authorization token, using a placeholder "DUMMY_TOKEN" for now.
            val token = "Bearer DUMMY_TOKEN" 
            val response = apiService.ingestRawSms(token, rawSmsIn) 

            if (response.isSuccessful) {
                // Use the new ID field from RawSmsOut as the backend reference ID
                val backendRefId = response.body()?.id?.toString()
                
                // 4. Update ingestion status in the local database
                // CRITICAL FIX: Using SmsDao's updateIngestionStatus
                smsDao.updateIngestionStatus(
                    localId = smsId, 
                    processed = true, 
                    backendId = backendRefId
                )
                Log.d(TAG, "SMS ID $smsId successfully ingested and status updated.")
                return Result.success()
            } else {
                Log.w(TAG, "API Ingestion failed for SMS ID $smsId. Code: ${response.code()}")
                // Retry for recoverable errors (e.g., 5xx)
                return if (response.code() in 500..599) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception during SMS ingestion for ID $smsId: ${e.message}", e)
            // Retry on network failure
            return Result.retry()
        }
    }
}
