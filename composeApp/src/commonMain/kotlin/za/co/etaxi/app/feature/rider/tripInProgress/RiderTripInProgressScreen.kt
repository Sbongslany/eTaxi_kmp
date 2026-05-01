package za.co.etaxi.app.feature.rider.tripInProgress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapCamera
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.repository.RiderRepository
import za.co.etaxi.app.feature.rider.rate.RateDriverScreen

// Rider's active-trip screen. The whole map fills the background. The
// bottom sheet is draggable: it has a peek state (status + ETA + driver
// name) and an expanded state (full driver card, stops list with
// reorder controls, fare breakdown, action buttons).
class RiderTripInProgressScreen(private val tripId: String) : Screen {

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: RiderRepository = koinInject()
        val scope = rememberCoroutineScope()
        val trip by repo.activeTrip().collectAsState(initial = null)
        val driverLoc by repo.driverLocation().collectAsState(initial = null)

        var showAddStop by remember { mutableStateOf(false) }
        var navigated by remember { mutableStateOf(false) }

        // Navigate to rate screen on completion. Same hardening as the
        // driver screen — no suspend calls before nav, idempotent flag
        // so duplicate WS events can't crash the screen.
        LaunchedEffect(trip?.status) {
            val s = trip?.status ?: return@LaunchedEffect
            if (s == TripStatus.COMPLETED && !navigated) {
                val t = trip ?: return@LaunchedEffect
                navigated = true
                nav.replaceAll(
                    RateDriverScreen(
                        tripId = t.id,
                        driverName = t.driver?.name ?: "Driver",
                        fareLabel = formatRand(t.fareCents),
                    ),
                )
            } else if (s == TripStatus.CANCELLED && !navigated) {
                navigated = true
                nav.pop()
            }
        }

