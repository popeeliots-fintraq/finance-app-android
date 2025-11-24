package com.example.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The local Room database entity for storing raw SMS audit records before they are processed by the backend.
 * This is the sole entity used for local SMS storage.
 */
@Entity(tableName = "local_sms_records")
data class LocalSmsRecord(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,

    val userId: Int, 
    val messageBody: String, // The actual raw SMS text
    val timestamp: Long, // When the SMS was received/recorded (Epoch ms)
    
    val processed: Boolean = false, 
    val backendRefId: String? = null 
)
