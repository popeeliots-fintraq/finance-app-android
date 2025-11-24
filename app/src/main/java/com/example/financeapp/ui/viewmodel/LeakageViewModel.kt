package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// *** DTO Imports ***
import com.example.financeapp.data.model.LeakageOut 
// *** UI Model Imports ***
import com.example.financeapp.ui.model.LeakageUiState
import com.example.financeapp.ui.model.LeakBucketUiModel
// *** Corrected API Interface Path (Assuming standard package structure) ***
import com.example.financeapp.api.ApiService 


class LeakageViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(LeakageUiState())
    val uiState: StateFlow<LeakageUiState> = _uiState

    init {
        fetchLeakageView()
    }

    private fun fetchLeakageView() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Call the actual function on the API service (resolves fetchLeakageView)
                val response: LeakageOut = apiService.fetchLeakageView()
                
                // Safely parse String amounts to Double
                val currentLeakage = response.total_reclaimable_salary.toDoubleOrNull() ?: 0.0
                val projectedSalary = response.if_leak_fixed_new_salary.toDoubleOrNull() ?: 0.0
                
                // --- Transformation Logic ---
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    
                    // 1. Projection Card Data
                    currentLeakageAmount = currentLeakage,
                    reclaimedSalaryProjection = projectedSalary,
                    
                    // 2. Leakage Bucket List Data (Mapping DTO to UI Model)
                    leakageBuckets = response.leakage_buckets.map { networkBucket ->
                        LeakBucketUiModel(
                            bucketName = networkBucket.category, 
                            leakageAmount = networkBucket.leak_amount.toDoubleOrNull() ?: 0.0
                        )
                    },
                    autopilotStatusText = "Leakage Data Loaded"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error fetching leakage data: ${e.message}"
                )
            }
        }
    }
}
