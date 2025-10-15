package com.example.financeapp.data.remote

import com.example.financeapp.data.model.CategorizationResponse
import com.example.financeapp.data.model.TransactionRequest
import retrofit2.Response // <-- Import the Response class
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CategorizerService {

    // Health check should also return a standard response or unit
    @GET("/")
    suspend fun healthCheck(): Response<String> // Changed to Response<String>

    @POST("/api/v1/categorize") // <-- Corrected path (see below)
    suspend fun categorizeTransaction(
        @Body request: TransactionRequest
    ): Response<CategorizationResponse> // <-- FIXED: Return type must be Response<T>
}
