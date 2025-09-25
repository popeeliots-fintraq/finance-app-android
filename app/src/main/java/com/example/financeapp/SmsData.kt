package com.example.financeapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_table")
data class SmsData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val messageBody: String,
    val timestamp: Long
)
