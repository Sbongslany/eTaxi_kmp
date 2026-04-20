package za.co.etaxi.app.feature.driver.activeTrip

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import za.co.etaxi.app.core.components.MockMapCanvas
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.domain.model.LatLng
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.feature.driver.rate.RateRiderScreen

class DriverActiveTripScreen(private val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val scope = rememberCoroutineScope()
        val trip by repo.activeTrip().collectAsState(initial = null)

        // Simulate pushing location to backend every 3s while in progress
        LaunchedEffect(trip?.status, trip?.id) {
            if (trip?.status == TripStatus.IN_PROGRESS) {
                val t = trip ?: return@LaunchedEffect
                var step = 0
                while (trip?.status == TripStatus.IN_PROGRESS) {
                    val progress = (step.coerceAtMost(30)) / 30.0
                    val lat = t.pickup.location.lat +
                        (t.dropoff.location.lat - t.pickup.location.lat) * progress
                    val lng = t.pickup.location.lng +
                        (t.dropoff.location.lng - t.pickup.location.lng) * progress
                    repo.pushLocation(t.id, LatLng(lat, lng))
                    delay(3_000)
                    step++
                }
            }
        }

        // On completion, navigate to rate-rider screen
        LaunchedEffect(trip?.status) {
            if (trip?.status == TripStatus.COMPLETED) {
                val t = trip ?: return@LaunchedEffect
                delay(800)
                nav.replace(
                    RateRiderScreen(
                        tripId = t.id,
                        riderName = "Xolani N.",
                        fareLabel = formatRand((t.fareCents * 0.8).toInt()),
                    ),
                )
            } else if (trip == null) {
                nav.pop()
            }
        }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            MockMapCanvas(
                modifier = Modifier.fillMaxSize(),
                showPickup = trip != null,
                showDropoff = trip != null,
                showDriver = true,
                animateDriver = trip?.status == TripStatus.IN_PROGRESS ||
                    trip?.status == TripStatus.DRIVER_ARRIVING,
            )

            trip?.let { t ->
                // Rider card
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
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
                            Text("X", color = EtaxiColors.InkOnPower, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Rider", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
                            Text("Xolani N.", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("4.91", style = MaterialTheme.typography.bodySmall, color = EtaxiColors.InkMuted)
                            }
                        }
                        Text(formatRand(t.fareCents), style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Power)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t.pickup.label, color = EtaxiColors.Ink, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ArrowOutward, null, tint = EtaxiColors.InkMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(t.dropoff.label, color = EtaxiColors.InkMuted, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // Action sheet
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                ) {
                    AnimatedContent(
                        targetState = t.status,
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
                        "${t.distanceKm} km • ${t.durationMinutes} min · you earn ${formatRand((t.fareCents * 0.8).toInt())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EtaxiColors.InkMuted,
                    )
                    Spacer(Modifier.height(20.dp))

                    val (label, action) = when (t.status) {
                        TripStatus.ACCEPTED        -> "I'm on my way" to { scope.launch { repo.markArriving(t.id) } }
                        TripStatus.DRIVER_ARRIVING -> "I've arrived"   to { scope.launch { repo.markArrived(t.id) } }
                        TripStatus.ARRIVED         -> "Start trip"      to { scope.launch { repo.startTrip(t.id) } }
                        TripStatus.IN_PROGRESS     -> "End trip"        to { scope.launch { repo.endTrip(t.id) } }
                        else                       -> "Close"           to { nav.pop() }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(EtaxiColors.Power)
                            .clickable { action() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, style = MaterialTheme.typography.titleLarge, color = EtaxiColors.InkOnPower)
                    }
                }
            }
        }
    }
}
