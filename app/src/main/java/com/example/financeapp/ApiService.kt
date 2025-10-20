package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.SmsData
import com.example.financeapp.SmsDatabase
import com.example.financeapp.ApiService // Assuming ApiService is directly in the main package
// New imports for the API call
import com.example.financeapp.data.model.RawTransactionIn
import com.example.financeapp.data.model.CategorizedTransactionOut
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SmsProcessingWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    // Define keys for input data
    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        private const val TAG = "SmsWorker"
        // List of keywords for transaction filtering
        private val transactionKeywords = listOf("debited", "credit", "rs", "inr", "transferred", "paid")
    }

    // REMOVE OLD: private val repository = CategorizationRepository()
    // ADD NEW: Instantiate the ApiService instance
    // NOTE: This assumes you have a RetrofitClient setup available to get the ApiService instance.
    private val apiService: ApiService = RetrofitClient.apiService // **Adjust 'RetrofitClient' to your actual setup**

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val messageBody = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, 0L)
        
        Log.d(TAG, "Worker started for SMS from $sender.")

        // 1. FILTER MESSAGE 
        if (!isTransactionMessage(messageBody)) {
             Log.d(TAG, "Ignoring non-transaction SMS.")
             return Result.success()
        }
        
        // 2. LOCAL DB I/O (Initial Insert)
        val db = SmsDatabase.getDatabase(applicationContext) 
        var smsData = SmsData(sender = sender, messageBody = messageBody, timestamp = timestamp)

        try {
            val generatedId = db.smsDao().insert(smsData)
            smsData = smsData.copy(id = generatedId.toInt()) 
            Log.d(TAG, "Saved SMS with generated ID: ${smsData.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save initial SMS to DB: ${e.localizedMessage}. Still attempting API call.")
        }

        // --- NEW LOGIC: Construct and Send RawTransactionIn ---

        // 3. CONSTRUCT REQUEST BODY
        // Format timestamp into the ISO string the backend expects (yyyy-MM-dd HH:mm:ss)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("IST") // Use a consistent timezone
        
        val smsDateTimeString = dateFormat.format(Date(timestamp))
        val preExtractedAmount = parseAmountFromSms(messageBody) // Use existing parser

        val requestBody = RawTransactionIn(
            transactionText = messageBody,
            smsDateTime = smsDateTimeString,
            bankIdentifier = sender, // Sender is used as bank_identifier
            preExtractedAmount = if (preExtractedAmount > 0.0) preExtractedAmount.toString() else null
        )

        // 4. API CALL & DB UPDATE
        try {
            Log.d(TAG, "Sending raw transaction to backend: $smsDateTimeString")
            
            // Call the new service method
            val response = apiService.ingestRawTransaction(requestBody)
            
            if (response.isSuccessful && response.body() != null) {
                val categorizedData = response.body()!!
                Log.d(TAG, "API Success! Category: ${categorizedData.category}, Leak: ${categorizedData.leakPotential}")
                
                // Update local DB entry with categorization results
                val updatedSmsData = smsData.copy(
                    category = categorizedData.category,
                    isProcessed = true
                    // NOTE: You may want to update SmsData to store merchant, leakPotential, etc.
                )
                db.smsDao().update(updatedSmsData) // Assumes SmsDao has an update function
                Log.d(TAG, "Updated DB for SMS ID: ${updatedSmsData.id} with categorization.")
                
                return Result.success()
            } else {
                Log.e(TAG, "API call failed with HTTP code: ${response.code()}. Body: ${response.errorBody()?.string()}")
                return Result.retry() 
            }
        } catch (e: HttpException) {
            Log.e(TAG, "API CRASHED (HTTP Exception): ${e.code()}. Message: ${e.message}")
            return Result.retry()
        } catch (e: IOException) {
            Log.e(TAG, "API CRASHED (Network/IO Exception): ${e.localizedMessage}")
            return Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "API CRASHED (Unknown Error): ${e.localizedMessage}")
            return Result.retry()
        }
    }
    
    // Helper function for transaction filtering
    private fun isTransactionMessage(body: String): Boolean {
        return transactionKeywords.any { keyword -> body.toLowerCase(Locale.ROOT).contains(keyword) }
    }
    
    // The robust amount parser
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
