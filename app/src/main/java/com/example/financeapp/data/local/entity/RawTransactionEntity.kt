package com.example.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity for storing raw SMS messages locally before/after ingestion.
 */
@Entity(tableName = "raw_transactions")
data class RawTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Int,
    val rawText: String,
    val sender: String,
    val localTimestamp: Long,
    val ingestionStatus: String = "PENDING" // PENDING, SENT, FAILED
)
