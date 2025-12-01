package com.example.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leak_buckets")
data class LeakBucket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // E.g., 'Subscriptions', 'Dining Out', 'Impulse Buys'
    val bucketName: String, 
    // The total amount attributed to this leak type
    val currentLeakageAmount: Double = 0.0,
    // The maximum you'd allow for this bucket (optional goal setting)
    val targetLimit: Double? = null, 
    // Human-readable description of why this is a leak
    val description: String 
)
