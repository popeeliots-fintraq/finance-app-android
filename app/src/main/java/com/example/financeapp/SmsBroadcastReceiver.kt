package com.example.financeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.util.Log

class SmsBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            for (smsMessage in messages) {
                val smsBody = smsMessage.messageBody
                val sender = smsMessage.displayOriginatingAddress
                val timestamp = smsMessage.timestampMillis
                
                Log.d(TAG, "SMS received. Starting WorkManager job.")

                // 1. Create Data payload to pass the sensitive SMS content securely
                val inputData = Data.Builder()
                    .putString(SmsProcessingWorker.KEY_SENDER, sender ?: "Unknown")
                    .putString(SmsProcessingWorker.KEY_BODY, smsBody)
                    .putLong(SmsProcessingWorker.KEY_TIMESTAMP, timestamp)
                    .build()

                // 2. Create the Work Request
                val smsWorkRequest = OneTimeWorkRequestBuilder<SmsProcessingWorker>()
                    .setInputData(inputData)
                    .build()

                // 3. Enqueue the work IMMEDIATELY
                context?.let {
                    WorkManager.getInstance(it).enqueue(smsWorkRequest)
                }
            }
        }
    }
}
