package com.example.financeapp // Use your actual package name

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/")
    fun getMessage(): Call<Message>
}
