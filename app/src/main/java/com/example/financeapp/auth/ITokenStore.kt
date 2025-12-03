// ITokenStore.kt
package com.example.financeapp.auth

interface ITokenStore {
    fun saveToken(token: String)
    fun getToken(): String
    fun clearToken()
}
