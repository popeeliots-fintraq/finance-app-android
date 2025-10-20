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
    
    // ✅ Keep the CAMELCASE property, which aligns with the updated Worker code.
    val leakBucket: String? = null, 
    
    // Keep the CAMELCASE property to be consistent with standard Kotlin practice.
    val confidenceScore: Double? = null, 
    
    // The 'leak_bucket' property was removed here.
    
    val isProcessed: Boolean = false
)
