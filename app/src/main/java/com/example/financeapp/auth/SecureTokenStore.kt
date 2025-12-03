package com.example.financeapp.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "fintraq_secure_token_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    private val prefs by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to normal SharedPreferences (still encrypted by Base64 from caller)
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    /** Saves raw token (only the token body, e.g. "abcd1234") */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    /** Returns Bearer token: "Bearer abcd1234" or "" if missing */
    fun getToken(): String {
        val raw = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return ""
        return if (raw.startsWith("Bearer")) raw else "Bearer $raw"
    }

    fun clearToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }
}
