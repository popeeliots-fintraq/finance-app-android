package com.example.financeapp 

import retrofit2.Response 
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header // Added for user auth
// Assuming these new DTOs will be created in data.model package
import com.example.financeapp.data.model.RawSmsIn
import com.example.financeapp.data.model.RawSmsOut 

interface ApiService {
    // --- V2 Salary Autopilot Raw Ingestion Endpoint (Gap #1 Fix) ---
    /**
     * Sends the RAW, unparsed SMS message to the backend's high-speed IngestionService
     * for asynchronous processing.
     * Maps to POST /v2/ingestion/raw-sms
     */
    @POST("v2/ingestion/raw-sms") // Changed endpoint to V2/ingestion
    suspend fun ingestRawSms( // Renamed method
        @Header("Authorization") token: String, // Added token for user authentication
        @Body rawSmsData: RawSmsIn // Changed DTO to the simple RawSmsIn
    ): Response<RawSmsOut> // Changed response DTO
}
