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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import za.co.etaxi.app.core.components.ServiceMode
import za.co.etaxi.app.core.components.ServiceModeToggle
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * My Trips — reference screenshots 5 & 6.
 * Standard / Custom Hire toggle; filter chips (All/Completed/Cancelled);
 * Upcoming Hire card when Custom Hire selected; trip cards with timeline dots.
 */
object HistoryScreen : Screen {
    @Composable
    override fun Content() {
        var mode by remember { mutableStateOf(ServiceMode.STANDARD) }
        var filter by remember { mutableStateOf(Filter.ALL) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(64.dp))

            Text(
                "My Trips",
                style = MaterialTheme.typography.displaySmall,
                color = EtaxiColors.Bone,
            )

            Spacer(Modifier.height(20.dp))

            ServiceModeToggle(mode = mode, onChange = { mode = it })

            if (mode == ServiceMode.HIRE) {
                Spacer(Modifier.height(16.dp))
                UpcomingHireCard()
            }

            Spacer(Modifier.height(16.dp))

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip("All", filter == Filter.ALL, mode) { filter = Filter.ALL }
                FilterChip("Completed", filter == Filter.COMPLETED, mode) { filter = Filter.COMPLETED }
                FilterChip("Cancelled", filter == Filter.CANCELLED, mode) { filter = Filter.CANCELLED }
            }

            Spacer(Modifier.height(20.dp))

            // Trip list (or empty state)
            val sample = sampleTrips(mode, filter)
            if (sample.isEmpty()) {
                EmptyState(mode)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sample) { trip -> TripCard(trip) }
                    item { Spacer(Modifier.height(100.dp)) } // breathing room above tab bar
                }
            }
        }
    }
}

private enum class Filter { ALL, COMPLETED, CANCELLED }

@Composable
private fun FilterChip(label: String, selected: Boolean, mode: ServiceMode, onClick: () -> Unit) {
    val accent = if (mode == ServiceMode.STANDARD) EtaxiColors.Power else EtaxiColors.Hire
    val bg = if (selected) accent else Color.Transparent
    val border = if (selected) accent else EtaxiColors.Ink500
    val fg = if (selected) (if (mode == ServiceMode.STANDARD) EtaxiColors.PowerInk else EtaxiColors.Bone) else EtaxiColors.BoneMuted
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = fg)
    }
}

@Composable
private fun UpcomingHireCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EtaxiColors.Ink700)
            .border(1.dp, EtaxiColors.Hire.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable { /* TODO: go to hire details */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(EtaxiColors.HireGlow),
            contentAlignment = Alignment.Center,
        ) {
            Text("\uD83D\uDCC5", style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Upcoming Hire", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Hire)
            Spacer(Modifier.height(2.dp))
            Text("Wed 25 Mar · 20:33", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
            Spacer(Modifier.height(2.dp))
            Text("8 hr", style = MaterialTheme.typography.bodySmall, color = EtaxiColors.BoneMuted)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = EtaxiColors.BoneMuted)
    }
}

private data class SampleTrip(
    val category: String,
    val status: String,
    val statusColor: Color,
    val amount: String?,
    val pickup: String,
    val dropoff: String,
    val rating: Int,
    val distance: String?,
)

private fun sampleTrips(mode: ServiceMode, filter: Filter): List<SampleTrip> {
    val all = listOf(
        SampleTrip("Economy", "Completed", EtaxiColors.BoneMuted, "R1 805",
            "29 Pear Street", "29 South Africa Drive", 0, "36.7 km · 63 min"),
        SampleTrip("Economy", "Cancelled", EtaxiColors.Flare, null,
            "29 Pear Street", "Isambane Street", 0, null),
        SampleTrip("Comfort", "Completed", EtaxiColors.BoneMuted, "R287",
            "Sandton City", "OR Tambo Airport", 5, "28.4 km · 41 min"),
    )
    return when (filter) {
        Filter.ALL -> all
        Filter.COMPLETED -> all.filter { it.status == "Completed" }
        Filter.CANCELLED -> all.filter { it.status == "Cancelled" }
    }
}

@Composable
private fun TripCard(trip: SampleTrip) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(EtaxiColors.Ink700)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(trip.category, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
                Text("—", style = MaterialTheme.typography.bodySmall, color = EtaxiColors.BoneFaint)
            }
            Column(horizontalAlignment = Alignment.End) {
                trip.amount?.let {
                    Text(it, style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Bone)
                }
                Text(trip.status, style = MaterialTheme.typography.bodySmall, color = trip.statusColor)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.width(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(EtaxiColors.Power))
                Box(Modifier.width(1.dp).height(18.dp).background(EtaxiColors.Ink500))
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(EtaxiColors.Amber))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(trip.pickup, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
                Spacer(Modifier.height(14.dp))
                Text(trip.dropoff, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
            }
        }

        trip.distance?.let {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        Icons.Rounded.Star,
                        null,
                        tint = if (i < trip.rating) EtaxiColors.Power else EtaxiColors.Ink500,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = EtaxiColors.BoneMuted)
            }
        }
    }
}

@Composable
private fun EmptyState(mode: ServiceMode) {
    val accent = if (mode == ServiceMode.STANDARD) EtaxiColors.Power else EtaxiColors.Hire
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Schedule, null, tint = accent, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("No trips yet", style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Bone)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your trip history will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = EtaxiColors.BoneMuted,
            textAlign = TextAlign.Center,
        )
    }
}
