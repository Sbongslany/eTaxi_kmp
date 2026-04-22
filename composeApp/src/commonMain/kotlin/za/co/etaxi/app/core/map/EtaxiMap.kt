package za.co.etaxi.app.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import za.co.etaxi.app.domain.model.LatLng

/**
 * Platform-agnostic map camera target.
 *
 * - `center` is what the map should be looking at.
 * - `zoom` is the Google Maps / MapKit zoom level (≈2 world, ≈15 street, ≈18 building).
 * - `followDriver` = if a driver location is provided, keep camera locked to it
 *   and re-center automatically as it moves.
 */
@Stable
data class MapCamera(
    val center: LatLng,
    val zoom: Float = 14f,
    val followDriver: Boolean = false,
)

/**
 * The state an Etaxi map renders. Pass nulls when a pin isn't relevant.
 *
 * Implementations (Android / iOS) decide how to draw pickup/dropoff/driver
 * markers in their native style but should visually distinguish them.
 */
@Stable
data class MapState(
    val camera: MapCamera,
    val pickup: LatLng? = null,
    val dropoff: LatLng? = null,
    val driver: LatLng? = null,
    /**
     * Optional polyline representing the route. For mock mode this can be
     * just [pickup, dropoff]; for a real directions API result this is the
     * decoded polyline.
     */
    val route: List<LatLng> = emptyList(),
)

/**
 * The main cross-platform map composable.
 *
 * - On Android: Google Maps (via maps-compose).
 * - On iOS: Apple MapKit (via UIKitView).
 * - On other / unimplemented platforms: mock canvas fallback.
 *
 * Keep this composable side-effect-free; the caller owns [state] and updates it.
 */
@Composable
expect fun EtaxiMap(
    state: MapState,
    modifier: Modifier = Modifier,
)

/**
 * Sensible default camera centered on central Johannesburg.
 * Use when the screen opens before real state arrives.
 */
fun defaultJoburgCamera(): MapCamera = MapCamera(
    center = LatLng(-26.2041, 28.0473),
    zoom = 13f,
)
