package com.example.financeapp.data.model

import com.google.gson.annotations.SerializedName

// RENAME: CategorizationResponse.kt -> CategorizedTransactionOut.kt
data class CategorizedTransactionOut(
    // Core Transaction Data
    @SerializedName("transaction_id")
    val transactionId: String,

    @SerializedName("user_id") // New field from backend schema
    val userId: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("merchant") // New field from backend schema
    val merchant: String,

    @SerializedName("category")
    val category: String,

    // FIX 1: Map the backend's 'leak_bucket' to a matching Kotlin property.
    // The worker is explicitly trying to read 'categorizedData.leak_bucket'.
    @SerializedName("leak_bucket") 
    val leakBucket: String, // Note: Must be String to match the DB/Worker update call

    // FIX 2: Map the backend's 'confidence_score' to a matching Kotlin property.
    // The worker is explicitly trying to read 'categorizedData.confidence_score'.
    @SerializedName("confidence_score")
    val confidenceScore: Double,

    // NEW FIELD: Keep the core Fin-Traq V2 field, even if the worker doesn't use it yet.
    @SerializedName("leak_potential")
    val leakPotential: Double? = null // Made nullable/defaulted as it might not be in every response yet
)
