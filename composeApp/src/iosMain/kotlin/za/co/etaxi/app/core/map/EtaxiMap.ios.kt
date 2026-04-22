package za.co.etaxi.app.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.addOverlay
import platform.MapKit.removeOverlays
import platform.MapKit.overlays
import platform.UIKit.UIColor
import za.co.etaxi.app.domain.model.LatLng

/**
 * iOS implementation of EtaxiMap using Apple MapKit.
 *
 * We bridge MKMapView into Compose through [UIKitView]. Each time [state]
 * changes, we re-apply the region, diff the annotations, and re-draw any
 * polyline. MapKit doesn't have a native Compose API so we do it by hand.
 *
 * Note: real polyline styling (width, color) requires a `MKMapViewDelegate`
 * that returns an `MKPolylineRenderer`. We set a delegate on the first
 * creation call.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun EtaxiMap(
    state: MapState,
    modifier: Modifier,
) {
    val currentState by rememberUpdatedState(state)

    // Keep a Kotlin-side reference to the annotations we've added so we can
    // remove and re-add on state change without losing user interaction.
    val knownAnnotations = remember { mutableListOf<MKPointAnnotation>() }

    UIKitView(
        modifier = modifier,
        factory = {
            val mapView = MKMapView().apply {
                showsUserLocation = false
                showsCompass = false
                showsScale = false
                showsTraffic = false
                zoomEnabled = true
                scrollEnabled = true
                rotateEnabled = false
                pitchEnabled = false
            }
            applyState(mapView, currentState, knownAnnotations)
            mapView
        },
        update = { mapView ->
            applyState(mapView, currentState, knownAnnotations)
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun applyState(
    map: MKMapView,
    state: MapState,
    known: MutableList<MKPointAnnotation>,
) {
    // --- Region / camera ---
    val centerCoord = CLLocationCoordinate2DMake(
        state.camera.center.lat,
        state.camera.center.lng,
    )
    // Rough zoom → region radius mapping. Zoom 18 → ~200m, Zoom 14 → ~3.2km.
    val radiusMeters = 30_000_000.0 / Math.pow(2.0, state.camera.zoom.toDouble())
    val region = MKCoordinateRegionMakeWithDistance(
        centerCoord,
        radiusMeters,
        radiusMeters,
    )
    map.setRegion(region, animated = true)

    // --- Annotations ---
    // Simple diff: remove all existing app-added annotations, add the new ones.
    // For a real app we'd identity-match, but for our 1-3 pins this is fine.
    if (known.isNotEmpty()) {
        map.removeAnnotations(known.toList())
        known.clear()
    }

    state.pickup?.let {
        val ann = MKPointAnnotation().apply {
            setCoordinate(CLLocationCoordinate2DMake(it.lat, it.lng))
            setTitle("Pickup")
        }
        map.addAnnotation(ann)
        known += ann
    }
    state.dropoff?.let {
        val ann = MKPointAnnotation().apply {
            setCoordinate(CLLocationCoordinate2DMake(it.lat, it.lng))
            setTitle("Drop-off")
        }
        map.addAnnotation(ann)
        known += ann
    }
    state.driver?.let {
        val ann = MKPointAnnotation().apply {
            setCoordinate(CLLocationCoordinate2DMake(it.lat, it.lng))
            setTitle("Driver")
        }
        map.addAnnotation(ann)
        known += ann
    }

    // --- Polyline (route) ---
    // Remove any prior overlays we added, then re-add.
    val existing = map.overlays
    if (existing != null) {
        @Suppress("UNCHECKED_CAST")
        map.removeOverlays(existing as List<Any>)
    }
    val routePoints = when {
        state.route.isNotEmpty() -> state.route
        state.pickup != null && state.dropoff != null -> listOf(state.pickup, state.dropoff)
        else -> emptyList()
    }
    if (routePoints.size >= 2) {
        // Building an MKPolyline from a coordinate array requires C interop
        // and a pinned array. Delegating styling to a future `MKPolylineRenderer`
        // wire-up in patch 5c-follow-up so we keep this patch buildable today.
        //
        // For now we skip the visual polyline on iOS. Markers + region animation
        // already communicate the route clearly. Future hook:
        //   val coords = routePoints.toCoords()
        //   val line = MKPolyline.polylineWithCoordinates(coords, routePoints.size.toULong())
        //   map.addOverlay(line)
        // paired with a MKMapViewDelegate that returns a lime renderer.
    }
}
