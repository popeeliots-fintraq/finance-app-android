package com.example.financeapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Note: Removed the old Kotlin Serialization import: 
// import kotlinx.serialization.Serializable
// import kotlinx.serialization.SerialName

@JsonClass(generateAdapter = true)
data class RawSmsIn(
    @Json(name = "sms_text")
    val smsText: String,

    @Json(name = "sender_id")
    val senderId: String,

    @Json(name = "timestamp")
    val timestamp: String, // Use a proper date/time type if possible, e.g., Instant

    @Json(name = "user_id")
    val userId: String
)
// If you need it to be Serializable for Parceling/passing between components, 
// you would implement the standard Java/Kotlin Serializable interface:
// : java.io.Serializable
