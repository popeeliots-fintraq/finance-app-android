package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * DTO for the synchronous response from the IngestionService. 
 * Confirms that the raw message was saved and handed off for async processing.
 */
data class RawSmsOut(
    // The ID of the RawTransaction entry created in the backend DB
    @SerializedName("id") 
    val id: Int, 

    @SerializedName("message")
    val message: String? = "Raw message ingested successfully for async processing."
)
