package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.repo.CategorizationRepository
import kotlinx.coroutines.launch

class CategorizationViewModel : ViewModel() {
    
    // Temporary use of the Repository without Dagger injection for this test
    private val repository = CategorizationRepository()

    fun testApiCall() {
        // A test SMS text that your model should categorize
        val testText = "Debit of Rs 500.00 to AMAZON SELLERS PAI on 01-10-2025."
        
        viewModelScope.launch {
            println("Attempting to categorize: \"$testText\"")
            
            // Call the repository function
            val result = repository.getCategorizedSpend(testText)
            
            // Handle the result
            result.fold(
                onSuccess = { response ->
                    println("SUCCESS! Category: ${response.suggested_category}, Confidence: ${response.confidence_score}")
                },
                onFailure = { error ->
                    println("FAILURE! Check logs for API error details. Message: ${error.message}")
                }
            )
        }
    }
}
