package com.example.financeapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RawSmsIn(
    @Json(name = "sms_text")
    val smsText: String,

    @Json(name = "sender_id")
    val senderId: String,

    @Json(name = "timestamp")
    val timestamp: String, // Convert Long -> ISO string in worker

    @Json(name = "user_id")
    val userId: String
)
