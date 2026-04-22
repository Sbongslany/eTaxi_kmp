package za.co.etaxi.app.feature.rider.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Custom Hire form — reference screenshots 12 & 13.
 * Map peeks at top; sheet with pickup/destination stack,
 * start date & time, duration picker (emoji chips), vehicle card,
 * confirm hire CTA.
 */
object CustomHireScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        var duration by remember { mutableStateOf(Duration.HOURLY) }
        var pickup by remember { mutableStateOf("") }
        var destination by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .verticalScroll(rememberScrollState()),
        ) {
            // Map peek area with header
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(EtaxiColors.Ink800),
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(EtaxiColors.Ink700)
                            .clickable { nav.pop() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = EtaxiColors.Bone)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Custom Hire", style = MaterialTheme.typography.headlineLarge, color = EtaxiColors.Bone)
                        Text(
                            "Flat-rate packages",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtaxiColors.BoneMuted,
                        )
                    }
                    // Tiny "Standard" switch chip (top right in screenshot)
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EtaxiColors.PowerGlow)
                            .border(1.dp, EtaxiColors.Power.copy(alpha = 0.4f), CircleShape)
                            .clickable { nav.pop() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Bolt, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Standard", style = MaterialTheme.typography.titleSmall, color = EtaxiColors.Power)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(EtaxiColors.Ink900)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.Ink500),
                )
                Spacer(Modifier.height(20.dp))

                // Pickup + Destination stack card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(EtaxiColors.Ink700)
                        .padding(16.dp),
                ) {
                    LocationRow(
                        dotColor = EtaxiColors.Power,
                        value = pickup,
                        placeholder = "Pickup location",
                        onChange = { pickup = it },
                        onClear = { pickup = "" },
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(EtaxiColors.Ink500.copy(alpha = 0.5f)))
                    Spacer(Modifier.height(10.dp))
                    LocationRow(
                        dotColor = EtaxiColors.Hire,
                        value = destination,
                        placeholder = "Destination (optional)",
                        onChange = { destination = it },
                        onClear = { destination = "" },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Start Date & Time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(EtaxiColors.Ink700)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarToday, null, tint = EtaxiColors.BoneMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Start Date & Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = EtaxiColors.Bone,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Pill(text = "26 Mar 2026", onClick = { /* TODO date picker */ })
                        Pill(text = "01:33", onClick = { /* TODO time picker */ })
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Duration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(EtaxiColors.Ink700)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, null, tint = EtaxiColors.BoneMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Duration",
                            style = MaterialTheme.typography.titleMedium,
                            color = EtaxiColors.Bone,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DurationTile("\u23F0", "Hourly", duration == Duration.HOURLY) { duration = Duration.HOURLY }
                        DurationTile("\uD83C\uDF24\uFE0F", "Half Day", duration == Duration.HALF_DAY) { duration = Duration.HALF_DAY }
                        DurationTile("\u2600\uFE0F", "Full Day", duration == Duration.FULL_DAY) { duration = Duration.FULL_DAY }
                        DurationTile("\uD83C\uDFD6\uFE0F", "Weekend", duration == Duration.WEEKEND) { duration = Duration.WEEKEND }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Vehicle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(EtaxiColors.Ink700)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DirectionsCar, null, tint = EtaxiColors.BoneMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Vehicle", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(EtaxiColors.Ink600)
                            .border(1.5.dp, EtaxiColors.Hire.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("\uD83D\uDE97", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Sedan (4 pax)", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
                            Text(
                                "3 passengers",
                                style = MaterialTheme.typography.bodySmall,
                                color = EtaxiColors.BoneMuted,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "R150",
                                style = MaterialTheme.typography.titleLarge,
                                color = EtaxiColors.Hire,
                            )
                            Text(
                                "flat",
                                style = MaterialTheme.typography.bodySmall,
                                color = EtaxiColors.BoneMuted,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Box(
                            Modifier.size(22.dp).clip(CircleShape).background(EtaxiColors.Hire),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EtaxiColors.Bone),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Confirm CTA (indigo for Custom Hire)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(EtaxiColors.Hire)
                        .clickable { /* TODO: confirm hire */ nav.push(HireConfirmedScreen) }
                        .padding(vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        null,
                        tint = EtaxiColors.Bone,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Confirm Hire · R155",
                        style = MaterialTheme.typography.titleLarge,
                        color = EtaxiColors.Bone,
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

private enum class Duration { HOURLY, HALF_DAY, FULL_DAY, WEEKEND }

@Composable
private fun LocationRow(
    dotColor: Color,
    value: String,
    placeholder: String,
    onChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(12.dp))
        Text(
            value.ifEmpty { placeholder },
            style = MaterialTheme.typography.titleMedium,
            color = if (value.isEmpty()) EtaxiColors.BoneFaint else EtaxiColors.Bone,
            modifier = Modifier.weight(1f).clickable { /* TODO open search */ },
        )
        if (value.isNotEmpty()) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.Ink600)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Clear, null, tint = EtaxiColors.BoneMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(EtaxiColors.Ink600)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
    }
}

@Composable
private fun DurationTile(emoji: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) EtaxiColors.Hire else EtaxiColors.Ink600)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) EtaxiColors.Bone else EtaxiColors.BoneMuted,
        )
    }
}
