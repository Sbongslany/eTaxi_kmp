package za.co.etaxi.app.core.network

object ApiConfig {
    const val BASE_URL = "http://192.168.0.232:4001/api/v1"
    // The WebSocket endpoint sits at host:port/ws (no /api/v1 prefix).
    // Derived rather than hard-coded so a future BASE_URL change tracks here too.
    const val WS_BASE_URL = "ws://192.168.0.232:4001/ws"
    const val CONNECT_TIMEOUT_MS = 15_000L
    const val REQUEST_TIMEOUT_MS = 30_000L
}
//http://192.168.0.181:4001/api/v1/health
