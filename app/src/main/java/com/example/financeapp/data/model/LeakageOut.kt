package com.example.financeapp.data.model

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing the full response from the V2 Leakage View API.
 * This object is used by Retrofit/Kotlinx Serialization.
 */
@Serializable
data class LeakageOut(
    // CRITICAL FIX: Change BigDecimal to String (Matches user's version)
    val total_reclaimable_salary: String,
    // CRITICAL FIX: Change BigDecimal to String (Matches user's version)
    val if_leak_fixed_new_salary: String,
    // CRITICAL FIX: Change BigDecimal to String (Matches user's version)
    val net_monthly_income: String,
    // CRITICAL FIX: Change LocalDate to String (e.g., "2025-11-01") (Matches user's version)
    val reporting_period: String,

    val leakage_buckets: List<LeakageBucketNetwork>,

    // CRITICAL FIX: Change BigDecimal to String (Matches user's version)
    val e_family_size: String,
    // CRITICAL FIX: Change BigDecimal to String (Matches user's version)
    val essential_target: String
)

/**
 * Nested DTO for a single leakage bucket item within the LeakageOut response.
 */
@Serializable
data class LeakageBucketNetwork(
    val category: String, // e.g., "Subscription Overlaps", "Uncategorized", "High-Cost Transit"
    val leak_amount: String, // Amount of leakage in this bucket
    val insight_summary: String? = null // Short insight text
)
