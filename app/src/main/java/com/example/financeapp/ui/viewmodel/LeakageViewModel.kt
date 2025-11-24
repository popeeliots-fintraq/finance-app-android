package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// *** DTO Imports (Based on your data/model structure) ***
import com.example.financeapp.data.model.LeakageOut
// *** UI Model Imports (Ensure these files exist in ui/model) ***
import com.example.financeapp.ui.model.LeakageUiState
import com.example.financeapp.ui.model.LeakBucketUiModel
// *** API Interface (From your original snippet) ***
import com.fintraq.android.api.ApiService 


class LeakageViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(LeakageUiState())
    val uiState: StateFlow<LeakageUiState> = _uiState

    init {
        // Automatically fetch data when the ViewModel is created
        fetchLeakageView()
    }

    private fun fetchLeakageView() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Call the completed V2 API endpoint. Ensure ApiService returns LeakageOut.
                val response: LeakageOut = apiService.fetchLeakageView()
                
                // CRITICAL STEP: Safely parse String amounts to Double for display.
                // Uses toDoubleOrNull() and defaults to 0.0 if parsing fails.
                val currentLeakage = response.total_reclaimable_salary.toDoubleOrNull() ?: 0.0
                val projectedSalary = response.if_leak_fixed_new_salary.toDoubleOrNull() ?: 0.0
                
                // --- Transformation Logic for Projection Card and Bucket List ---
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    
                    // 1. Projection Card Data (Using converted Doubles)
                    currentLeakageAmount = currentLeakage,
                    reclaimedSalaryProjection = projectedSalary,
                    
                    // 2. Leakage Bucket List Data (Mapping DTO to UI Model)
                    leakageBuckets = response.leakage_buckets.map { networkBucket ->
                        LeakBucketUiModel(
                            // Maps DTO field 'category' to UI model field 'bucketName'
                            bucketName = networkBucket.category, 
                            // Converts and maps DTO field 'leak_amount'
                            leakageAmount = networkBucket.leak_amount.toDoubleOrNull() ?: 0.0
                        )
                    },
                    // Update status as data is successfully loaded
                    autopilotStatusText = "Leakage Data Loaded"
                )
            } catch (e: Exception) {
                // Log the full stack trace for better debugging in GitHub Actions
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error fetching leakage data: ${e.message}"
                )
            }
        }
    }
}
