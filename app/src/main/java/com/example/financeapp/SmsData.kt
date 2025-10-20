package com.example.financeapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo // Needed if you want to use snake_case property names

@Entity(tableName = "sms_table")
data class SmsData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    // Note: The message body is stored locally in camelCase but sent to the API via @SerializedName annotation in RawTransactionIn.kt
    val messageBody: String, 
    val timestamp: Long,
    val category: String? = null,
    
    // 💡 FIX 1: The worker uses snake_case, so change the property name to match OR change the worker. 
    // Changing the worker is safer here since it's cleaner to keep the DTO/Entity aligned to the API snake_case for new fields.
    // However, since you're already using 'confidenceScore', let's fix the worker instead for consistency:
    // **ASSUMPTION:** We keep 'confidenceScore' here and fix the worker in the next step.
    val confidenceScore: Double? = null,
    
    // 💡 FIX 2: Add the LEAK BUCKET field for Fin-Traq's core logic. 
    // We must use 'leak_bucket' here to match the worker's call (smsData.copy(leak_bucket = ...))
    val leak_bucket: String? = null, 
    
    val isProcessed: Boolean = false
)
