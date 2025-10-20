package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

// RENAME: TransactionRequest.kt -> RawTransactionIn.kt
data class RawTransactionIn(
    // Matches 'transaction_text' in the backend schema
    @SerializedName("transaction_text") 
    val transactionText: String,

    // Matches 'sms_date_time' in the backend schema
    @SerializedName("sms_date_time")
    val smsDateTime: String,

    // Matches 'bank_identifier' in the backend schema
    @SerializedName("bank_identifier")
    val bankIdentifier: String,

    // Matches 'pre_extracted_amount' in the backend schema
    @SerializedName("pre_extracted_amount")
    val preExtractedAmount: String? = null // Optional
)
