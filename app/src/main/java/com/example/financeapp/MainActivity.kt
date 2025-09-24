package com.example.financeapp // Use your actual package name here

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// This data class should be in its own file (e.g., Message.kt)
data class Message(val message: String)

interface ApiService {
    @GET("/")
    fun getMessage(): Call<Message>
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the TextView by its ID
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Welcome to Finance App!"

        // Call the function that makes the network request
        fetchMessage(textView)
    }

    private fun fetchMessage(textView: TextView) {
        // Log to confirm this function is being called
        Log.d("API_CALL", "Attempting to make API call...")

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://finance-app-backend-0qo0.onrender.com/") // Your Render URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getMessage().enqueue(object : Callback<Message> {
            override fun onResponse(call: Call<Message>, response: Response<Message>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message
                    Log.d("API_CALL_SUCCESS", "API Response: $message")
                    textView.text = message // Update the TextView with the API response
                } else {
                    Log.e("API_CALL_ERROR", "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Message>, t: Throwable) {
                Log.e("API_CALL_FAILURE", "API Call Failed: ${t.message}")
            }
        })
    }
}
