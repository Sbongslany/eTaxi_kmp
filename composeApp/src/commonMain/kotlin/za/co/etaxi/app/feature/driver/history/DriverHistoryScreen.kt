package za.co.etaxi.app.feature.driver.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import za.co.etaxi.app.domain.repository.DriverRepository

object DriverHistoryScreen : Screen {
    @Composable
    override fun Content() {
        val repo: DriverRepository = koinInject()
        val scope = rememberCoroutineScope()
        var trips by remember { mutableStateOf<List<Trip>>(emptyList()) }

        LaunchedEffect(Unit) { scope.launch { trips = repo.history() } }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp),
        ) {
            item {
                Text("Trips", style = MaterialTheme.typography.displayMedium, color = EtaxiColors.Ink)
                Spacer(Modifier.height(6.dp))
                Text("Your recent driving history.", style = MaterialTheme.typography.bodyLarge, color = EtaxiColors.InkMuted)
                Spacer(Modifier.height(24.dp))
            }
            items(trips) { t ->
                DriverTripCard(t)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DriverTripCard(trip: Trip) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ArrowOutward, null, tint = EtaxiColors.InkMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(trip.dropoff.label, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.InkMuted)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "${trip.distanceKm} km • ${trip.durationMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkFaint,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatRand(trip.fareCents), style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Power)
            Spacer(Modifier.height(2.dp))
            Text("+${formatRand((trip.fareCents * 0.8).toInt())} earnings", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
        }
    }
}
