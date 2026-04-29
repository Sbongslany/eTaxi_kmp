package za.co.etaxi.app.core.location

import kotlinx.coroutines.flow.Flow
import za.co.etaxi.app.domain.model.LatLng

// Cross-platform GPS streamer.
//
// Implementations:
//   - androidMain: starts a foreground service that uses
//     FusedLocationProviderClient at HIGH_ACCURACY priority and emits
//     a flow of LatLng updates while the service is alive.
//   - iosMain: stub that emits nothing (iOS deferred per project plan).
//
// Permission state: on Android, the caller is responsible for ensuring
// ACCESS_FINE_LOCATION + (Android 14+) FOREGROUND_SERVICE_LOCATION are
// granted before calling start(). If permissions are missing the flow
// will simply not emit; no exception thrown.
//
// Usage:
//   val streamer = rememberLocationStreamer()
//   LaunchedEffect(active) {
//       if (active) {
//           streamer.start()
//           streamer.locations.collect { pos -> ... push to backend ... }
//       } else {
//           streamer.stop()
//       }
//   }
interface LocationStreamer {
    val locations: Flow<LatLng>
    fun start()
    fun stop()
}

// No-op streamer that emits nothing. Used as a fallback on platforms
// that don't have a real implementation, or when DI binding fails.
class NoopLocationStreamer : LocationStreamer {
    override val locations: Flow<LatLng> = kotlinx.coroutines.flow.emptyFlow()
    override fun start() {}
    override fun stop() {}
}
