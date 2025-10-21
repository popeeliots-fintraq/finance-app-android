package com.example.financeapp.data.remote

import com.example.financeapp.BuildConfig // <-- CRITICAL: Ensure this is imported!
import com.example.financeapp.ApiService // <-- CRITICAL: Ensure your API service interface is imported!
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ✅ Correctly uses the securely injected Base URL
    private const val BASE_URL = BuildConfig.API_BASE_URL

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // The Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // 🔥 CRITICAL ADDITION: The public property to get the ApiService implementation
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
