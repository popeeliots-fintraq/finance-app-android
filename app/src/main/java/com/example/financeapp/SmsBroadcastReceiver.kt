package com.example.financeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.financeapp.workers.RawSmsIngestionWorker // <-- Use the new worker class
import com.example.financeapp.workers.SmsWorkerConstants

/**
 * BroadcastReceiver to intercept incoming SMS messages and queue a WorkManager task
 * to securely store the raw data locally.
 *
 * NOTE: This requires the android.permission.RECEIVE_SMS permission in the manifest.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            // We assume the messages array is non-empty upon receipt
            if (messages.isNotEmpty()) {
                val smsMessage = messages[0] // Typically, only the first message is enough
                val sender = smsMessage.displayOriginatingAddress
                val body = smsMessage.displayMessageBody
                val timestamp = smsMessage.timestampMillis
                
                // Log the reception (no toasts allowed)
                Log.i(SmsWorkerConstants.LOG_TAG, "SMS received from $sender at $timestamp")
                Log.d(SmsWorkerConstants.LOG_TAG, "Body: $body")

                // Prepare input data for the Worker
                val inputData = Data.Builder()
                    .putString(SmsWorkerConstants.KEY_SMS_SENDER, sender)
                    .putString(SmsWorkerConstants.KEY_SMS_BODY, body)
                    .putLong(SmsWorkerConstants.KEY_SMS_TIMESTAMP, timestamp)
                    .build()

                // Create a work request for the new worker
                val workRequest = OneTimeWorkRequestBuilder<RawSmsIngestionWorker>() // <-- Use RawSmsIngestionWorker
                    .setInputData(inputData)
                    .addTag(SmsWorkerConstants.TAG_SMS_INGESTION_WORKER)
                    .build()
                
                // Enqueue the work request
                WorkManager.getInstance(context).enqueue(workRequest)
                Log.d(SmsWorkerConstants.LOG_TAG, "RawSmsIngestionWorker enqueued.")
            }
        }
    }
}
