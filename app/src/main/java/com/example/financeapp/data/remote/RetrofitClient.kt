package com.example.financeapp.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.tasks.Tasks
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.ExecutionException

object RetrofitClient {

    // !! IMPORTANT !!
    // BASE_URL is for Retrofit configuration.
    // CLOUD_RUN_AUDIENCE is the target for the ID Token request (often the same URL without trailing slash).
    private const val BASE_URL = "https://transaction-categorizer-801862457352.us-central1.run.app/"
    private const val CLOUD_RUN_AUDIENCE = "https://transaction-categorizer-801862457352.us-central1.run.app"

    // 🛑 YOU MUST INITIALIZE THIS WITH YOUR APPLICATION CONTEXT BEFORE USE
    // Example: MyApp.applicationContext
    private var applicationContext: Context? = null 

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    // 🎯 STEP 1: Function to safely fetch the Google ID Token
    private fun getGoogleIdToken(): String? {
        val context = applicationContext ?: run {
            // Log an error if context is null, this should never happen if initialize() is called
            println("FATAL ERROR: RetrofitClient not initialized with application context.")
            return null
        }
        
        // This requires the Google account to be signed in on the device/emulator.
        val account = GoogleSignIn.getLastSignedInAccount(context)
        
        return if (account != null) {
            try {
                // This call is synchronous and blocking. It is safe here because OkHttp Interceptors 
                // run on a background worker thread.
                // The audience is the Cloud Run URL.
                Tasks.await(GoogleAuthUtil.getToken(context, account.email, CLOUD_RUN_AUDIENCE)) 
            } catch (e: Exception) {
                // Log and print the error details (e.g., token expired, account access issue)
                println("RetrofitClient Auth Error: Could not fetch ID Token.")
                e.printStackTrace()
                null
            }
        } else {
            println("RetrofitClient Auth Warning: No Google account found signed in.")
            null
        }
    }

    // 🎯 STEP 2: Define the ID Token Auth Interceptor
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val idToken = getGoogleIdToken()

        val newRequest = if (idToken != null) {
            originalRequest.newBuilder()
                // FIX: Add the token to the Authorization header, which Cloud Run IAM requires
                .header("Authorization", "Bearer $idToken") 
                .build()
        } else {
            // No token, proceed with original request, but it will result in 403 from Cloud Run
            originalRequest
        }

        chain.proceed(newRequest)
    }

    // 🎯 STEP 3: Define the OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        // Add logging for debugging
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Shows request/response headers and body
        })
        // Add the authentication interceptor (MUST be before your other interceptors if any)
        .addInterceptor(authInterceptor)
        // Set reasonable timeouts
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // 🎯 STEP 4: Build Retrofit using the OkHttpClient
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) 
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Service access property
    val categorizerService: CategorizerService by lazy {
        retrofit.create(CategorizerService::class.java)
    }
}
