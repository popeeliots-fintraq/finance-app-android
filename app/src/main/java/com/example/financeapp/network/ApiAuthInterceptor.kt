package com.example.financeapp.network

import com.example.financeapp.auth.SecureTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiAuthInterceptor @Inject constructor(
    private val tokenStore: SecureTokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.getToken().trim()

        val builder = original.newBuilder()
            .header("User-Agent", "FinTraq-Android/${android.os.Build.VERSION.RELEASE}")

        if (token.isNotEmpty()) {
            // tokenStore.getToken() returns "Bearer <token>" or "" per TokenStore implementation
            builder.header("Authorization", token)
        }

        val newRequest = builder.build()
        return chain.proceed(newRequest)
    }
}
