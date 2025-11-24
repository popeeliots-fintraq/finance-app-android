package com.example.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The local Room database entity for storing raw SMS audit records before they are processed by the backend.
 * This entity replaces the older RawSmsEntity to maintain clean nomenclature.
 */
@Entity(tableName = "local_sms_records")
data class LocalSmsRecord(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,

    val userId: String,
    val messageBody: String, // The actual raw SMS text
    val timestamp: Long, // When the SMS was received/recorded (Epoch ms)
    
    // Status fields
    val processed: Boolean = false, // True if successfully sent to backend
    val backendRefId: String? = null // Backend's reference ID if ingestion was successful
)
