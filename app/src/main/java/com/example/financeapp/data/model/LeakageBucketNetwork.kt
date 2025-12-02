package com.example.financeapp.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
// Removed: import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
data class LeakageBucketNetwork(
    // Replaced @SerialName with @Json
    @Json(name = "bucket_id")
    val bucketId: Int,

    @Json(name = "name")
    val name: String,

    @Json(name = "total_leakage_amount")
    val totalLeakageAmount: Double,

    @Json(name = "last_updated")
    val lastUpdated: String // Consider changing to a proper date/time object
)
// Removed the old 'implements Serializable' if it was present
