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
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher
import javax.inject.Singleton

private const val KEYSTORE_ALIAS = "fintraq_keystore_rsa"
private const val SECURE_PREF_NAME = "fintraq_secure_prefs"
private const val WRAPPED_PASSPHRASE_KEY = "wrapped_pass_base64"

@Module
@InstallIn(SingletonComponent::class)
object SecureDatabaseModule {

    private fun ensureKeyPair() {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!ks.containsAlias(KEYSTORE_ALIAS)) {
            val spec = android.security.keystore.KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                        android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setDigests(
                    android.security.keystore.KeyProperties.DIGEST_SHA1,
                    android.security.keystore.KeyProperties.DIGEST_SHA256
                )
                .build()

            val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
            generator.initialize(spec)
            generator.generateKeyPair()
        }
    }

    private fun getWrapped(context: Context): String? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                SECURE_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.getString(WRAPPED_PASSPHRASE_KEY, null)
        } catch (e: Exception) {
            context.getSharedPreferences(SECURE_PREF_NAME, Context.MODE_PRIVATE)
                .getString(WRAPPED_PASSPHRASE_KEY, null)
        }
    }

    private fun storeWrapped(context: Context, wrapped: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                SECURE_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().putString(WRAPPED_PASSPHRASE_KEY, wrapped).apply()
        } catch (e: Exception) {
            context.getSharedPreferences(SECURE_PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString(WRAPPED_PASSPHRASE_KEY, wrapped).apply()
        }
    }

    private fun createAndWrapPass(context: Context): ByteArray {
        ensureKeyPair()
        val pass = ByteArray(32)
        java.security.SecureRandom().nextBytes(pass)

        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateEntry = ks.getEntry(KEYSTORE_ALIAS, null) as KeyStore.PrivateKeyEntry
        val publicKey = privateEntry.certificate.publicKey

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val wrapped = cipher.doFinal(pass)
        storeWrapped(context, Base64.encodeToString(wrapped, Base64.NO_WRAP))

        return pass
    }

    private fun unwrapPass(context: Context): ByteArray {
        val wrappedB64 = getWrapped(context)
        if (wrappedB64.isNullOrEmpty()) return createAndWrapPass(context)

        val wrapped = Base64.decode(wrappedB64, Base64.NO_WRAP)
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val privateEntry = ks.getEntry(KEYSTORE_ALIAS, null) as KeyStore.PrivateKeyEntry
        val privateKey = privateEntry.privateKey

        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        return cipher.doFinal(wrapped)
    }

    @Provides
    @Singleton
    fun provideSupportFactory(@ApplicationContext context: Context): SupportFactory {
        return SupportFactory(unwrapPass(context))
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        supportFactory: SupportFactory
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "fintraq_database")
            .openHelperFactory(supportFactory)
            .build()
    }
}
