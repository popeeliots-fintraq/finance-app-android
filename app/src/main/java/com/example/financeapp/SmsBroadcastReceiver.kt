package com.example.financeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import com.example.financeapp.workers.RawSmsIngestionWorker
import com.example.financeapp.workers.SmsWorkerConstants // <-- Critical: Imports the constants

/**
 * BroadcastReceiver to intercept incoming SMS messages and delegate processing to a WorkManager task.
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            
            // Get all messages from the intent (usually one per PDU)
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            // Process the first message found
            val smsMessage = messages.firstOrNull() ?: run {
                Log.w(SmsWorkerConstants.LOG_TAG, "No SMS messages found in the intent.")
                return
            }

            val sender = smsMessage.displayOriginatingAddress
            val body = smsMessage.displayMessageBody
            val timestamp = smsMessage.timestampMillis
            
            Log.i(SmsWorkerConstants.LOG_TAG, "SMS received from $sender at $timestamp. Body: $body")

            // Create input data bundle for the worker
            val inputData = Data.Builder()
                .putString(SmsWorkerConstants.KEY_SMS_SENDER, sender)
                .putString(SmsWorkerConstants.KEY_SMS_BODY, body)
                .putLong(SmsWorkerConstants.KEY_SMS_TIMESTAMP, timestamp)
                .build()

            // Define constraints: worker needs internet access (or can run immediately)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                // For a robust app, you might want to consider .setRequiresBatteryNotLow(true)
                .build()

            // Create the Work Request
            val smsWorkRequest = OneTimeWorkRequestBuilder<RawSmsIngestionWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag(SmsWorkerConstants.TAG_SMS_INGESTION_WORKER)
                .build()

            // Enqueue the work request
            WorkManager.getInstance(context).enqueue(smsWorkRequest)
            Log.d(SmsWorkerConstants.LOG_TAG, "RawSmsIngestionWorker enqueued.")
        }
    }
}
