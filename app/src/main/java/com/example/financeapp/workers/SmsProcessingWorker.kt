package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.financeapp.api.ApiService
import com.example.financeapp.data.dao.SmsDao
import com.example.financeapp.data.model.RawSmsIn
import com.example.financeapp.data.model.RawSmsOut
import com.example.financeapp.data.model.LocalSmsRecord

/**
 * Worker responsible for sending raw SMS data to the backend for ingestion.
 * It uses Hilt to inject the ApiService and the SmsDao.
 */
@HiltWorker
class SmsProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val smsDao: SmsDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SmsProcessingWorker"
        const val INPUT_DATA_KEY_SMS_ID = "sms_id"
        private const val SOURCE_TYPE = "ANDROID_SMS_LISTENER"
    }

    override suspend fun doWork(): Result {
        val smsId = inputData.getLong(INPUT_DATA_KEY_SMS_ID, -1L)
        if (smsId == -1L) {
            Log.e(TAG, "Missing SMS ID in input data.")
            return Result.failure()
        }
        
        // 1. Fetch raw SMS data from the local database
        val localSmsRecord = smsDao.getLocalSmsRecordById(smsId) 
        if (localSmsRecord == null) {
            Log.e(TAG, "Local SMS audit record not found for ID: $smsId")
            return Result.failure()
        }

        // 2. Prepare DTO for network call
        val rawSmsIn = RawSmsIn(
            userId = localSmsRecord.userId, 
            rawText = localSmsRecord.messageBody, 
            sourceType = SOURCE_TYPE, 
            localTimestamp = localSmsRecord.timestamp 
        )

        try {
            // 3. Call the API to ingest the raw SMS
            // TODO: Replace DUMMY_TOKEN with actual authenticated token later
            val token = "Bearer DUMMY_TOKEN" 
            val response = apiService.ingestRawSms(token, rawSmsIn) 

            if (response.isSuccessful) {
                // Assuming RawSmsOut is the type of response.body()
                val backendRefId = (response.body() as? RawSmsOut)?.id?.toString()
                
                // 4. Update ingestion status in the local database
                smsDao.updateIngestionStatus(
                    localId = smsId, 
                    processed = true, 
                    backendId = backendRefId
                )
                Log.d(TAG, "SMS ID $smsId successfully ingested and status updated.")
                return Result.success()
            } else {
                Log.w(TAG, "API Ingestion failed for SMS ID $smsId. Code: ${response.code()}, Body: ${response.errorBody()?.string()}")
                // Retry for server errors (5xx)
                return if (response.code() in 500..599) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception during SMS ingestion for ID $smsId: ${e.message}", e)
            // Retry on network failure
            return Result.retry()
        }
    }
}
