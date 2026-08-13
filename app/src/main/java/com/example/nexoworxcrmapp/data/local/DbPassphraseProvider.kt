package com.example.nexoworxcrmapp.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

object DbPassphraseProvider {
    private const val PREFS_NAME = "secure_db_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

        val existing = prefs.getString(KEY_PASSPHRASE, null)
        if (existing != null) return android.util.Base64.decode(existing, android.util.Base64.NO_WRAP)

        val newPassphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encoded = android.util.Base64.encodeToString(newPassphrase, android.util.Base64.NO_WRAP)
        prefs.edit().putString(KEY_PASSPHRASE, encoded).apply()
        return newPassphrase
    }
}