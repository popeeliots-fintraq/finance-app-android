package com.example.financeapp.ui.model

// NOTE: LeakBucketUiModel is defined in LeakBucketUiModel.kt and should not be duplicated here.

/**
 * Data class representing the overall state of the Leakage View screen.
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
