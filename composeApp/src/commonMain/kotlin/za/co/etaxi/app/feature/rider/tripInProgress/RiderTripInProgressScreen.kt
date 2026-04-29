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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.MockMapCanvas
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapCamera
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.data.mock.MockRiderRepository
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.repository.RiderRepository
import za.co.etaxi.app.feature.rider.rate.RateDriverScreen

class RiderTripInProgressScreen(private val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: RiderRepository = koinInject()
        val scope = rememberCoroutineScope()
        val trip by repo.activeTrip().collectAsState(initial = null)
        val driverLoc by repo.driverLocation().collectAsState(initial = null)

        var showAddStop by remember { mutableStateOf(false) }
        var celebrationDismissed by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Build the route polyline: pickup -> stops -> dropoff
            val routePoints = remember(trip?.id, trip?.stops?.size) {
                val t = trip
                if (t == null) emptyList()
                else buildList {
                    add(t.pickup.location)
                    t.stops.forEach { add(it.location) }
                    add(t.dropoff.location)
                }
            }

            // Real map. Camera follows driver during IN_PROGRESS, otherwise
            // stays centered on the pickup.
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

            // Status banner (top)
            trip?.let { t ->
                StatusBanner(
                    trip = t,
                    onCancel = {
                        scope.launch { repo.cancelTrip(t.id); nav.pop() }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp),
                )
            }

            // Driver sheet (bottom) — only shown once accepted
            trip?.let { t ->
                if (t.status == TripStatus.REQUESTED) {
                    SearchingSheet(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onAdvance = {
                            scope.launch { (repo as? MockRiderRepository)?.advanceTripStatus() }
                        },
                    )
                } else if (t.driver != null && t.status != TripStatus.COMPLETED) {
                    DriverSheet(
                        trip = t,
                        onAddStop = { showAddStop = true },
                        onAdvance = {
                            scope.launch { (repo as? MockRiderRepository)?.advanceTripStatus() }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            // Add-stop sheet overlay
            AddStopSheet(
                visible = showAddStop,
                tripId = trip?.id,
                onDismiss = { showAddStop = false },
                onAdded = { showAddStop = false },
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            // Trip complete celebration overlay
            trip?.let { t ->
                if (t.status == TripStatus.COMPLETED && !celebrationDismissed) {
                    TripCompleteCelebration(
                        visible = true,
                        fareCents = t.fareCents,
                        distanceKm = t.distanceKm,
                        durationMinutes = t.durationMinutes,
                        onRate = {
                            celebrationDismissed = true
                            nav.replace(
                                RateDriverScreen.from(
                                    tripId = t.id,
                                    driver = t.driver,
                                    fareLabel = formatRand(t.fareCents),
                                ),
                            )
                        },
                        onDismiss = {
                            celebrationDismissed = true
                            nav.pop()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(
    trip: Trip,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (trip.status) {
        TripStatus.REQUESTED       -> "Finding your driver…"
        TripStatus.ACCEPTED        -> "Driver is on the way"
        TripStatus.DRIVER_ARRIVING -> "Driver arriving now"
        TripStatus.ARRIVED         -> "Your driver has arrived"
        TripStatus.IN_PROGRESS     -> "On your way"
        TripStatus.COMPLETED       -> "Trip complete"
        else                       -> "—"
    }
    val showCancel = trip.status in listOf(
        TripStatus.REQUESTED, TripStatus.ACCEPTED, TripStatus.DRIVER_ARRIVING,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = label,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status",
            modifier = Modifier.weight(1f),
        ) {
            Text(it, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
        }
        if (showCancel) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.DangerDim)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, "cancel", tint = EtaxiColors.Danger)
            }
        }
    }
}

@Composable
private fun DriverSheet(
    trip: Trip,
    onAddStop: () -> Unit,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val driver = trip.driver ?: return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 48.dp, height = 4.dp)
                .clip(CircleShape)
                .background(EtaxiColors.SlidesSoft),
        )
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.Power),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    driver.name.split(" ").map { it.firstOrNull() ?: ' ' }.joinToString("").take(2),
                    style = MaterialTheme.typography.titleLarge,
                    color = EtaxiColors.InkOnPower,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(driver.name, style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Ink)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        driver.rating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.InkMuted,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "• ${driver.vehicleModel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.InkMuted,
                    )
                }
            }
            ActionBubble(Icons.Rounded.Chat) {}
            Spacer(Modifier.width(8.dp))
            ActionBubble(Icons.Rounded.Call) {}
        }

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(EtaxiColors.SlidesRaised)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                driver.plateNumber,
                style = MaterialTheme.typography.labelLarge,
                color = EtaxiColors.Ink,
            )
        }

        Spacer(Modifier.height(20.dp))
        HairlineDivider()
        Spacer(Modifier.height(16.dp))

        Row {
            StatColumn("ETA", if (trip.status == TripStatus.IN_PROGRESS) "${trip.durationMinutes} min" else "3 min")
            Spacer(Modifier.width(24.dp))
            StatColumn("Distance", "${trip.distanceKm} km")
            Spacer(Modifier.width(24.dp))
            StatColumn("Fare", formatRand(trip.fareCents))
        }

        // Add-stop chip (only during IN_PROGRESS or DRIVER_ARRIVING)
        if (trip.status in listOf(TripStatus.IN_PROGRESS, TripStatus.DRIVER_ARRIVING, TripStatus.ARRIVED)) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, EtaxiColors.Power, RoundedCornerShape(12.dp))
                    .clickable(onClick = onAddStop)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Add, null, tint = EtaxiColors.Power, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add a stop",
                    style = MaterialTheme.typography.labelLarge,
                    color = EtaxiColors.Power,
                )
            }
            if (trip.stops.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${trip.stops.size} additional ${if (trip.stops.size == 1) "stop" else "stops"} · +R${trip.stops.size * 15}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EtaxiColors.InkMuted,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Dev: advance status button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EtaxiColors.PowerGlow)
                .clickable(onClick = onAdvance)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Advance status (mock)",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.Power,
            )
        }
    }
}

@Composable
private fun SearchingSheet(modifier: Modifier = Modifier, onAdvance: () -> Unit) {
    val inf = rememberInfiniteTransition(label = "searching")
    val pulse by inf.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "p",
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(EtaxiColors.PowerGlow)
                .alpha(pulse),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(EtaxiColors.Power))
        }
        Spacer(Modifier.height(16.dp))
        Text("Searching nearby drivers", style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Ink)
        Spacer(Modifier.height(6.dp))
        Text("Hang tight, this usually takes ~30s", style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.InkMuted)

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(EtaxiColors.PowerGlow)
                .clickable(onClick = onAdvance)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Simulate driver accepted (mock)",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.Power,
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
    }
}

@Composable
private fun ActionBubble(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(EtaxiColors.SlidesRaised)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = EtaxiColors.Ink)
    }
}

@Composable
private fun HairlineDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(EtaxiColors.SlidesEdge),
    )
}
