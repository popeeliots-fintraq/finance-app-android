package com.example.financeapp // Use your actual package name

import retrofit2.Response // Use Response for CoroutineWorker compatibility
import retrofit2.http.POST
import retrofit2.http.Body
// You must ensure these DTOs are correctly defined and accessible in your project
import com.example.financeapp.data.model.RawTransactionIn
import com.example.financeapp.data.model.CategorizedTransactionOut

interface ApiService {
    // This is the new endpoint that sends the raw SMS data to the backend
    @POST("api/v1/transactions/ingest-raw")
    suspend fun ingestRawTransaction(
        @Body rawTransaction: RawTransactionIn
    ): Response<CategorizedTransactionOut>
}
