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

    // The backend does not send 'confidence_score' in this new schema.
    // It sends 'leak_potential' which is core to Fin-Traq's vision.
    @SerializedName("leak_potential")
    val leakPotential: Double
)
