// RetrofitClient.kt 

package com.example.financeapp.data.remote

import com.example.financeapp.BuildConfig
import com.example.financeapp.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
// New Imports for Kotlinx Serialization Converter
import com.jakewharton.retrofit.converter.kotlinx.serialization.asConverterFactory 
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = BuildConfig.API_BASE_URL
    private const val API_KEY = BuildConfig.FIN_TRAQ_API_KEY // Assumes this is defined in BuildConfig

    // 🔥 CRITICAL FIX 1: Configure the JSON instance for deserialization
    // Since BigDecimal and LocalDate are complex, we configure it to:
    // 1. Ignore unknown keys (robustness).
    // 2. Allow coercion of incorrect types (e.g., if a number comes back as a string).
    // NOTE: This assumes you have changed BigDecimal/LocalDate in your DTOs to String for network transfer.
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true // Helps with minor type mismatches
    }

    // 1. Define the API Key Interceptor
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Add the FIN-TRAQ-API-KEY header to the request
        val newRequest = originalRequest.newBuilder()
            .header("FIN-TRAQ-API-KEY", API_KEY)
            .build()
            
        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        // Add the logging interceptor
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        // 2. Add the API Key Interceptor BEFORE the logging interceptor
        .addInterceptor(apiKeyInterceptor) 
        
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // 🔥 CRITICAL FIX 2: Replace GsonConverterFactory with the Kotlinx Converter
            .addConverterFactory(json.asConverterFactory(contentType)) 
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
