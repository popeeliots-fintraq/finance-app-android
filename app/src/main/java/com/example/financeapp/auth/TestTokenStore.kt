package com.example.financeapp.auth

/**
 * Simple in-memory implementation of ITokenStore
 * used only for unit tests.
 */
class TestTokenStore : ITokenStore {

    private var token: String = ""

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun getToken(): String {
        if (token.isBlank()) return ""
        return if (token.startsWith("Bearer ")) token else "Bearer $token"
    }

    override fun clearToken() {
        token = ""
    }
}
