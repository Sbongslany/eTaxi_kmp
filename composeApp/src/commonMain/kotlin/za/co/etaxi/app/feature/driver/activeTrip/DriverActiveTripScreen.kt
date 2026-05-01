package za.co.etaxi.app.feature.driver.activeTrip

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.location.LocationStreamer
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapCamera
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.feature.driver.rate.RateRiderScreen

class DriverActiveTripScreen(private val tripId: String) : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val streamer: LocationStreamer = koinInject()
        val scope = rememberCoroutineScope()
        val trip by repo.activeTrip().collectAsState(initial = null)

        var driverPos by remember { mutableStateOf<LatLng?>(null) }
        var cancelling by remember { mutableStateOf(false) }

        // Start the location streamer when the screen mounts and stop on
        // dispose. The streamer is shared via Koin so multiple subscribers
        // on the same flow are fine.
        LaunchedEffect(Unit) {
            streamer.start()
        }

        // Forward GPS pings to the backend. We push every received fix
        // (the streamer is configured at 3-second intervals).
        LaunchedEffect(trip?.id, trip?.status) {
            val t = trip ?: return@LaunchedEffect
            // Only push during the actively-moving statuses.
            val pushable = t.status == TripStatus.IN_PROGRESS ||
                t.status == TripStatus.DRIVER_ARRIVING ||
                t.status == TripStatus.ACCEPTED ||
                t.status == TripStatus.ARRIVED
            if (!pushable) return@LaunchedEffect
            streamer.locations.collect { pos ->
                driverPos = pos
                runCatching { repo.pushLocation(t.id, pos) }
            }
        }

        // Status observer — only handles cancellation. End-trip nav is
        // done directly in the click handler so it can't be cancelled
        // by status-flow churn.
        var navigated by remember { mutableStateOf(false) }
        LaunchedEffect(trip?.status) {
            val s = trip?.status ?: return@LaunchedEffect
            println("[DriverActiveTrip] status=$s navigated=$navigated")
            if (s == TripStatus.CANCELLED && !navigated) {
                navigated = true
                println("[DriverActiveTrip] trip cancelled, popping")
                streamer.stop()
                nav.pop()
            }
        }

        // Build the route polyline: pickup -> stops -> dropoff
        val routePoints: List<LatLng> = remember(trip) {
            val t = trip
            if (t == null) emptyList()
            else buildList {
                add(t.pickup.location)
                t.stops.forEach { add(it.location) }
                add(t.dropoff.location)
            }
        }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            EtaxiMap(
                state = MapState(
                    camera = MapCamera(
                        center = driverPos
                            ?: trip?.pickup?.location
                            ?: defaultJoburgCamera().center,
                        zoom = if (trip?.status == TripStatus.IN_PROGRESS) 15f else 14f,
                        followDriver = trip?.status == TripStatus.IN_PROGRESS,
                    ),
                    pickup = trip?.pickup?.location,
                    dropoff = trip?.dropoff?.location,
                    driver = driverPos,
                    route = routePoints,
                ),
                modifier = Modifier.fillMaxSize(),
            )

            trip?.let { t ->
                // Rider card (top)
                RiderCard(
                    trip = t,
                    onCancel = {
                        scope.launch {
                            cancelling = true
                            runCatching { repo.cancelTrip(t.id, "Cancelled by driver") }
                            cancelling = false
                            nav.pop()
                        }
                    },
                    cancelling = cancelling,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                )

                // Action sheet (bottom)
                ActionSheet(
                    trip = t,
                    onPrimary = {
                        scope.launch {
                            when (t.status) {
                                TripStatus.ACCEPTED        -> repo.markArriving(t.id)
                                TripStatus.DRIVER_ARRIVING -> repo.markArrived(t.id)
                                TripStatus.ARRIVED         -> repo.startTrip(t.id)
                                TripStatus.IN_PROGRESS     -> {
                                    // End trip — call repo and navigate
                                    // directly from the click handler.
                                    // Don't depend on LaunchedEffect to
                                    // observe the COMPLETED state because
                                    // status-flow churn (WS refresh,
                                    // optimistic updates) can cancel the
                                    // observer's coroutine before it
                                    // navigates.
                                    runCatching { repo.endTrip(t.id) }
                                    streamer.stop()
                                    nav.replaceAll(
                                        RateRiderScreen(
                                            tripId = t.id,
                                            riderName = t.rider?.name ?: "Rider",
                                            fareLabel = formatRand((t.fareCents * 0.8).toInt()),
                                        ),
                                    )
                                }
                                else                       -> nav.pop()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                )
            }

            // Stop streaming location when this screen leaves the
            // composition. Without this the streamer keeps firing GPS
            // pings to a finished trip.
            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose {
                    streamer.stop()
                }
            }
        }
    }
}

@Composable
private fun RiderCard(
    trip: Trip,
    onCancel: () -> Unit,
    cancelling: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.Power),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = trip.rider?.name?.firstOrNull()?.uppercase()?.toString() ?: "R",
                    color = EtaxiColors.InkOnPower,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Rider", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
                Text(
                    trip.rider?.name ?: "Rider",
                    style = MaterialTheme.typography.titleMedium,
                    color = EtaxiColors.Ink,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = (trip.rider?.rating ?: 5.0).toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.InkMuted,
                    )
                }
            }
            // Cancel button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.Danger.copy(alpha = 0.15f))
                    .clickable(enabled = !cancelling, onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Close, null,
                    tint = EtaxiColors.Danger,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                formatRand(trip.fareCents),
                style = MaterialTheme.typography.titleLarge,
                color = EtaxiColors.Power,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.PlayArrow, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                trip.pickup.address,
                color = EtaxiColors.Ink,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        // Stops in between (if any)
        trip.stops.forEach { stop ->
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Add, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stop.address,
                    color = EtaxiColors.InkMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.ArrowOutward, null,
                tint = EtaxiColors.InkMuted,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                trip.dropoff.address,
                color = EtaxiColors.InkMuted,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ActionSheet(
    trip: Trip,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        AnimatedContent(
            targetState = trip.status,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status",
        ) { s ->
            Text(
                when (s) {
                    TripStatus.ACCEPTED        -> "Head to pickup"
                    TripStatus.DRIVER_ARRIVING -> "Arriving at pickup"
                    TripStatus.ARRIVED         -> "You're at the pickup point"
                    TripStatus.IN_PROGRESS     -> "Trip in progress"
                    TripStatus.COMPLETED       -> "Trip completed"
                    else                       -> "—"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = EtaxiColors.Ink,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${trip.distanceKm} km - ${trip.durationMinutes} min - you earn ${formatRand((trip.fareCents * 0.8).toInt())}",
            style = MaterialTheme.typography.bodyMedium,
            color = EtaxiColors.InkMuted,
        )
        Spacer(Modifier.height(20.dp))

        // Primary action button only — driver doesn't add stops, only riders do.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(EtaxiColors.Power)
                .clickable { onPrimary() },
            contentAlignment = Alignment.Center,
        ) {
                Text(
                    when (trip.status) {
                        TripStatus.ACCEPTED        -> "I'm on my way"
                        TripStatus.DRIVER_ARRIVING -> "I've arrived"
                        TripStatus.ARRIVED         -> "Start trip"
                        TripStatus.IN_PROGRESS     -> "End trip"
                        else                       -> "Close"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = EtaxiColors.InkOnPower,
                )
            }
    }
}
