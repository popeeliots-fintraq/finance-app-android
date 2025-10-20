package com.example.financeapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_table")
data class SmsData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val messageBody: String,
    val timestamp: Long,
    val category: String? = null,
    
    // ✅ Keep ONLY the CAMELCASE property for the local DB entity
    val leakBucket: String? = null, 
    
    // Keep the CAMELCASE property
    val confidenceScore: Double? = null, 
    
    // The previous snake_case property 'leak_bucket' is REMOVED
    
    val isProcessed: Boolean = false
)
