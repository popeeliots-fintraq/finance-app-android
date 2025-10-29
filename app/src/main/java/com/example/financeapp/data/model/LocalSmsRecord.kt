// app/src/main/java/com/example/financeapp/data/model/LocalSmsRecord.kt

package com.example.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_audit_log")
data class LocalSmsRecord(
    // Local primary key for Room DB
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val sender: String,
    val messageBody: String,
    val timestamp: Long, // Epoch time for when the SMS was received
    val isProcessed: Boolean = false, // Flag to track if the backend ingestion succeeded
    val backendRawId: String? = null // Optional ID returned by the backend after successful ingestion
)
