package com.example.financeapp.api

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.financeapp.data.model.RawSmsIn
import com.example.financeapp.data.model.RawSmsOut
import com.example.financeapp.data.model.LeakageOut

/**
 * Retrofit interface for the Fin-Traq Backend API (V2).
 */
interface ApiService {

    // --- V2 Salary Autopilot Raw Ingestion Endpoint ---
    @POST("v2/ingestion/raw-sms")
    suspend fun ingestRawSms(
        @Header("Authorization") token: String,
        @Body rawSmsData: RawSmsIn
    ): Response<RawSmsOut>

    // --- V2 Salary Autopilot Leakage View Endpoint (REQUIRED FIX) ---
    /**
     * Fetches the comprehensive Leakage Bucket View data for the Autopilot V2 dashboard.
     * Maps to GET /api/v2/leakage/current-leak-view
     */
    @GET("api/v2/leakage/current-leak-view")
    suspend fun fetchLeakageView(
        @Query("reporting_period_str") reportingPeriod: String // Required query parameter
    ): Response<LeakageOut> // Use Response<> for safe error handling
}
