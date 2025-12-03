package com.example.financeapp.di

import com.example.financeapp.api.ApiService
import com.example.financeapp.network.ApiAuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    // Provide your backend base URL here (or override via BuildConfig / flavors)
    @Provides
    @Singleton
    fun provideBaseUrl(): String = "https://your-cloud-run-url/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        // Log body only in debug builds; adjust with BuildConfig.DEBUG if available
        logging.level =
            if (isDebug()) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(apiAuthInterceptor: ApiAuthInterceptor): Interceptor = apiAuthInterceptor

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(authInterceptor)   // auth first
            .addInterceptor(logging)           // then logging
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String, moshi: Moshi, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // small helper: detect debug (fallback if BuildConfig not available)
    private fun isDebug(): Boolean {
        return try {
            val clz = Class.forName("com.example.financeapp.BuildConfig")
            val field = clz.getField("DEBUG")
            field.getBoolean(null)
        } catch (e: Exception) {
            false
        }
    }
}
