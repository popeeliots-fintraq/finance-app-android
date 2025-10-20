package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.SmsData
import com.example.financeapp.SmsDatabase
// DELETE: import com.example.financeapp.data.repo.CategorizationRepository // <--- THIS LINE IS REMOVED

import com.example.financeapp.data.model.RawTransactionIn
import com.example.financeapp.data.remote.ApiService
import com.example.financeapp.data.remote.RetrofitClient // Assuming you have a RetrofitClient

class SmsProcessingWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    // Initialize the API Service instance
    private val apiService = RetrofitClient.retrofit.create(ApiService::class.java)

    // Define keys for input data
    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        private const val TAG = "SmsWorker"
        // List of keywords for transaction filtering
        private val transactionKeywords = listOf("debited", "credit", "rs", "inr", "transferred", "paid")
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val messageBody = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, 0L)
        
        Log.d(TAG, "Worker started for SMS from $sender.")

        // 1. FILTER MESSAGE 
        if (!isTransactionMessage(messageBody)) {
            Log.d(TAG, "Ignoring non-transaction SMS.")
            return Result.success() // Success, no further action needed
        }
        
        // 2. LOCAL DB I/O (Initial Insert)
        val db = SmsDatabase.getDatabase(applicationContext) // Initialize DB access
        
        // Initialize SmsData object (id=0 will trigger autoGenerate)
        var smsData = SmsData(sender = sender, messageBody = messageBody, timestamp = timestamp)

        try {
            // 💡 1. Capture the generated ID from the insert operation (Returns Long)
            val generatedId = db.smsDao().insert(smsData)

            // 💡 2. Update the smsData object with the generated ID for subsequent updates
            smsData = smsData.copy(id = generatedId.toInt()) 
            Log.d(TAG, "Saved SMS with generated ID: ${smsData.id}")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save initial SMS to DB: ${e.localizedMessage}")
            // Continue, as we still want to try the API call
        }

        // 3. API CALL & DB UPDATE (Final step of the worker)
        try {
            val amount = parseAmountFromSms(messageBody)

            // Prepare the RawTransactionIn request body
            val requestBody = RawTransactionIn(
                id = smsData.id,
                messagebody = messageBody,
                sender = sender,
                timestamp = timestamp,
                extractedAmount = amount
            )

            // Execute the network call
            val response = apiService.ingestRawTransaction(requestBody)

            if (response.isSuccessful && response.body() != null) {
                val categorizedData = response.body()!!
                Log.d(TAG, "API Success! Category: ${categorizedData.category}, Leak Bucket: ${categorizedData.leak_bucket}")

                // 💡 3. Update the local DB entry with categorization and leak results
                val updatedSmsData = smsData.copy(
                    category = categorizedData.category,
                    leakBucket = categorizedData.leak_bucket,
                    confidenceScore = categorizedData.confidence_score,
                    isProcessed = true
                )
                db.smsDao().update(updatedSmsData)
                Log.d(TAG, "Updated DB for SMS ID: ${updatedSmsData.id} with categorization.")
                
                return Result.success()
            } else {
                Log.e(TAG, "API Failure: Response code ${response.code()}, Body: ${response.errorBody()?.string()}")
                return Result.retry()
            }

        } catch (e: Exception) {
            Log.e(TAG, "API/Network Error: ${e.localizedMessage}")
            return Result.retry()
        }
    }
    
    // Helper function for transaction filtering
    private fun isTransactionMessage(body: String): Boolean {
        return transactionKeywords.any { keyword -> body.toLowerCase().contains(keyword) }
    }
    
    // The robust amount parser you created
    private fun parseAmountFromSms(message: String): Double {
        val patterns = listOf(
            """(?i)(?:rs|inr)[\s.:]*([\d,]+\.?\d*)""".toRegex(),
            """(?i)(?:debited|credited|spent|received)[\s:]*([\d,]+\.?\d*)""".toRegex(),
            """(?i)upi\s*(?:payment|txn|transfer)[\s:]*([\d,]+\.?\d*)""".toRegex(),
            """(?i)a/c\s*x\d+[:\s]+(?:rs|inr)[\s]*([\d,]+\.?\d*)""".toRegex()
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val amountStr = match.groups[1]?.value?.replace(",", "")
                if (amountStr != null) {
                    return amountStr.toDoubleOrNull() ?: 0.0
                }
            }
        }
        return 0.0
    }
}
