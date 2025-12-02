package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.repository.FinanceRepository // FIX: Changed import from LeakageRepository
import com.example.financeapp.data.model.LeakageOut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeakageViewModel @Inject constructor(
    // FIX: Inject the FinanceRepository, which is now the available/provided repository
    private val financeRepository: FinanceRepository 
) : ViewModel() {

    private val _leakageViewState = MutableStateFlow<LeakageViewState>(LeakageViewState.Loading)
    val leakageViewState: StateFlow<LeakageViewState> = _leakageViewState

    init {
        fetchLeakageData()
    }

    private fun fetchLeakageData() {
        viewModelScope.launch {
            _leakageViewState.value = LeakageViewState.Loading
            try {
                // Use the financeRepository instance
                val leakageOut = financeRepository.getLeakageView() 
                _leakageViewState.value = LeakageViewState.Success(leakageOut)
            } catch (e: Exception) {
                _leakageViewState.value = LeakageViewState.Error("Failed to load leakage data: ${e.message}")
            }
        }
    }

    // --- Data Access and Transformation ---

    // Function to calculate a summary based on the fetched data
    fun getInsightSummary(data: LeakageOut): String {
        val reclaimable = data.totalReclaimableSalary
        val newSalary = data.ifLeakFixedNewSalary
        
        return "You can potentially save $reclaimable, which would increase your effective salary to $newSalary."
    }

    // Function to get the list of buckets for the adapter
    fun getLeakageBuckets(data: LeakageOut) = data.leakageBuckets

    // Function to display the reporting period
    fun getReportingPeriod(data: LeakageOut) = "Data for: ${data.reportingPeriod}"

    
}

// Sealed class to represent the different states of the UI
sealed class LeakageViewState {
    object Loading : LeakageViewState()
    data class Success(val data: LeakageOut) : LeakageViewState()
    data class Error(val message: String) : LeakageViewState()
}
