// SecureDatabaseModule.kt
package com.example.financeapp.di

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.financeapp.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Singleton

private const val KEYSTORE_ALIAS = "fintraq_keystore_rsa"
private const val WRAPPED_PASSPHRASE_PREF = "fintraq_wrapped_pass"
private const val WRAPPED_PASSPHRASE_KEY = "wrapped_pass_base64"

@Module
@InstallIn(SingletonComponent::class)
object SecureDatabaseModule {

    // Ensure KeyPair exists
    private fun ensureKeyPair() {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!ks.containsAlias(KEYSTORE_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
            kpg.initialize(android.security.keystore.KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_DECRYPT or android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
            ).run {
                setDigests(android.security.keystore.KeyProperties.DIGEST_SHA256, android.security.keystore.KeyProperties.DIGEST_SHA512)
                setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                build()
            })
            kpg.generateKeyPair()
        }
    }

    private fun getWrappedPassFromPrefs(context: Context): String? {
        return try {
            // Use EncryptedSharedPreferences when available
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val prefs = EncryptedSharedPreferences.create(
                context,
                "fintraq_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.getString(WRAPPED_PASSPHRASE_KEY, null)
        } catch (e: Exception) {
            // fallback to regular shared prefs (less ideal) — but still base64
            val prefs = context.getSharedPreferences(WRAPPED_PASSPHRASE_PREF, Context.MODE_PRIVATE)
            prefs.getString(WRAPPED_PASSPHRASE_KEY, null)
        }
    }

    private fun storeWrappedPassToPrefs(context: Context, wrapped: String) {
        try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val prefs = EncryptedSharedPreferences.create(
                context,
                "fintraq_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().putString(WRAPPED_PASSPHRASE_KEY, wrapped).apply()
        } catch (e: Exception) {
            val prefs = context.getSharedPreferences(WRAPPED_PASSPHRASE_PREF, Context.MODE_PRIVATE)
            prefs.edit().putString(WRAPPED_PASSPHRASE_KEY, wrapped).apply()
        }
    }

    private fun wrapRandomPass(context: Context): String {
        ensureKeyPair()
        // generate random 32 bytes passphrase
        val pass = ByteArray(32)
        java.security.SecureRandom().nextBytes(pass)

        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(KEYSTORE_ALIAS, null) as java.security.KeyStore.PrivateKeyEntry
        val pub = entry.certificate.publicKey

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pub)
        val wrapped = cipher.doFinal(pass)
        val wrappedB64 = Base64.encodeToString(wrapped, Base64.NO_WRAP)

        // store wrapped in prefs
        storeWrappedPassToPrefs(context, wrappedB64)
        // also return the raw pass for immediate use
        return Base64.encodeToString(pass, Base64.NO_WRAP)
    }

    private fun unwrapPassFromPrefs(context: Context): ByteArray {
        val wrappedB64 = getWrappedPassFromPrefs(context)
        if (wrappedB64.isNullOrEmpty()) {
            // wrap new pass and return raw
            val rawB64 = wrapRandomPass(context)
            return Base64.decode(rawB64, Base64.NO_WRAP)
        }

        val wrapped = Base64.decode(wrappedB64, Base64.NO_WRAP)
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(KEYSTORE_ALIAS, null) as java.security.KeyStore.PrivateKeyEntry
        val priv = entry.privateKey
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, priv)
        val raw = cipher.doFinal(wrapped)
        return raw
    }

    @Provides
    @Singleton
    fun provideSupportFactory(@ApplicationContext context: Context): SupportFactory {
        val passBytes = unwrapPassFromPrefs(context)
        return SupportFactory(passBytes)
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportFactory
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "fintraq_database")
            // Do not use fallbackToDestructiveMigration() in production
            //.fallbackToDestructiveMigration()
            .openHelperFactory(supportFactory)
            .build()
    }

    // DAOs same as before
}
