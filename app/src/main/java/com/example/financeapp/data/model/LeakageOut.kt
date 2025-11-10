// LeakageOut.kt
package com.example.financeapp.data.model // This is your correct package path

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.math.BigDecimal
import java.time.LocalDate 

@Serializable
data class LeakageOut(
    val total_reclaimable_salary: BigDecimal, 
    val if_leak_fixed_new_salary: BigDecimal, 
    val net_monthly_income: BigDecimal,
    val reporting_period: LocalDate,
    
    // References LeakageBucketNetwork.kt
    val leakage_buckets: List<LeakageBucketNetwork>, 

    val e_family_size: BigDecimal,
    val essential_target: BigDecimal
)
