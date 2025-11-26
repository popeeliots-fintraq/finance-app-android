package com.example.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for raw, unparsed SMS transactions. Stored securely via SQLCipher.
 */
@Entity(tableName = "raw_transactions")
data class RawTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String, // Mandatory for multi-user support
    val sender: String,
    val rawText: String, // The SMS body
    val ingestionStatus: String, // e.g., PENDING, PROCESSED, ERROR
    val localTimestamp: Long, // Time when the app received/processed it
    val smsTimestamp: Long, // Original timestamp from the SMS
    val uniqueSmsId: String // For deduplication
)
