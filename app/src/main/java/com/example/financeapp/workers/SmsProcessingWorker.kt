package com.example.financeapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.SmsData
import com.example.financeapp.SmsDatabase
import com.example.financeapp.data.model.RawTransactionIn
import com.example.financeapp.ApiService
import com.example.financeapp.data.remote.RetrofitClient
import com.example.financeapp.data.model.CategorizedTransactionOut

class SmsProcessingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val apiService = RRetrofitClient.apiService

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
        private const val TAG = "SmsWorker"

        private val transactionKeywords = listOf(
            "debited", "credit", "rs", "inr", "transferred", "paid"
        )
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val messageBody = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, 0L)

        Log.d(TAG, "Worker started for SMS from $sender.")

        // 1. Filter non-transaction messages
        if (!isTransactionMessage(messageBody)) {
            Log.d(TAG, "Ignoring non-transaction SMS.")
            return Result.success()
        }

        // 2. Save raw SMS to DB
        val db = SmsDatabase.getDatabase(applicationContext)
        var smsData = SmsData(sender = sender, messageBody = messageBody, timestamp = timestamp)

        try {
            val generatedId = db.smsDao().insert(smsData)
            smsData = smsData.copy(id = generatedId.toInt())
            Log.d(TAG, "Saved SMS with generated ID: ${smsData.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save initial SMS: ${e.localizedMessage}")
        }

        // 3. Parse amount and call API
        return try {
            val amount = parseAmountFromSms(messageBody)
            val requestBody = RawTransactionIn(
                id = smsData.id,
                messageBody = messageBody,
                sender = sender,
                timestamp = timestamp,
                extractedAmount = amount
            )

            val response = apiService.ingestRawTransaction(requestBody)
            if (response.isSuccessful && response.body() != null) {
                val categorizedData = response.body()!!

                val updatedSmsData = smsData.copy(
                    category = categorizedData.category,
                    leakBucket = categorizedData.leakBucket,
                    confidenceScore = categorizedData.confidenceScore,
                    isProcessed = true
                )

                db.smsDao().update(updatedSmsData)
                Log.d(TAG, "Updated DB for SMS ID: ${updatedSmsData.id}")
                Result.success()
            } else {
                Log.e(TAG, "API Failure: Code ${response.code()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network/API Error: ${e.localizedMessage}")
            Result.retry()
        }
    }

    private fun isTransactionMessage(body: String): Boolean {
        return transactionKeywords.any { keyword ->
            body.lowercase().contains(keyword)
        }
    }

    private fun parseAmountFromSms(message: String): Double {
        val patterns = listOf(
            "(?i)(?:rs|inr)[\\s.:]*([\\d,]+\\.?\\d*)".toRegex(),
            "(?i)(?:debited|credited|spent|received)[\\s:]*([\\d,]+\\.?\\d*)".toRegex(),
            "(?i)upi\\s*(?:payment|txn|transfer)[\\s:]*([\\d,]+\\.?\\d*)".toRegex(),
            "(?i)a/c\\s*x\\d+[:\\s]+(?:rs|inr)[\\s]*([\\d,]+\\.?\\d*)".toRegex()
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val amountString = match.groupValues[1].replace(",", "")
                return amountString.toDoubleOrNull() ?: 0.0
            }
        }
        return 0.0
    }
}
