package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
// Removed: import com.example.financeapp.data.repo.CategorizationRepository

class CategorizationViewModel : ViewModel() {
    
    // Removed all dependencies on CategorizationRepository which was deleted.
    // The transaction ingestion process is now handled by SmsProcessingWorker.

    // Placeholder function to confirm the ViewModel is operational.
    fun logStatus() {
        viewModelScope.launch {
            // Log for debugging or future initialization
            println("CategorizationViewModel initialized. This ViewModel is now clean and ready for Leakage Dashboard data binding.")
        }
    }
}
