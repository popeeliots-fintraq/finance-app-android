package com.example.financeapp.data.remote

import com.example.financeapp.data.model.CategorizationResponse
import com.example.financeapp.data.model.TransactionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CategorizerService {

    @GET("/")
    suspend fun healthCheck(): String

    @POST("/categorize")
    suspend fun categorizeTransaction(
        @Body request: TransactionRequest
    ): CategorizationResponse
}
