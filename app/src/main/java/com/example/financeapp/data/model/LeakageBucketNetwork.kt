// LeakageBucketNetwork.kt
package com.example.financeapp.data.model 

import kotlinx.serialization.Serializable
// NOTE: java.math.BigDecimal is no longer needed
// NOTE: kotlinx.serialization.Contextual is no longer needed

@Serializable
data class LeakageBucketNetwork(
    val category: String,
    // CRITICAL FIX: Change BigDecimal to String
    val actual_spend: String, 
    // CRITICAL FIX: Change BigDecimal to String
    val baseline_threshold: String, 
    // CRITICAL FIX: Change BigDecimal to String
    val leak_amount: String, 
    val insight_card_text: String 
)
