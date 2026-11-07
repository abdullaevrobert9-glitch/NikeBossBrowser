package com.nikeboss.browser

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Хранит ТОЛЬКО email/логин, привязанный к домену сайта.
 * Пароли НЕ хранятся. Никогда.
 */
class CredentialsStore(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "nikeboss_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun keyFor(domain: String) = "login::$domain"

    fun saveLogin(domain: String, login: String) {
        if (domain.isBlank() || login.isBlank()) return
        prefs.edit().putString(keyFor(domain), login).apply()
    }

    fun getLogin(domain: String): String? = prefs.getString(keyFor(domain), null)

    fun clearLogin(domain: String) {
        prefs.edit().remove(keyFor(domain)).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
