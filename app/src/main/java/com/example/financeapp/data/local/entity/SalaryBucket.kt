package com.example.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_buckets")
data class SalaryBucket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // E.g., 'Main Salary', 'Side Gig 1', 'Bonus'
    val sourceName: String, 
    // The fixed/expected amount
    val expectedAmount: Double, 
    // Date the amount is expected/received
    val expectedDate: String, 
    // How frequently the amount is expected (e.g., 'Monthly', 'Quarterly')
    val frequency: String,
    // The actual amount received (can differ from expectedAmount)
    val lastReceivedAmount: Double = 0.0,
    val lastReceivedDate: Long = 0L 
)
