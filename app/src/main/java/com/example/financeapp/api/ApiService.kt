package com.example.financeapp.api

import com.example.financeapp.data.model.RawSmsIn
import com.example.financeapp.data.model.RawSmsOut
import com.example.financeapp.data.model.LeakageOut
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for the Fin-Traq Backend API (V2).
 * Provides endpoints for salary autopilot ingestion and leakage view.
 */
interface ApiService {

    /**
     * Ingest raw SMS data for Salary Autopilot V2.
     * @param token Authorization Bearer token
     * @param rawSmsData Raw SMS payload
     * @return Response wrapping RawSmsOut
     */
    @POST("v2/ingestion/raw-sms")
    suspend fun ingestRawSms(
        @Header("Authorization") token: String,
        @Body rawSmsData: RawSmsIn
    ): Response<RawSmsOut>

    /**
     * Fetch the current Leakage Bucket View.
     * @param reportingPeriod Required query param in format e.g., "2025-12"
     * @return Response wrapping LeakageOut
     */
    @GET("api/v2/leakage/current-leak-view")
    suspend fun fetchLeakageView(
        @Query("reporting_period_str") reportingPeriod: String
    ): Response<LeakageOut>
}
