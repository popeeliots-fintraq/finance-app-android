// LeakageBucketNetwork.kt
package com.example.financeapp.data.model // This is your correct package path

import kotlinx.serialization.Serializable
import java.math.BigDecimal 
import java.time.LocalDate // Required for Date parsing

@Serializable
data class LeakageBucketNetwork(
    val category: String,
    val actual_spend: BigDecimal,
    val baseline_threshold: BigDecimal,
    val leak_amount: BigDecimal,
    val insight_card_text: String 
)
