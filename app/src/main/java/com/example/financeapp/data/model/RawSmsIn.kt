package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO for sending raw SMS data to the backend's high-speed IngestionService.
 * This is the V2 DTO for Frictionless Flow (Gap #1).
 */
data class RawSmsIn(
    // 🚨 IMPORTANT: Match the field names expected by the backend's RawTransaction Pydantic schema
    
    // Auth is handled via Header, but include user_id in the body for audit/service layer
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("raw_text")
    val rawText: String, 

    @SerializedName("source_type")
    val sourceType: String, // e.g., "ANDROID_SMS_LISTENER"

    // Use a Long to capture the local timestamp before sending
    @SerializedName("local_timestamp")
    val localTimestamp: Long
)
