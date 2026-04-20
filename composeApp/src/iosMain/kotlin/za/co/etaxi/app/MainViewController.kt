package za.co.etaxi.app

import androidx.compose.ui.window.ComposeUIViewController
import za.co.etaxi.app.core.di.initKoin

/**
 * Entry point called from Swift. Starts Koin once and returns a UIViewController
 * hosting the Compose UI.
 */
fun MainViewController() = ComposeUIViewController(
    configure = {
        // Compose config placeholder — e.g. enforceStrictPlistSanityCheck = false in 1.7+
    },
) {
    // Koin init is idempotent-safe: we only call once on first invocation.
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
    App()
}

private var koinStarted = false
