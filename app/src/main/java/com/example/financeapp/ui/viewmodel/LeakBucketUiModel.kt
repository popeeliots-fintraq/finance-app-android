package com.example.financeapp.ui.viewmodel

// A simple model for displaying a single leakage bucket item in the list
data class LeakBucketUiModel(
    val bucketName: String,
    val leakageAmount: Double,
    val insightSummary: String = "Tap for next action." // Added default for safety
)
