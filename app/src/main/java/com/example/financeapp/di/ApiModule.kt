package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.auth.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    // -------------------------------
    // Base URL
    // -------------------------------
    @Provides
    @Singleton
    fun provideBaseUrl(): String {
        // TODO: Replace with your Cloud Run backend URL
        return "https://your-cloud-run-url/"
    }

    // -------------------------------
    // Moshi JSON
    // -------------------------------
    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    // -------------------------------
    // Logging Interceptor
    // -------------------------------
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val log = HttpLoggingInterceptor()
        log.level = HttpLoggingInterceptor.Level.BODY
        return log
    }

    // -------------------------------
    // Token Interceptor
    // Injects token into every request
    // -------------------------------
    @Provides
    @Singleton
    fun provideTokenInterceptor(tokenStore: TokenStore): Interceptor {
        return Interceptor { chain ->

            val token = tokenStore.getToken() // returns "Bearer abc" or ""

            val original: Request = chain.request()
            val newRequest = if (token.isNotBlank()) {
                original.newBuilder()
                    .header("Authorization", token)
                    .build()
            } else {
                original
            }

            chain.proceed(newRequest)
        }
    }

    // -------------------------------
    // OkHttpClient with timeouts
    // -------------------------------
    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        tokenInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(tokenInterceptor)  // Add auth first
            .addInterceptor(logging)           // Logging last
            .build()
    }

    // -------------------------------
    // Retrofit instance
    // -------------------------------
    @Provides
    @Singleton
    fun provideRetrofit(
        baseUrl: String,
        moshi: Moshi,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // -------------------------------
    // ApiService
    // -------------------------------
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
