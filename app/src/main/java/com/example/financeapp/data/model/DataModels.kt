package com.example.financeapp.data.model

import com.example.financeapp.ui.viewmodel.LeakageUiState

// --- Leakage Data Models ---

/**
 * Model representing a single item in the Leakage Buckets list.
 * Note: Assumes 'bucketName' matches the click listener in MainActivity.
 */
data class LeakageBucketNetwork(
    val bucketId: String,
    val bucketName: String, // e.g., "Dining Out", "Subscriptions"
    val totalLeakageAmount: Double, // The money leaked in this bucket
    val insight: String? = null // Optional insight text
)

/**
 * The full API response model for leakage data.
 * Used by the Repository and ViewModel to handle raw data.
 */
data class LeakageOut(
    val totalReclaimableSalary: Double,
    val ifLeakFixedNewSalary: Double,
    val reportingPeriod: String,
    val leakageBuckets: List<LeakageBucketNetwork>
)

// --- UI State Models ---

/**
 * The data class representing the entire UI state of the Leakage View.
 * This matches the properties accessed in MainActivity (state.leakageBuckets, etc.).
 */
data class LeakageUiState(
    val isLoading: Boolean = false,
    val currentLeakageAmount: Double = 0.0,
    val reclaimedSalaryProjection: Double = 0.0,
    val leakageBuckets: List<LeakageBucketNetwork> = emptyList(),
    val autopilotStatusText: String = "Status Unknown",
    val errorMessage: String? = null
)
