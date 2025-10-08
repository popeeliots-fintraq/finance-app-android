package com.example.financeapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // !! IMPORTANT !!
    // REPLACE this with your actual live Cloud Run service URL
    // e.g., "https://frictionless-finance-service-hash-uc.a.run.app/"
    private const val BASE_URL = "YOUR_CLOUD_RUN_SERVICE_URL_HERE/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val categorizerService: CategorizerService by lazy {
        retrofit.create(CategorizerService::class.java)
    }
}
