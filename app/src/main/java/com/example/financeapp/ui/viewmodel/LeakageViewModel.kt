package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.repository.LeakageRepository
import com.example.financeapp.data.model.LeakageOut
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeakageViewModel @Inject constructor(
    private val leakageRepository: LeakageRepository
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
                // Assuming a suspend function is defined in LeakageRepository
                val leakageOut = leakageRepository.getLeakageView() 
                _leakageViewState.value = LeakageViewState.Success(leakageOut)
            } catch (e: Exception) {
                _leakageViewState.value = LeakageViewState.Error("Failed to load leakage data: ${e.message}")
            }
        }
    }

    // --- Data Access and Transformation (Addressing Compilation Errors) ---

    // Function to calculate a summary based on the fetched data
    fun getInsightSummary(data: LeakageOut): String {
        // FIX: Renamed properties from snake_case to camelCase 
        val reclaimable = data.totalReclaimableSalary
        val newSalary = data.ifLeakFixedNewSalary
        
        // FIX: The original error was 'Unresolved reference: insightSummary' (Line 51/54)
        // I've defined this method to provide the insight summary. 
        // If this function previously had a different name or was a property, 
        // you may need to adjust the UI code that calls it.
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
