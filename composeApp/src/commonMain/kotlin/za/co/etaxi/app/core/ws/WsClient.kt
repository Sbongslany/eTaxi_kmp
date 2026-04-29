package za.co.etaxi.app.core.ws

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import za.co.etaxi.app.core.network.ApiConfig
import za.co.etaxi.app.core.network.TokenManager
import za.co.etaxi.app.core.network.WsEnvelope

// Persistent WebSocket connection to the backend's /ws endpoint.
//
// Lifecycle:
//   - Call connect() after the user logs in. The client opens the socket,
//     authenticates with the access token, listens for messages, and
//     reconnects automatically with backoff if the connection drops.
//   - Call disconnect() on logout.
//
// All decoded events are emitted on the `events` SharedFlow. Repositories
// subscribe to this flow and update their own state flows in response.
//
// Auth: backend's wsHub expects the client to send `auth:<accessToken>`
// as the first message after connecting. Older auth strategies passed
// the token as a query param; this client uses the message-based form
// to match the server we have on disk.
class WsClient(
    private val tokenManager: TokenManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var connectionJob: Job? = null

    private val _events = MutableSharedFlow<TripEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )
    val events: SharedFlow<TripEvent> = _events.asSharedFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Dedicated client with WebSockets support. Separate from the REST
    // client because WS uses different timeouts and we don't want JSON
    // content negotiation interfering with frame parsing.
    private val client: HttpClient = HttpClient {
        install(WebSockets)
    }

    fun connect() {
        if (connectionJob?.isActive == true) return
        connectionJob = scope.launch {
            var attempt = 0
            while (isActive) {
                try {
                    runConnection()
                    // Clean exit (server closed) -> retry quickly
                    attempt = 0
                } catch (t: Throwable) {
                    println("[WsClient] connection error: ${t.message}")
                }
                if (!isActive) break
                // Exponential backoff capped at 30s.
                attempt++
                val delayMs = (1000L * (1 shl minOf(attempt, 5))).coerceAtMost(30_000L)
                println("[WsClient] reconnecting in ${delayMs}ms (attempt $attempt)")
                delay(delayMs)
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
    }

    private suspend fun runConnection() {
        val token = tokenManager.getAccessToken()
        if (token.isNullOrBlank()) {
            println("[WsClient] no token - delaying connection")
            delay(2000)
            return
        }
        client.webSocket(
            urlString = ApiConfig.WS_BASE_URL,
            request = {
                header(HttpHeaders.Authorization, "Bearer $token")
            },
        ) {
            // Send auth as first message in case the server reads it from
            // the message stream rather than (or in addition to) the
            // Authorization header.
            send(Frame.Text("auth:$token"))

            // Read frames until the connection closes.
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val raw = frame.readText()
                handleMessage(raw)
            }
        }
    }

    private fun handleMessage(raw: String) {
        // Try to decode as our envelope. Skip silently on parse failure;
        // the server sends some non-trip messages we don't care about
        // (heartbeats, server-time pings, etc).
        val envelope = runCatching { json.decodeFromString(WsEnvelope.serializer(), raw) }
            .getOrNull() ?: return
        val tripId = envelope.tripId ?: return
        val payload = envelope.payload ?: WsPayloadDefaults

        val event: TripEvent? = when (envelope.type) {
            "NEW_RIDE_REQUEST" -> TripEvent.NewRideRequest(
                tripId = tripId,
                pickupLat = payload.pickup?.lat ?: 0.0,
                pickupLng = payload.pickup?.lng ?: 0.0,
                pickupAddress = payload.pickup?.address ?: "",
                dropoffLat = payload.dropoff?.lat ?: 0.0,
                dropoffLng = payload.dropoff?.lng ?: 0.0,
                dropoffAddress = payload.dropoff?.address ?: "",
                estimatedFareCents = payload.estimatedFareCents ?: 0,
                currency = payload.currency ?: "ZAR",
                pickupDistanceKm = payload.distanceKm ?: 0.0,
            )

            "TRIP_STATUS_UPDATE" -> TripEvent.StatusUpdate(
                tripId = tripId,
                status = payload.status ?: return,
                driverId = payload.driverId,
                finalFareCents = payload.finalFareCents,
            )

            "DRIVER_LOCATION_UPDATE" -> TripEvent.DriverLocation(
                tripId = tripId,
                lat = payload.lat ?: return,
                lng = payload.lng ?: return,
                heading = payload.heading,
            )

            "TRIP_CANCELLED" -> TripEvent.Cancelled(
                tripId = tripId,
                by = payload.by,
                reason = payload.reason,
            )

            else -> null
        }

        if (event != null) {
            scope.launch { _events.emit(event) }
        }
    }
}

// Empty-ish payload used when an envelope has no payload field at all
// (defensive — server always sends one for the events we care about).
private val WsPayloadDefaults = za.co.etaxi.app.core.network.WsPayload()
