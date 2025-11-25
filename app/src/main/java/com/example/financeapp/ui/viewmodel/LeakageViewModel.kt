package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import retrofit2.Response // Required for handling Retrofit Response type

// DTO Imports
import com.example.financeapp.data.model.LeakageOut 
// UI Model Imports
import com.example.financeapp.ui.model.LeakageUiState
import com.example.financeapp.ui.model.LeakBucketUiModel
// API Interface Path
import com.example.financeapp.api.ApiService 


@HiltViewModel
class LeakageViewModel @Inject constructor( 
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeakageUiState())
    val uiState: StateFlow<LeakageUiState> = _uiState

    init {
        fetchLeakageView()
    }

    private fun fetchLeakageView() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // IMPORTANT: Calling the API with the required reportingPeriod query parameter.
                // Using "current" as a placeholder for the default period.
                val response: Response<LeakageOut> = apiService.fetchLeakageView(reportingPeriod = "current")
                
                if (response.isSuccessful && response.body() != null) {
                    val leakageOut = response.body()!!

                    // Safely parse String amounts to Double
                    val currentLeakage = leakageOut.total_reclaimable_salary.toDoubleOrNull() ?: 0.0
                    val projectedSalary = leakageOut.if_leak_fixed_new_salary.toDoubleOrNull() ?: 0.0
                    
                    // --- Transformation Logic ---
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        
                        // 1. Projection Card Data
                        currentLeakageAmount = currentLeakage,
                        reclaimedSalaryProjection = projectedSalary,
                        
                        // 2. Leakage Bucket List Data (Mapping DTO to UI Model)
                        leakageBuckets = leakageOut.leakage_buckets.map { networkBucket ->
                            LeakBucketUiModel(
                                bucketName = networkBucket.category, 
                                leakageAmount = networkBucket.leak_amount.toDoubleOrNull() ?: 0.0,
                                insightSummary = networkBucket.insight_summary ?: "Tap for next action."
                            )
                        },
                        autopilotStatusText = "Leakage Data Loaded for Period: ${leakageOut.reporting_period}"
                    )
                } else {
                    // Handle API non-2xx response error (e.g., 404, 500)
                     _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "API Error: ${response.code()} ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network Error: ${e.message}"
                )
            }
        }
    }
}
