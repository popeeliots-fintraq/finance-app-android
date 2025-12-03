package com.example.financeapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import com.example.financeapp.workers.RawSmsIngestionWorker
import com.example.financeapp.workers.SmsWorkerConstants
import com.example.financeapp.workers.RawSmsSyncWorker  // <-- NEW: sync worker

/**
 * BroadcastReceiver to intercept incoming SMS messages and delegate processing to workers.
 * 1. ALWAYS save SMS locally (offline safe)
 * 2. If internet available → trigger sync worker
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val smsMessage = messages.firstOrNull() ?: run {
                Log.w(SmsWorkerConstants.LOG_TAG, "No SMS messages found in the intent.")
                return
            }

            val sender = smsMessage.displayOriginatingAddress
            val body = smsMessage.displayMessageBody
            val timestamp = smsMessage.timestampMillis

            Log.i(
                SmsWorkerConstants.LOG_TAG,
                "SMS received from $sender at $timestamp | Body: $body"
            )

            // Input data for the ingestion layer
            val inputData = Data.Builder()
                .putString(SmsWorkerConstants.KEY_SMS_SENDER, sender)
                .putString(SmsWorkerConstants.KEY_SMS_BODY, body)
                .putLong(SmsWorkerConstants.KEY_SMS_TIMESTAMP, timestamp)
                .build()

            // ---------- FIX #1: NO NETWORK NEEDED TO SAVE SMS LOCALLY ----------
            val localIngestionConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val saveWorkRequest = OneTimeWorkRequestBuilder<RawSmsIngestionWorker>()
                .setConstraints(localIngestionConstraints)
                .setInputData(inputData)
                .addTag(SmsWorkerConstants.TAG_SMS_INGESTION_WORKER)
                .build()

            WorkManager.getInstance(context).enqueue(saveWorkRequest)
            Log.d(SmsWorkerConstants.LOG_TAG, "RawSmsIngestionWorker scheduled (offline safe).")

            // ---------- FIX #2: Trigger sync worker WHEN INTERNET IS AVAILABLE ----------
            val syncConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<RawSmsSyncWorker>()
                .setConstraints(syncConstraints)
                .addTag("RAW_SMS_SYNC")
                .build()

            WorkManager.getInstance(context).enqueue(syncWorkRequest)
            Log.d(SmsWorkerConstants.LOG_TAG, "RawSmsSyncWorker scheduled.")
        }
    }
}
