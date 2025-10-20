package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
    // Backend expects 'sms_text', but we want to call it 'description' in Kotlin
    @SerializedName("sms_text") 
    val description: String
)
