package com.example.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing raw SMS data locally before or after processing.
 * NOTE: Added userId field to align with the new RawSmsIn DTO structure.
 */
@Entity(tableName = "raw_sms")
data class RawSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Int = 1, // Placeholder: Assuming a known user ID for the current authenticated user
    val sender: String,
    val content: String,
    val timestamp: Long,
    val ingestionStatus: Int = 0, // 0: Pending, 1: Processed, 2: Failed
    val backendReferenceId: String? = null
)
