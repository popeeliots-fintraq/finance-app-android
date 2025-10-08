package com.example.financeapp.data.remote

import com.example.financeapp.BuildConfig // REQUIRED to access the API key
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // !! IMPORTANT !!
    private const val BASE_URL = "https://transaction-categorizer-801862457352.us-central1.run.app/"

    // 🎯 STEP 1: Define the Auth Interceptor
    // This Interceptor automatically adds the 'key' query parameter to every request.
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("key", BuildConfig.GEMINI_API_KEY) // Injects the secure key
            .build()
        
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        
        chain.proceed(newRequest)
    }

    // 🎯 STEP 2: Define the OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        // Add logging for debugging (you already had the dependency)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        // Add the authentication interceptor (this fixes the 403)
        .addInterceptor(authInterceptor)
        // Set reasonable timeouts
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 🎯 STEP 3: Build Retrofit using the OkHttpClient
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- MUST use the client with the interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val categorizerService: CategorizerService by lazy {
        retrofit.create(CategorizerService::class.java)
    }
}
