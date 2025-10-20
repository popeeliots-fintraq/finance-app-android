package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

data class CategorizationResponse(
    // Core Transaction Data
    @SerializedName("transaction_id")
    val transactionId: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    // AI/Model Confidence Score (Your requested field)
    @SerializedName("confidence_score")
    val confidenceScore: Double
)
