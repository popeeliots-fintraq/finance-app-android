package com.example.financeapp.di

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
object NetworkModule {

    // IMPORTANT: Replace this with your actual base URL
    private const val BASE_URL = "https://api.fintraq.com/v1/" 

    // 1. PROVIDE MOSHI: Replaces the old Kotlin Serialization 'Json' object.
    // This resolves the 'error.NonExistentClass' return type for provideJson.
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        // KotlinJsonAdapterFactory is crucial for supporting Kotlin data classes
        // and default values with Moshi.
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // 2. PROVIDE OKHTTP CLIENT
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Set level to BODY for detailed request/response logging in debug builds
            level = HttpLoggingInterceptor.Level.BODY 
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    // 3. PROVIDE RETROFIT: Now depends on and uses the Moshi instance.
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            // Use MoshiConverterFactory instead of the kotlinx.serialization converter
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // You would typically add your API Service provision here, e.g.:
    /*
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): FinanceApiService {
        return retrofit.create(FinanceApiService::class.java)
    }
    */
}
