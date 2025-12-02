package com.example.financeapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
// Removed: import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing the full response from the V2 Leakage View API.
 */
@Serializable
data class LeakageOut(
    val total_reclaimable_salary: String,
    val if_leak_fixed_new_salary: String,
    val net_monthly_income: String,
    val reporting_period: String,

    val leakage_buckets: List<LeakageBucketNetwork>, 

    val e_family_size: String,
    val essential_target: String
)
