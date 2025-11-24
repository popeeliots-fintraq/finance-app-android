package com.example.financeapp.ui.model

/**
 * Data class representing a single item in the Leakage Bucket RecyclerView.
 */
data class LeakBucketUiModel(
    val bucketName: String,
    val leakageAmount: Double,
    val insightSummary: String = "Tap for next action."
)

/**
 * Data class representing the overall state of the Leakage View screen.
 * This is used to manage and update the UI via Kotlin Flow/StateFlow.
 */
data class LeakageUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    
    // Projection Card Data
    val currentLeakageAmount: Double = 0.0,
    val reclaimedSalaryProjection: Double = 0.0,
    
    // List Data
    val leakageBuckets: List<LeakBucketUiModel> = emptyList(),
    
    // Autopilot Status
    val autopilotStatusText: String = "Initializing Autopilot..."
)
