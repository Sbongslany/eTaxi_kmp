package za.co.etaxi.app.core.network

/**
 * Persists the access + refresh token pair across app restarts.
 *
 * Android: backed by EncryptedSharedPreferences (AES-256-GCM + AES-256-SIV)
 * iOS: for now, backed by NSUserDefaults. Upgrading to Keychain is a
 *      separate hardening pass — see iosMain/actual for the upgrade path.
 *
 * The contract is deliberately minimal: store, read, clear. Anything more
 * (rotation, expiry parsing) lives in RemoteSessionRepository.
 */
expect class TokenManager {
    fun saveAccessToken(token: String)
    fun saveRefreshToken(token: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun hasSession(): Boolean
    fun clear()
}
