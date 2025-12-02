package com.example.financeapp.di

import com.example.financeapp.BuildConfig
import com.example.financeapp.api.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    /**
     * Provides the base URL string.
     * We wrap this in a function to avoid KSP issues with direct use of BuildConfig in const val.
     * NOTE: Ensure you have 'BASE_URL' defined in your build.gradle's defaultConfig/buildTypes.
     * Using a safe fallback value for compilation if it's missing.
     */
    @Provides
    fun provideBaseUrl(): String {
        return try {
            BuildConfig.BASE_URL
        } catch (e: Exception) {
            // Placeholder/Fallback URL - replace with your actual development/production API URL
            "https://api.fintraq.com/"
        }
    }

    /**
     * Provides a configured Moshi instance for JSON serialization/deserialization.
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Provides a configured OkHttpClient for networking.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Setup logging interceptor for debugging network calls
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the Retrofit instance.
     * This now consumes the URL provided by provideBaseUrl().
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    /**
     * Provides the ApiService implementation using Retrofit.
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
