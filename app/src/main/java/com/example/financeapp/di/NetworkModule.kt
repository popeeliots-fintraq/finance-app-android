package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.BuildConfig // Added to resolve BuildConfig reference
import com.jakewharton.retrofit.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger Hilt module to provide application-wide network dependencies.
 * This has been refactored to use Kotlinx Serialization instead of Moshi.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides the Kotlinx Serialization Json instance.
     * Configuration includes ignoring unknown keys for robustness.
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            // Allows parsing JSON fields that may be missing in the Kotlin data class
            ignoreUnknownKeys = true
            // Allows for flexible JSON structures (e.g., if numbers are quoted as strings)
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Check against BuildConfig.DEBUG for dynamic logging level
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    // Parameter changed from Moshi to Json
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        // Define the media type for the converter factory
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            // Preserving your original backend URL
            .baseUrl("https://fintraq.backend.example.com/") 
            .client(okHttpClient)
            // Using Kotlinx Serialization converter factory
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