        // Build polyline pickup -> stops -> dropoff
        val routePoints: List<LatLng> = remember(trip) {
            val t = trip
            if (t == null) emptyList()
            else buildList {
                add(t.pickup.location)
                t.stops.forEach { add(it.location) }
                add(t.dropoff.location)
            }
        }

        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true,
        )
        val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 220.dp,
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            sheetShadowElevation = 8.dp,
            sheetDragHandle = {
                // Custom handle so users SEE the sheet is draggable.
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .size(width = 48.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.SlidesSoft),
                )
            },
            sheetContent = {
                trip?.let { t ->
                    SheetBody(
                        trip = t,
                        onAddStop = { showAddStop = true },
                        onMoveStopUp = { stopIdx ->
                            val ids = t.stops.mapNotNull { it.id }.toMutableList()
                            if (stopIdx > 0 && stopIdx < ids.size) {
                                val tmp = ids[stopIdx]
                                ids[stopIdx] = ids[stopIdx - 1]
                                ids[stopIdx - 1] = tmp
                                scope.launch {
                                    runCatching { repo.reorderStops(t.id, ids) }
                                }
                            }
                        },
                        onMoveStopDown = { stopIdx ->
                            val ids = t.stops.mapNotNull { it.id }.toMutableList()
                            if (stopIdx in 0 until ids.size - 1) {
                                val tmp = ids[stopIdx]
                                ids[stopIdx] = ids[stopIdx + 1]
                                ids[stopIdx + 1] = tmp
                                scope.launch {
                                    runCatching { repo.reorderStops(t.id, ids) }
                                }
                            }
                        },
                        onCancel = {
                            scope.launch {
                                runCatching { repo.cancelTrip(t.id) }
                                nav.pop()
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                EtaxiMap(
                    state = MapState(
                        camera = MapCamera(
                            center = driverLoc
                                ?: trip?.pickup?.location
                                ?: defaultJoburgCamera().center,
                            zoom = if (trip?.status == TripStatus.IN_PROGRESS) 15f else 14f,
                            followDriver = trip?.status == TripStatus.IN_PROGRESS,
                        ),
                        pickup = trip?.pickup?.location,
                        dropoff = trip?.dropoff?.location,
                        driver = if (trip?.status != TripStatus.REQUESTED) driverLoc else null,
                        route = routePoints,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )

                // Top status banner shows next-action hint at all times
                trip?.let { t ->
                    StatusBanner(
                        trip = t,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }

        AddStopSheet(
            visible = showAddStop,
            tripId = trip?.id,
            onDismiss = { showAddStop = false },
            onAdded = { showAddStop = false },
        )
    }
}

// ============================================================
// Top status banner — gives the rider their current step at a
// glance, kept above the sheet so it's visible regardless of
// whether the user has the sheet collapsed or expanded.
// ============================================================
@Composable
private fun StatusBanner(trip: Trip, modifier: Modifier = Modifier) {
    val title = when (trip.status) {
        TripStatus.REQUESTED       -> "Finding your driver"
        TripStatus.ASSIGNED        -> "Driver found"
        TripStatus.ACCEPTED        -> "Driver is on the way"
        TripStatus.DRIVER_ARRIVING -> "Driver arriving soon"
        TripStatus.ARRIVED         -> "Driver has arrived"
        TripStatus.IN_PROGRESS     -> "On the way"
        TripStatus.COMPLETED       -> "Trip completed"
        TripStatus.CANCELLED       -> "Trip cancelled"
        else                       -> ""
    }
    val subtitle = when (trip.status) {
        TripStatus.REQUESTED       -> "Hang tight — we're matching you with a nearby driver"
        TripStatus.ACCEPTED        -> "Heading to ${trip.pickup.address}"
        TripStatus.DRIVER_ARRIVING -> "${trip.pickup.address}"
        TripStatus.ARRIVED         -> "Meet your driver outside"
        TripStatus.IN_PROGRESS     -> "Heading to ${trip.dropoff.address}"
        else                       -> ""
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        AnimatedContent(targetState = title, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "banner") {
            Text(it, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
        }
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.InkMuted)
        }
    }
}

// ============================================================
// Bottom sheet body — peek shows status + driver basics, expanded
// shows full card, stops list with reorder, fare, cancel.
// ============================================================
@Composable
private fun SheetBody(
    trip: Trip,
    onAddStop: () -> Unit,
    onMoveStopUp: (Int) -> Unit,
    onMoveStopDown: (Int) -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
    ) {
        // Driver section (always visible in peek)
        DriverRow(trip)
        Spacer(Modifier.height(16.dp))

        // Pickup row
        TripPointRow(
            iconColor = EtaxiColors.Power,
            address = trip.pickup.address,
            label = "Pickup",
        )

        // Stops with reorder buttons
        trip.stops.forEachIndexed { idx, stop ->
            Spacer(Modifier.height(8.dp))
            StopRow(
                index = idx,
                total = trip.stops.size,
                stop = stop,
                canReorder = trip.status == TripStatus.ACCEPTED ||
                    trip.status == TripStatus.IN_PROGRESS,
                onMoveUp = { onMoveStopUp(idx) },
                onMoveDown = { onMoveStopDown(idx) },
            )
        }

        Spacer(Modifier.height(8.dp))
        TripPointRow(
            iconColor = EtaxiColors.InkMuted,
            address = trip.dropoff.address,
            label = "Dropoff",
        )

        Spacer(Modifier.height(20.dp))

        // Fare row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Fare", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
                Text(
                    formatRand(trip.fareCents),
                    style = MaterialTheme.typography.headlineSmall,
                    color = EtaxiColors.Ink,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Trip", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
                Text(
                    "${trip.distanceKm} km · ${trip.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EtaxiColors.InkMuted,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Action buttons
        if (trip.status in listOf(TripStatus.ACCEPTED, TripStatus.IN_PROGRESS)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(EtaxiColors.SlidesRaised)
                        .clickable(onClick = onAddStop)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Add, null, tint = EtaxiColors.Ink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add stop", color = EtaxiColors.Ink)
                    }
                }
                if (trip.status == TripStatus.ACCEPTED) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(EtaxiColors.Danger.copy(alpha = 0.15f))
                            .clickable(onClick = onCancel)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Cancel", color = EtaxiColors.Danger)
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverRow(trip: Trip) {
    val driver = trip.driver
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(EtaxiColors.Power),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                driver?.name?.firstOrNull()?.uppercase()?.toString() ?: "?",
                color = EtaxiColors.InkOnPower,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                driver?.name ?: "Matching driver…",
                style = MaterialTheme.typography.titleMedium,
                color = EtaxiColors.Ink,
            )
            if (driver != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${driver.rating} · ${driver.vehicleModel} · ${driver.plateNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.InkMuted,
                    )
                }
            }
        }
        if (driver != null) {
            IconCircle(Icons.Rounded.Call, EtaxiColors.Power) { /* TODO call */ }
            Spacer(Modifier.width(8.dp))
            IconCircle(Icons.Rounded.Chat, EtaxiColors.Power) { /* TODO chat */ }
        }
    }
}

@Composable
private fun IconCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(EtaxiColors.SlidesRaised)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun TripPointRow(
    iconColor: Color,
    address: String,
    label: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Place, null, tint = iconColor, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
            Text(address, style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.Ink)
        }
    }
}

@Composable
private fun StopRow(
    index: Int,
    total: Int,
    stop: Place,
    canReorder: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(EtaxiColors.Power.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.Power,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Stop ${index + 1}", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
            Text(stop.address, style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.Ink)
        }
        if (canReorder && stop.id != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Rounded.ArrowDropUp, "Move up",
                    tint = if (index > 0) EtaxiColors.Ink else EtaxiColors.InkFaint,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = index > 0, onClick = onMoveUp),
                )
                Icon(
                    Icons.Rounded.ArrowDropDown, "Move down",
                    tint = if (index < total - 1) EtaxiColors.Ink else EtaxiColors.InkFaint,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(enabled = index < total - 1, onClick = onMoveDown),
                )
            }
        }
    }
}
