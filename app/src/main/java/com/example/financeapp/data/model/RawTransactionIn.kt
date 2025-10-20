package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

// This DTO must match the data being sent from the SmsProcessingWorker
data class RawTransactionIn(
    // ID is used internally in the worker and should be passed to the backend for correlation
    val id: Int, 
    
    // Maps to 'message_body' in the backend Pydantic schema
    @SerializedName("message_body")
    val messageBody: String, // Use messageBody in Kotlin for camelCase

    // Maps to 'sender' (bank identifier)
    val sender: String, 

    // Maps to 'timestamp'
    val timestamp: Long, 
    
    // Maps to 'extracted_amount' in the backend schema
    @SerializedName("extracted_amount")
    val extractedAmount: Double // Use extractedAmount in Kotlin for camelCase
)
