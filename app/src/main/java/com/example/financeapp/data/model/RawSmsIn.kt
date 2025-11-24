package com.example.financeapp.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName // Replaces Gson's @SerializedName

/**
 * Data Transfer Object (DTO) for sending raw SMS content to the backend.
 * This DTO is aligned with the backend's RawTransaction Pydantic schema and uses Kotlinx Serialization.
 */
@Serializable // Mandatory for Kotlinx Serialization
data class RawSmsIn(
    // Auth is handled via Header, but include user_id in the body for audit/service layer
    @SerialName("user_id")
    val userId: Int,
    
    @SerialName("raw_text")
    val rawText: String, 

    @SerialName("source_type")
    val sourceType: String, // e.g., "ANDROID_SMS_LISTENER"

    // Use a Long to capture the local timestamp before sending
    @SerialName("local_timestamp")
    val localTimestamp: Long
)

/**
 * Data Transfer Object (DTO) for the synchronous response from the IngestionService. 
 * Confirms that the raw message was saved and handed off for async processing.
 */
@Serializable // Mandatory for Kotlinx Serialization
data class RawSmsOut(
    // The ID of the RawTransaction entry created in the backend DB
    @SerialName("id") 
    val id: Int, 

    @SerialName("message")
    val message: String? = "Raw message ingested successfully for async processing."
)
