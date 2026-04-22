package za.co.etaxi.app.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng as GLatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.LatLng

@Composable
actual fun EtaxiMap(
    state: MapState,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.camera.center.toGoogle(),
            state.camera.zoom,
        )
    }

    LaunchedEffect(state.camera.center, state.camera.zoom) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            state.camera.center.toGoogle(),
            state.camera.zoom,
        )
    }

    LaunchedEffect(state.camera.followDriver, state.driver) {
        val d = state.driver
        if (state.camera.followDriver && d != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                d.toGoogle(),
                state.camera.zoom.coerceAtLeast(15f),
            )
        }
    }

    val properties = remember {
        MapProperties(mapType = MapType.NORMAL, isMyLocationEnabled = false)
    }
    val uiSettings = remember {
        MapUiSettings(
            compassEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false,
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
    ) {
        val routePts = buildList {
            if (state.route.isNotEmpty()) {
                addAll(state.route)
            } else {
                state.pickup?.let { add(it) }
                state.dropoff?.let { add(it) }
            }
        }
        if (routePts.size >= 2) {
            Polyline(
                points = routePts.map { it.toGoogle() },
                color = EtaxiColors.Power,
                width = 14f,
            )
        }
        state.pickup?.let {
            Marker(state = MarkerState(position = it.toGoogle()), title = "Pickup")
        }
        state.dropoff?.let {
            Marker(state = MarkerState(position = it.toGoogle()), title = "Drop-off")
        }
        state.driver?.let {
            Marker(state = MarkerState(position = it.toGoogle()), title = "Driver")
        }
    }
}

private fun LatLng.toGoogle(): GLatLng = GLatLng(lat, lng)
