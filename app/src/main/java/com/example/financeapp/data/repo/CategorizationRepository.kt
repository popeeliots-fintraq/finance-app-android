package com.example.financeapp.data.repo

import com.example.financeapp.data.model.CategorizationResponse
import com.example.financeapp.data.model.TransactionRequest
import com.example.financeapp.data.remote.RetrofitClient // We use the client directly for now

class CategorizationRepository {
    
    // Access the service defined in your RetrofitClient
    private val service = RetrofitClient.categorizerService

    // The suspend function performs the asynchronous network call
    suspend fun getCategorizedSpend(rawText: String): Result<CategorizationResponse> {
        return try {
            val request = TransactionRequest(description = rawText)
            
            // Execute the API call
            val response = service.categorizeTransaction(request)
            if (response.isSuccessful && response.body() != null) {
                // ✅ FIX 1: Extract the body before wrapping it in Result.success
                Result.success(response.body()!!)
            } else {
                // Handle API error codes (e.g., 400, 500)
                val errorBody = response.errorBody()?.string() ?: "Unknown API error"
                throw Exception("API call failed: ${response.code()} - $errorBody")
            }    
        } catch (e: Exception) {
            // Failure: Catch network/server errors and return failure
            println("API Error: ${e.message}")
            Result.failure(e)
        }
    }
}
