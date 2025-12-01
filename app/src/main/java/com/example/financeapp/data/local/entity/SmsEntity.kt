package com.example.financeapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an SMS record in Fin-Traq.
 */
@Entity(tableName = "sms_entity")
data class SmsEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "processed")
    val processed: Boolean = false,

    @ColumnInfo(name = "backendRefId")
    val backendRefId: String? = null
)
