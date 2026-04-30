package za.co.etaxi.app.feature.rider.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.components.MockMapCanvas
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapCamera
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatDuration
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.data.mock.MockData
import za.co.etaxi.app.domain.model.Place
import za.co.etaxi.app.domain.model.VehicleClass
import za.co.etaxi.app.domain.model.VehicleClassOption
import za.co.etaxi.app.domain.repository.RiderRepository
import za.co.etaxi.app.feature.notifications.NotificationsScreen
import za.co.etaxi.app.feature.profile.ProfileScreen
import za.co.etaxi.app.feature.rider.destinationSearch.DestinationPickerBus
import za.co.etaxi.app.feature.rider.destinationSearch.DestinationSearchScreen
import za.co.etaxi.app.feature.rider.history.RiderHistoryScreen
import za.co.etaxi.app.feature.rider.tripInProgress.RiderTripInProgressScreen

object RiderHomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val repo: RiderRepository = koinInject()
        val locationStreamer: za.co.etaxi.app.core.location.LocationStreamer = koinInject()
        val scope = remember { CoroutineScope(Dispatchers.Default) }

        // Pickup starts at the user's first saved place ("Home" / Sandton)
        // so the screen has a usable pickup even before GPS resolves.
        // If a fresh GPS fix arrives we upgrade pickup to the real
        // current location (provided the user hasn't manually picked
        // something else in the meantime).
        var pickup by remember { mutableStateOf<Place?>(MockData.savedPlaces.firstOrNull()) }
        var dropoff by remember { mutableStateOf<Place?>(null) }
        var options by remember { mutableStateOf<List<VehicleClassOption>>(emptyList()) }
        var selectedOption by remember { mutableStateOf<VehicleClassOption?>(null) }
        var requesting by remember { mutableStateOf(false) }
        var requestError by remember { mutableStateOf<String?>(null) }
        // Track whether the current pickup came from GPS so we don't
        // overwrite a manual pick if a late GPS fix shows up.
        var pickupFromGps by remember { mutableStateOf(false) }

        // One-shot GPS fix on mount. If it lands AND the user hasn't
        // already picked a destination or chosen a different pickup,
        // upgrade pickup to "Current location". If GPS fails we keep
        // the saved-place default so the screen still works.
        LaunchedEffect(Unit) {
            val fix = runCatching { locationStreamer.lastKnownLocation() }.getOrNull()
            if (fix != null && (pickupFromGps || pickup?.label == "Home")) {
                pickup = Place(
                    label = "Current location",
                    address = "My location",
                    location = fix,
                )
                pickupFromGps = true
            }
        }

        // If the rider has an active trip already (e.g. they backgrounded
        // the app mid-ride and came back), jump straight into the trip
        // screen so they don't accidentally request a second one.
        val activeTrip by repo.activeTrip().collectAsState(initial = null)
        LaunchedEffect(activeTrip?.id) {
            val t = activeTrip ?: return@LaunchedEffect
            if (t.status != za.co.etaxi.app.domain.model.TripStatus.COMPLETED &&
                t.status != za.co.etaxi.app.domain.model.TripStatus.CANCELLED
            ) {
                navigator.push(RiderTripInProgressScreen(t.id))
            }
        }

        // Re-estimate whenever pickup/dropoff change
        LaunchedEffect(pickup, dropoff) {
            val p = pickup; val d = dropoff
            options = if (p != null && d != null) repo.estimate(p, d) else emptyList()
            selectedOption = options.firstOrNull()
        }

        Scaffold(
            containerColor = EtaxiColors.SlidesDeep,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
            ) {
                // Map fills the top ~55% of screen
                EtaxiMap(
                    state = MapState(
                        camera = pickup?.location?.let {
                            MapCamera(center = it, zoom = 14f)
                        } ?: defaultJoburgCamera(),
                        pickup = pickup?.location,
                        dropoff = dropoff?.location,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )

                // Top row: avatar + greeting
                TopGreeting(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    onAvatarClick = { navigator.push(ProfileScreen) },
                )

                // Bottom sheet content
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = EtaxiColors.Slides,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    tonalElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Grab handle
                        Box(
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier
                                    .size(44.dp, 4.dp)
                                    .background(EtaxiColors.SlidesSoft, RoundedCornerShape(2.dp)),
                            )
                        }

                        Text(
                            text = "Where to, ${MockData.riderUser.firstName}?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = EtaxiColors.Ink,
                        )
                        Spacer(Modifier.height(12.dp))

                        // Pickup / dropoff inline inputs
                        LocationInput(
                            icon = Icons.Rounded.MyLocation,
                            label = pickup?.label ?: "Pickup",
                            value = pickup?.address ?: "Tap to set pickup",
                            filled = pickup != null,
                        ) {
                            DestinationPickerBus.await { picked -> pickup = picked }
                            navigator.push(DestinationSearchScreen(title = "Set pickup"))
                        }

                        Spacer(Modifier.height(8.dp))

                        LocationInput(
                            icon = Icons.Rounded.LocationOn,
                            label = dropoff?.label ?: "Destination",
                            value = dropoff?.address ?: "Where are you going?",
                            filled = dropoff != null,
                            accent = true,
                        ) {
                            DestinationPickerBus.await { picked -> dropoff = picked }
                            navigator.push(DestinationSearchScreen(title = "Where to?"))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Saved places quick chips
                        Text(
                            "Saved places",
                            style = MaterialTheme.typography.labelMedium,
                            color = EtaxiColors.InkMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(MockData.savedPlaces) { place ->
                                PlaceChip(place, onClick = { dropoff = place })
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Vehicle class picker (only when dropoff set)
                        AnimatedContent(
                            targetState = options.isNotEmpty(),
                            transitionSpec = {
                                (fadeIn(tween(250)) + slideInVertically { 60 })
                                    .togetherWith(fadeOut(tween(150)))
                            },
                            label = "options",
                        ) { hasOptions ->
                            if (hasOptions) {
                                Column {
                                    Text(
                                        "Choose your ride",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EtaxiColors.InkMuted,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    options.forEach { opt ->
                                        VehicleOptionRow(
                                            option = opt,
                                            selected = selectedOption == opt,
                                            onClick = { selectedOption = opt },
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Spacer(Modifier.height(8.dp))
                                    requestError?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EtaxiColors.Danger,
                                            modifier = Modifier.padding(bottom = 8.dp),
                                        )
                                    }
                                    EtaxiPrimaryButton(
                                        text = if (requesting) "Requesting..." else "REQUEST RIDE",
                                        enabled = selectedOption != null && !requesting,
                                        onClick = {
                                            val p = pickup ?: return@EtaxiPrimaryButton
                                            val d = dropoff ?: return@EtaxiPrimaryButton
                                            val o = selectedOption ?: return@EtaxiPrimaryButton
                                            scope.launch {
                                                requesting = true
                                                requestError = null
                                                try {
                                                    repo.requestTrip(p, d, o)
                                                    // Don't push manually - the LaunchedEffect
                                                    // above watches activeTrip and pushes
                                                    // RiderTripInProgressScreen as soon as
                                                    // _activeTrip flips non-null. Pushing here
                                                    // too caused a double-push race that
                                                    // crashed the new screen on mount.
                                                } catch (t: Throwable) {
                                                    requestError = t.message
                                                        ?: "Could not request ride. Try again."
                                                } finally {
                                                    requesting = false
                                                }
                                            }
                                        },
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(0.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopGreeting(modifier: Modifier = Modifier, onAvatarClick: () -> Unit) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = EtaxiColors.Slides.copy(alpha = 0.85f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onAvatarClick),
            border = androidx.compose.foundation.BorderStroke(1.dp, EtaxiColors.SlidesEdge),
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.size(32.dp).background(EtaxiColors.Power, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        MockData.riderUser.initials,
                        color = EtaxiColors.InkOnPower,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
                Column {
                    Text("Hi, ${MockData.riderUser.firstName}",
                        style = MaterialTheme.typography.labelLarge, color = EtaxiColors.Ink)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(10.dp))
                        Text(
                            "${MockData.riderUser.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EtaxiColors.InkMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationInput(
    icon: ImageVector,
    label: String,
    value: String,
    filled: Boolean,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = EtaxiColors.SlidesRaised,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (accent && !filled) EtaxiColors.Power else EtaxiColors.SlidesEdge,
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(32.dp).background(
                    if (accent) EtaxiColors.Power.copy(alpha = 0.15f) else EtaxiColors.Slides,
                    CircleShape,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = if (accent) EtaxiColors.Power else EtaxiColors.Ink, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkMuted)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (filled) EtaxiColors.Ink else EtaxiColors.InkFaint,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PlaceChip(place: Place, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        color = EtaxiColors.SlidesRaised,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, EtaxiColors.SlidesEdge),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = when (place.label) {
                    "Home" -> Icons.Rounded.Home
                    "Work" -> Icons.Rounded.Work
                    "Airport" -> Icons.Rounded.Flight
                    else -> Icons.Rounded.Place
                },
                contentDescription = null,
                tint = EtaxiColors.Power,
                modifier = Modifier.size(16.dp),
            )
            Text(place.label, style = MaterialTheme.typography.labelMedium, color = EtaxiColors.Ink)
        }
    }
}

@Composable
private fun VehicleOptionRow(
    option: VehicleClassOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) EtaxiColors.Power else EtaxiColors.SlidesEdge
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (selected) EtaxiColors.Slides else EtaxiColors.SlidesRaised,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, border),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier.size(48.dp).background(
                    if (selected) EtaxiColors.Power.copy(alpha = 0.15f) else EtaxiColors.Slides,
                    RoundedCornerShape(12.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when (option.vehicleClass) {
                        VehicleClass.STANDARD -> Icons.Rounded.DirectionsCar
                        VehicleClass.XL -> Icons.Rounded.AirportShuttle
                        VehicleClass.LUX -> Icons.Rounded.Star
                    },
                    null,
                    tint = EtaxiColors.Power,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    option.vehicleClass.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = EtaxiColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${option.vehicleClass.maxSeats} seats · ${option.vehicleClass.eta} away",
                    style = MaterialTheme.typography.bodySmall,
                    color = EtaxiColors.InkMuted,
                )
            }
            Text(
                formatRand(option.priceCents),
                style = MaterialTheme.typography.titleMedium,
                color = EtaxiColors.Ink,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// LazyRow DSL helper
private fun <T> androidx.compose.foundation.lazy.LazyListScope.items(
    items: List<T>,
    content: @Composable (T) -> Unit,
) = items(items.size) { content(items[it]) }

