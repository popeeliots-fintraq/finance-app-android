package com.example.financeapp.workers // Create this 'workers' package

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.data.local.SmsData // Assuming this is correct
import com.example.financeapp.data.local.SmsDatabase // Assuming this is correct
import com.example.financeapp.data.repo.CategorizationRepository // You need the repo

class SmsProcessingWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    // Define keys for input data - These resolve the 'unresolved reference' error in the receiver!
    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        private const val TAG = "SmsWorker"
        // List of keywords for transaction filtering (moved from the previous suggestion)
        private val transactionKeywords = listOf("debited", "credit", "rs", "inr", "transferred", "paid")
    }

    private val repository = CategorizationRepository()

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
        
        // 2. LOCAL DB I/O (Moved from old receiver)
        try {
            // Note: If you were using a custom encryption library, ensure the setup is here
            val db = SmsDatabase.getDatabase(applicationContext) 
            val smsData = SmsData(sender = sender, messageBody = messageBody, timestamp = timestamp)
            db.smsDao().insert(smsData)
            Log.d(TAG, "Saved SMS to encrypted database.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save SMS to DB: ${e.localizedMessage}")
        }

        // 3. API CALL (Moved from old receiver)
        try {
            val amount = parseAmountFromSms(messageBody) // Use the parser
            
            val result = repository.getCategorizedSpend(messageBody) // Use the repository
            
            result.onSuccess { response ->
                Log.d(TAG, "API Success! Category: ${response.category}, Confidence: ${response.confidenceScore}")
                // TODO: Update the local database entry with the categorization result
            }.onFailure { error ->
                Log.e(TAG, "API Failure: ${error.message}")
                return Result.retry() // Retry API call later if it fails
            }
            
            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "API CRASHED: ${e.localizedMessage}")
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
