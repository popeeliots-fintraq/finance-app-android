package com.example.financeapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Removed: import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing the full response from the V2 Leakage View API.
 */
@JsonClass(generateAdapter = true)
data class LeakageOut(
    // Converted to camelCase and added @Json for snake_case API compatibility
    @Json(name = "total_reclaimable_salary")
    val totalReclaimableSalary: String,

    @Json(name = "if_leak_fixed_new_salary")
    val ifLeakFixedNewSalary: String,

    @Json(name = "net_monthly_income")
    val netMonthlyIncome: String,

    @Json(name = "reporting_period")
    val reportingPeriod: String,

    @Json(name = "leakage_buckets")
    val leakageBuckets: List<LeakageBucketNetwork>,

    @Json(name = "e_family_size")
    val eFamilySize: String,

    @Json(name = "essential_target")
    val essentialTarget: String
)
