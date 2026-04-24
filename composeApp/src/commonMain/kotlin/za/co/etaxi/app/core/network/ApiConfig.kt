package za.co.etaxi.app.core.network

// API configuration. The backend is listening on port 4001 per the
// server log output. For physical-device testing on the same Wi-Fi
// as the backend Mac, the host is 192.168.0.181.
object ApiConfig {
    const val BASE_URL = "http://192.168.0.134:4001/api/v1"
    const val CONNECT_TIMEOUT_MS = 15_000L
    const val REQUEST_TIMEOUT_MS = 30_000L
}
//http://192.168.0.181:4001/api/v1/health