package com.example.financeapp.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * DTO for the synchronous response from the IngestionService.
 * Confirms that the raw message was saved and handed off for async processing.
 */
@Serializable // Mandatory for Kotlinx Serialization
data class RawSmsOut(
    // The ID of the RawTransaction entry created in the backend DB
    // Using @SerialName as the equivalent to Gson's @SerializedName
    @SerialName("id")
    val id: Int,

    @SerialName("message")
    val message: String? = "Raw message ingested successfully for async processing."
)
