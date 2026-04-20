package za.co.etaxi.app.feature.rider.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.domain.model.Trip
import za.co.etaxi.app.domain.model.TripStatus
import za.co.etaxi.app.domain.repository.RiderRepository

object RiderHistoryScreen : Screen {
    @Composable
    override fun Content() {
        val repo: RiderRepository = koinInject()
        val scope = rememberCoroutineScope()
        var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }

        LaunchedEffect(Unit) {
            scope.launch { trips = repo.history() }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp,
            ),
        ) {
            item {
                Text(
                    "Your rides",
                    style = MaterialTheme.typography.displayMedium,
                    color = EtaxiColors.Ink,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Past trips and receipts.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EtaxiColors.InkMuted,
                )
                Spacer(Modifier.height(24.dp))
            }
            items(trips) { t ->
                TripRow(t)
                Spacer(Modifier.height(12.dp))
            }
            if (trips.isEmpty()) {
                item { EmptyHistory() }
            }
        }
    }
}

@Composable
private fun TripRow(trip: Trip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(18.dp))
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PlayArrow, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(trip.pickup.label, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.padding(start = 6.dp).width(2.dp).height(16.dp).background(EtaxiColors.SlidesEdge))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ArrowOutward, null, tint = EtaxiColors.InkMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(trip.dropoff.label, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.InkMuted)
            }
            Spacer(Modifier.height(10.dp))
            Row {
                StatusTag(trip.status)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${trip.distanceKm} km • ${trip.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = EtaxiColors.InkFaint,
                )
            }
        }
        Text(
            if (trip.status == TripStatus.CANCELLED) "—" else formatRand(trip.fareCents),
            style = MaterialTheme.typography.titleLarge,
            color = EtaxiColors.Power,
        )
    }
}

@Composable
private fun StatusTag(status: TripStatus) {
    val (label, color) = when (status) {
        TripStatus.COMPLETED -> "Completed" to EtaxiColors.Success
        TripStatus.CANCELLED -> "Cancelled" to EtaxiColors.Danger
        else -> status.name to EtaxiColors.Power
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun EmptyHistory() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("No rides yet", style = MaterialTheme.typography.headlineMedium, color = EtaxiColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text("Your past rides will show here.", style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.InkMuted)
    }
}
