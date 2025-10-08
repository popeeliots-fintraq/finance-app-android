package com.example.financeapp.data.remote

import com.example.financeapp.data.model.CategorizationResponse
import com.example.financeapp.data.model.TransactionRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface CategorizerService {

    @POST("/categorize")
    suspend fun categorizeTransaction(
        @Body request: TransactionRequest
    ): CategorizationResponse
}
