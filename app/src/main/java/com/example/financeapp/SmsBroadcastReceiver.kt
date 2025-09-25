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
                    }
                }
            }
        }
    }
}
