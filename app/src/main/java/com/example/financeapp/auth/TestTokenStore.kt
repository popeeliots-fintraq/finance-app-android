// TestTokenStore.kt
package com.example.financeapp.auth

class TestTokenStore : ITokenStore {
    private var token: String = ""

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun getToken(): String {
        return if (token.startsWith("Bearer")) token else "Bearer $token"
    }

    override fun clearToken() {
        token = ""
    }
}
