package com.example.financeapp 

import retrofit2.Response 
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header // Added for user auth
import retrofit2.http.GET // <-- NEW IMPORT: Needed for the Leakage View endpoint
import retrofit2.http.Query // <-- NEW IMPORT: Needed for the reporting_period parameter

// Assuming these new DTOs will be created in data.model package
import com.example.financeapp.data.model.RawSmsIn
import com.example.financeapp.data.model.RawSmsOut 
import com.example.financeapp.data.model.LeakageOut // <-- NEW IMPORT: For the V2 Leakage View

interface ApiService {
    
    // --- V2 Salary Autopilot Raw Ingestion Endpoint ---
    @POST("v2/ingestion/raw-sms") // Changed endpoint to V2/ingestion
    suspend fun ingestRawSms( // Renamed method
        @Header("Authorization") token: String, // You still need this if using JWT/OAuth
        @Body rawSmsData: RawSmsIn // Changed DTO to the simple RawSmsIn
    ): Response<RawSmsOut> // Changed response DTO

    // --- V2 Salary Autopilot Leakage View Endpoint ---
    /**
     * Fetches the comprehensive Leakage Bucket View data for the Autopilot V2 dashboard.
     * Maps to GET /api/v2/leakage/current-leak-view
     */
    @GET("api/v2/leakage/current-leak-view")
    suspend fun fetchLeakageView(
        // API Key handled by the OkHttp Interceptor, so no @Header here.
        @Query("reporting_period_str") reportingPeriod: String
    ): Response<LeakageOut> // Use Response<> for safe error handling
}
