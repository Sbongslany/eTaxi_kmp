package za.co.etaxi.app.core.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Android TokenManager backed by EncryptedSharedPreferences.
 *
 * The master key is stored in the AndroidKeyStore (hardware-backed
 * where available). Token values themselves are encrypted-at-rest with
 * AES-256-GCM for values and AES-256-SIV for keys.
 *
 * A falls-back-to-plain-prefs guard exists for the unlikely case where
 * EncryptedSharedPreferences fails to initialize (e.g. corrupted
 * keystore on a rooted device). We log + fall back rather than crash,
 * because a crash-on-launch is a worse user experience than a
 * sign-in-required recovery path.
 */
actual class TokenManager(context: Context) {
    private val prefs: SharedPreferences = runCatching {
        val key = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        // Catastrophic fallback — app still works but tokens live in plain prefs.
        context.getSharedPreferences(PREFS_NAME_FALLBACK, Context.MODE_PRIVATE)
    }

    actual fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS, token).apply()
    }

    actual fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH, token).apply()
    }

    actual fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)

    actual fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    actual fun hasSession(): Boolean =
        !prefs.getString(KEY_ACCESS, null).isNullOrBlank()

    actual fun clear() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "etaxi_secure_tokens"
        private const val PREFS_NAME_FALLBACK = "etaxi_tokens_fallback"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
    }
}
