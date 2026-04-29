package za.co.etaxi.app.core.location

import kotlinx.coroutines.flow.Flow
import za.co.etaxi.app.domain.model.LatLng

// Cross-platform GPS abstraction.
//
// `locations` is a continuous flow that's only populated while start()
// has been called. `lastKnownLocation()` is a one-shot helper that
// returns the device's most recent fix without requiring the caller
// to subscribe to the continuous stream — used by go-online and home
// screens that just need the user's current position once.
interface LocationStreamer {
    val locations: Flow<LatLng>
    fun start()
    fun stop()
    suspend fun lastKnownLocation(): LatLng?
}

// No-op streamer used as a default and on iOS until that platform
// is wired up. Returns nothing for both stream and one-shot reads.
class NoopLocationStreamer : LocationStreamer {
    override val locations: Flow<LatLng> = kotlinx.coroutines.flow.emptyFlow()
    override fun start() {}
    override fun stop() {}
    override suspend fun lastKnownLocation(): LatLng? = null
}
