package za.co.etaxi.app.feature.driver.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.MockMapCanvas
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapCamera
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.data.mock.MockData
import za.co.etaxi.app.domain.model.DriverIncomingRequest
import za.co.etaxi.app.domain.repository.DriverOnboardingState
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.feature.driver.activeTrip.DriverActiveTripScreen

object DriverHomeScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val locationStreamer: za.co.etaxi.app.core.location.LocationStreamer = koinInject()
        val scope = rememberCoroutineScope()
        val online by repo.isOnline.collectAsState(initial = false)
        val incoming by repo.incomingRequests().collectAsState(initial = null)
        val active by repo.activeTrip().collectAsState(initial = null)
        val onboarding by repo.onboardingState.collectAsState(
            initial = DriverOnboardingState.APPROVED,
        )
        val canGoOnline = onboarding == DriverOnboardingState.APPROVED

        var myLocation by remember {
            mutableStateOf<za.co.etaxi.app.domain.model.LatLng?>(null)
        }
        // One-shot GPS read so the map opens centred on the driver's
        // actual position. If denied or unavailable, falls back to the
        // default Joburg camera.
        LaunchedEffect(Unit) {
            myLocation = runCatching { locationStreamer.lastKnownLocation() }.getOrNull()
        }

        // Push to active trip screen once accepted
        LaunchedEffect(active?.id) {
            if (active != null) nav.push(DriverActiveTripScreen(active!!.id))
        }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            EtaxiMap(
                state = MapState(
                    camera = myLocation
                        ?.let { MapCamera(center = it, zoom = 15f) }
                        ?: defaultJoburgCamera(),
                    driver = myLocation,
                ),
                modifier = Modifier.fillMaxSize(),
            )

            // Top info card
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (online) "You're online" else "You're offline",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (online) EtaxiColors.Power else EtaxiColors.Ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (online) "Waiting for ride requests" else "Go online to start earning",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtaxiColors.InkMuted,
                        )
                    }
                    OnlineDot(online)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("Today", formatRand(68_450), Modifier.weight(1f))
                    MiniStat("Trips", "9", Modifier.weight(1f))
                    MiniStat("Rating", "4.96★", Modifier.weight(1f))
                }
            }

            // Online toggle at the bottom (signature action)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
            ) {
                if (canGoOnline) {
                    OnlineToggleButton(
                        online = online,
                        onToggle = { scope.launch { repo.setOnline(!online) } },
                    )
                } else {
                    OnboardingGateChip(state = onboarding)
                }
            }

            // Incoming request modal
            AnimatedVisibility(
                visible = incoming != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                incoming?.let { req ->
                    IncomingRequestSheet(
                        request = req,
                        onAccept = { scope.launch { repo.acceptRequest(req.tripId) } },
                        onDecline = { scope.launch { repo.declineRequest(req.tripId) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineDot(online: Boolean) {
    val inf = rememberInfiniteTransition(label = "dot")
    val pulse by inf.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "p",
    )
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(if (online) EtaxiColors.Power else EtaxiColors.SlidesSoft)
            .alpha(if (online) pulse else 1f),
    )
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp),
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
    }
}

@Composable
private fun OnlineToggleButton(online: Boolean, onToggle: () -> Unit) {
    val scale by animateFloatAsState(if (online) 1f else 0.97f, label = "s")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(if (online) EtaxiColors.Danger else EtaxiColors.Power)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (online) "Go offline" else "Go online",
            style = MaterialTheme.typography.titleLarge,
            color = EtaxiColors.InkOnPower,
        )
    }
}

/**
 * Shown instead of the online toggle while the driver is still being
 * reviewed or hasn't finished their onboarding. Non-clickable, just
 * informational — onboarding itself is reached via the redirect in
 * HomeScreen when a fresh driver verifies OTP, or via the setup tab
 * (future work) for returning drivers.
 */
@Composable
private fun OnboardingGateChip(state: DriverOnboardingState) {
    val (title, subtitle, tint) = when (state) {
        DriverOnboardingState.NOT_STARTED     -> Triple(
            "Finish your onboarding",
            "Register your vehicle and documents to start earning",
            EtaxiColors.Power,
        )
        DriverOnboardingState.IN_PROGRESS     -> Triple(
            "Onboarding in progress",
            "Finish uploading your required documents",
            EtaxiColors.Power,
        )
        DriverOnboardingState.AWAITING_REVIEW -> Triple(
            "Under review",
            "We'll notify you within 24-48 hours",
            EtaxiColors.Warning,
        )
        DriverOnboardingState.REJECTED        -> Triple(
            "Action required",
            "One or more documents need attention",
            EtaxiColors.Danger,
        )
        DriverOnboardingState.APPROVED        -> Triple(
            "Approved",
            "You're good to go",
            EtaxiColors.Success,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = tint)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = EtaxiColors.InkMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun IncomingRequestSheet(
    request: DriverIncomingRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    var remaining by remember(request.tripId) { mutableStateOf(request.expiresAtSeconds) }
    LaunchedEffect(request.tripId) {
        while (remaining > 0) { delay(1000); remaining-- }
        if (remaining == 0) onDecline()
    }
    val progress = remaining / request.expiresAtSeconds.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(EtaxiColors.SlidesEdge),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(EtaxiColors.Power),
            )
        }
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("New ride request", style = MaterialTheme.typography.bodySmall, color = EtaxiColors.Power)
                Spacer(Modifier.height(4.dp))
                Text(request.riderName, style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Ink)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${request.riderRating} • ${request.pickupDistanceKm} km away",
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.InkMuted,
                    )
                }
            }
            Text(
                formatRand(request.fareCents),
                style = MaterialTheme.typography.displaySmall,
                color = EtaxiColors.Power,
            )
        }
        Spacer(Modifier.height(16.dp))

        // Pickup
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.PlayArrow, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(request.pickup.label, color = EtaxiColors.Ink, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text(request.pickup.address, color = EtaxiColors.InkMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 22.dp))
        Spacer(Modifier.height(10.dp))
        // Dropoff
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ArrowOutward, null, tint = EtaxiColors.InkMuted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(request.dropoff.label, color = EtaxiColors.Ink, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text(request.dropoff.address, color = EtaxiColors.InkMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 22.dp))

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(18.dp))
                    .clickable(onClick = onDecline),
                contentAlignment = Alignment.Center,
            ) {
                Text("Decline", style = MaterialTheme.typography.labelLarge, color = EtaxiColors.Ink)
            }
            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EtaxiColors.Power)
                    .clickable(onClick = onAccept),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Accept • ${remaining}s",
                    style = MaterialTheme.typography.labelLarge,
                    color = EtaxiColors.InkOnPower,
                )
            }
        }

        // Fine-print
        Spacer(Modifier.height(12.dp))
        Text(
            "${request.distanceKm} km • ${request.durationMinutes} min trip",
            style = MaterialTheme.typography.bodySmall,
            color = EtaxiColors.InkFaint,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
