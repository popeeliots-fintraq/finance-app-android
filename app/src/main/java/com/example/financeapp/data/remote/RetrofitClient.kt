// RetrofitClient.kt (FINAL, FIXED)

package com.example.financeapp.data.remote

import com.example.financeapp.BuildConfig // <-- CRITICAL: Ensure this holds your API Key
import com.example.financeapp.ApiService 
import okhttp3.Interceptor // <-- New Import
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = BuildConfig.API_BASE_URL
    // You must store the API Key securely, usually as a BuildConfig field.
    private const val API_KEY = BuildConfig.FIN_TRAQ_API_KEY // CRITICAL: Assume this exists in BuildConfig

    // 1. Define the API Key Interceptor
    private val apiKeyInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Add the FIN-TRAQ-API-KEY header to the request
        val newRequest = originalRequest.newBuilder()
            .header("FIN-TRAQ-API-KEY", API_KEY) // <--- CRITICAL FIX: Add the header here
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
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
