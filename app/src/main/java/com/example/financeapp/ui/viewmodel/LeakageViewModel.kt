import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.fintraq.android.api.ApiService // Assume this is the API interface

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
                // Call the completed V2 API endpoint
                val response = apiService.fetchLeakageView() [cite: 11]
                
                // --- Transformation Logic for Projection Card ---
                val totalLeakage = response.totalLeakageAmount // Assume this field exists in the backend model
                val projectedSalary = response.projectedNewSalary // Assume this field exists
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentLeakageAmount = totalLeakage,
                    reclaimedSalaryProjection = projectedSalary,
                    leakageBuckets = response.leakageBuckets.map { /* ... map to LeakBucketUiModel */ },
                    // Clear the placeholder status as real data is loaded
                    autopilotStatusText = "Leakage Data Loaded" 
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error fetching leakage data: ${e.message}"
                )
            }
        }
    }
}
