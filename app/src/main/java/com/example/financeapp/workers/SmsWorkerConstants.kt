package com.example.financeapp.workers

/**
 * Constants used across SMS handling components (Receiver, Worker, and possibly ViewModel/Repo).
 */
object SmsWorkerConstants {
    // Logging Tag
    const val LOG_TAG = "SmsIngestion"

    // Worker Input Keys
    const val KEY_SMS_SENDER = "sms_sender"
    const val KEY_SMS_BODY = "sms_body"
    const val KEY_SMS_TIMESTAMP = "sms_timestamp_ms"

    // Worker Tags
    const val TAG_SMS_INGESTION_WORKER = "sms_ingestion_work"
}
