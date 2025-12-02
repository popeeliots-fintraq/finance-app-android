package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.model.LeakageBucketNetwork
import com.example.financeapp.data.model.LeakageOut
import com.example.financeapp.data.model.LeakageUiState
import com.example.financeapp.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeakageViewModel @Inject constructor(
    private val financeRepository: FinanceRepository
) : ViewModel() {

    // FIX: Changed from LeakageViewState to LeakageUiState, and named it 'uiState'
    private val _uiState = MutableStateFlow(LeakageUiState(isLoading = true))
    val uiState: StateFlow<LeakageUiState> = _uiState.asStateFlow()

    init {
        fetchLeakageData()
    }

    private fun fetchLeakageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // FIX: Unresolved reference 'getLeakageView' now points to the function in the Repository stub
                val result: LeakageOut = financeRepository.getLeakageView() 
                
                val totalLeakage = result.leakageBuckets.sumOf { it.totalLeakageAmount }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        currentLeakageAmount = totalLeakage,
                        reclaimedSalaryProjection = result.ifLeakFixedNewSalary,
                        leakageBuckets = result.leakageBuckets,
                        autopilotStatusText = "Tracking ${result.reportingPeriod}",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load leakage data: ${e.message}") }
            }
        }
    }
}
