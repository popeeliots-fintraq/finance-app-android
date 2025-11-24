data class LeakageUiState(
    // Status to manage loading/error/success states
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Core data for the Projection Card
    val currentLeakageAmount: Double = 0.0,
    val reclaimedSalaryProjection: Double = 0.0, // The "If Leak Fixed → New Salary" value
    
    // Data for the Leakage Bucket List (next task)
    val leakageBuckets: List<LeakBucketUiModel> = emptyList(),
    
    // Flag to ensure the Autopilot is confirmed running
    val autopilotStatusText: String = "Fin-Traq V2: Salary Autopilot Running in Worker" [cite: 22]
)
