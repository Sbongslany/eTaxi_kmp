package za.co.etaxi.app.core.network

import platform.Foundation.NSUserDefaults

/**
 * iOS TokenManager — NSUserDefaults-backed.
 *
 * NSUserDefaults is NOT secure — values are plaintext on the device
 * filesystem. This is fine for a dev build / first-milestone
 * integration, but before public release we must migrate to Keychain
 * via SFSafariServices or a platform-specific KMP-compatible lib
 * (e.g. multiplatform-settings with ios-keychain module).
 *
 * Tracking this as tech debt. The public API is identical to the
 * Android EncryptedSharedPreferences impl so swapping the guts later
 * requires no call-site changes.
 */
actual class TokenManager {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun saveAccessToken(token: String) {
        defaults.setObject(token, forKey = KEY_ACCESS)
    }

    actual fun saveRefreshToken(token: String) {
        defaults.setObject(token, forKey = KEY_REFRESH)
    }

    actual fun getAccessToken(): String? =
        defaults.stringForKey(KEY_ACCESS)

    actual fun getRefreshToken(): String? =
        defaults.stringForKey(KEY_REFRESH)

    actual fun hasSession(): Boolean =
        !defaults.stringForKey(KEY_ACCESS).isNullOrBlank()

    actual fun clear() {
        defaults.removeObjectForKey(KEY_ACCESS)
        defaults.removeObjectForKey(KEY_REFRESH)
    }

    companion object {
        private const val KEY_ACCESS = "etaxi.access_token"
        private const val KEY_REFRESH = "etaxi.refresh_token"
    }
}
