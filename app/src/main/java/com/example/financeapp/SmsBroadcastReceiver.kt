package com.example.financeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (smsMessage in messages) {
                val messageBody = smsMessage.messageBody
                val sender = smsMessage.originatingAddress
                val timestamp = smsMessage.timestampMillis
                
                Log.d("SMS_RECEIVER", "SMS from: $sender, Body: $messageBody")

                if (context != null) {
                    val db = SmsDatabase.getDatabase(context)
                    val smsData = SmsData(sender = sender ?: "Unknown", messageBody = messageBody, timestamp = timestamp)

                    CoroutineScope(Dispatchers.IO).launch {
                        db.smsDao().insert(smsData)
                        Log.d("DB_SAVE", "Saved SMS to encrypted database.")

                        // 2️⃣ Send SMS to backend for categorization
                        try {
                            // TODO: Replace with your logged-in user ID
                            val userId = "USER_ID_HERE"

                            // Parse today's date
                            val today = java.time.LocalDate.now().toString()

                            // Call the backend API
                            handleIncomingSms(
                                smsText = messageBody,
                                amount = parseAmountFromSms(messageBody),
                                userId = userId
                            )

                            Log.d("API_CALL", "Sent SMS to backend successfully.")
                        } catch (e: Exception) {
                            Log.e("API_CALL", "Failed to send SMS to backend: ${e.localizedMessage}")
                    }
                }
            }
        }
    }
}
// Improved amount parser for Indian bank/UPI/wallet SMS formats
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

        return 0.0 // fallback if no pattern matches
    }
    
    // stubbed function to satisfy compiler
    private fun handleIncomingSms(smsText: String, amount: Double, userId: String) {
        // TODO: implement actual API call with Retrofit/OkHttp
        Log.d("HANDLE_SMS", "Handling SMS for $userId, amount: $amount, text: $smsText")
    }
}

