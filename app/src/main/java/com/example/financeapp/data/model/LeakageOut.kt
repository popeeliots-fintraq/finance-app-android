// LeakageOut.kt
package com.example.financeapp.data.model 

import kotlinx.serialization.Serializable
// NOTE: java.math.BigDecimal is no longer needed
// NOTE: java.time.LocalDate is no longer needed

@Serializable
data class LeakageOut(
    // CRITICAL FIX: Change BigDecimal to String
    val total_reclaimable_salary: String, 
    // CRITICAL FIX: Change BigDecimal to String
    val if_leak_fixed_new_salary: String, 
    // CRITICAL FIX: Change BigDecimal to String
    val net_monthly_income: String, 
    // CRITICAL FIX: Change LocalDate to String (e.g., "2025-11-01")
    val reporting_period: String, 
    
    val leakage_buckets: List<LeakageBucketNetwork>, 

    // CRITICAL FIX: Change BigDecimal to String
    val e_family_size: String,
    // CRITICAL FIX: Change BigDecimal to String
    val essential_target: String
)
