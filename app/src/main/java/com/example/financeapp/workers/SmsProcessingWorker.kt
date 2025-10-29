package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
// Use your actual package names for DTOs
import com.example.financeapp.data.model.RawSmsIn // DTO for network request
import com.example.financeapp.data.model.RawSmsOut // DTO for network response (Assuming you have this)
import com.example.financeapp.data.model.LocalSmsRecord // 🚨 CRITICAL FIX: Local DB model import
import com.example.financeapp.ApiService
import com.example.financeapp.data.remote.RetrofitClient
import com.example.financeapp.SmsDatabase // Keep local DB save as audit trail

// NOTE: We assume these are passed via inputData from where the worker is launched
const val KEY_USER_ID = "user_id"
const val KEY_AUTH_TOKEN = "auth_token"

class SmsProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    // Removed the direct apiService assignment to ensure lazy loading if needed, but your approach is fine.
    // private val apiService = RetrofitClient.apiService 

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        private const val TAG = "SmsWorker"

        // No longer needed: private val transactionKeywords = ...
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val messageBody = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, 0L)
        val userId = inputData.getInt(KEY_USER_ID, -1)
        val authToken = inputData.getString(KEY_AUTH_TOKEN)

        if (userId == -1 || authToken.isNullOrEmpty()) {
             Log.e(TAG, "Missing User ID or Auth Token. Cannot ingest.")
             return Result.failure()
        }

        Log.d(TAG, "Worker started for SMS from $sender. Initiating Frictionless Ingestion.")
        
        // 1. Save raw SMS to local DB (Retained for local audit/backup)
        val db = SmsDatabase.getDatabase(applicationContext)
        // 🚨 CRITICAL FIX: Renamed local model to LocalSmsRecord (or your actual local model name)
        val localSmsRecord = LocalSmsRecord(
            sender = sender, 
            messageBody = messageBody, 
            timestamp = timestamp, 
            isProcessed = false
        )
        try {
            // We use the local ID as a correlation ID, but the backend generates its own
            val generatedId = db.smsDao().insert(localSmsRecord)
            Log.d(TAG, "Saved local audit SMS with ID: $generatedId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save initial SMS to local audit DB: ${e.localizedMessage}")
            // Continue execution to send to backend even if local save fails
        }

        // 2. Call the V2 Ingestion API
        return try {
            val apiService = RetrofitClient.apiService // Fetch service instance here
            
            // 🚨 CRITICAL FIX: Ensure the RawSmsIn constructor uses DTO names (usually camelCase, but matching the DTO is key)
            val requestBody = RawSmsIn(
                userId = userId, // Assuming RawSmsIn DTO uses camelCase for Kotlin
                rawText = messageBody,
                sourceType = "ANDROID_SMS_LISTENER",
                localTimestamp = timestamp 
            )

            // NOTE: Using token = "Bearer $authToken" requires the ApiService method to accept a @Header parameter
            val response = apiService.ingestRawSms(
                token = "Bearer $authToken",
                rawSmsData = requestBody
            )
            
            if (response.isSuccessful && response.body() != null) {
                // Assuming RawSmsOut has an 'id' field
                val rawSmsOut = response.body()!! 
                Log.d(TAG, "Backend Ingestion successful. Backend Raw ID: ${rawSmsOut.id}")
                
                // Optional: Update local DB to reflect successful ingestion (by local ID)
                // db.smsDao().updateIngestionStatus(generatedId, true) 
                
                Result.success()
            } else {
                Log.e(TAG, "API Failure: Code ${response.code()}. Message: ${response.errorBody()?.string()}")
                Result.retry() // Retry on non-success HTTP status
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network/API Error during Ingestion: ${e.localizedMessage}")
            Result.retry() // Retry on connection failure
        }
    }
    
    // Removed isTransactionMessage and parseAmountFromSms as categorization is now backend-only.
}
