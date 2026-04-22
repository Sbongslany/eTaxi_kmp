package za.co.etaxi.app.feature.rider.destinationSearch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Destination search — reference screenshot 4.
 * Half-sheet over the map. "Plan your trip" headline, bordered stack with
 * green pickup row + yellow destination input, POPULAR DESTINATIONS list
 * with emoji tile rows.
 */
object DestinationSearchScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        var query by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900),
        ) {
            // Top map bleed area with back button
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(EtaxiColors.Ink800),
            ) {
                Box(
                    Modifier
                        .padding(top = 48.dp, start = 20.dp)
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EtaxiColors.Ink700)
                        .clickable { nav.pop() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = EtaxiColors.Bone)
                }
            }

            // Sheet container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(EtaxiColors.Ink900)
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.Ink500),
                )
                Spacer(Modifier.height(18.dp))

                Text(
                    "Plan your trip",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EtaxiColors.Bone,
                )

                Spacer(Modifier.height(20.dp))

                // Trip stack card with amber border
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(EtaxiColors.Ink800)
                        .border(1.5.dp, EtaxiColors.Amber.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(16.dp),
                ) {
                    // Pickup row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(EtaxiColors.Power))
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "29 Pear Street, Protea Glen",
                            style = MaterialTheme.typography.titleMedium,
                            color = EtaxiColors.Bone,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EtaxiColors.Ink700)
                                .clickable { /* swap pickup/dropoff */ },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.SwapVert, null, tint = EtaxiColors.BoneMuted)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(EtaxiColors.Ink500.copy(alpha = 0.5f)))
                    Spacer(Modifier.height(14.dp))

                    // Destination input
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(EtaxiColors.Amber),
                        )
                        Spacer(Modifier.width(14.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            cursorBrush = SolidColor(EtaxiColors.Power),
                            textStyle = MaterialTheme.typography.titleMedium.copy(color = EtaxiColors.Bone),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (query.isEmpty()) {
                                    Text(
                                        "Search destination…",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = EtaxiColors.BoneFaint,
                                    )
                                }
                                inner()
                            },
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Text(
                    "POPULAR DESTINATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.BoneFaint,
                )

                Spacer(Modifier.height(10.dp))

                val destinations = listOf(
                    Destination("\u2708\uFE0F", "OR Tambo Airport", "OR Tambo International Airport, JHB"),
                    Destination("\uD83D\uDED2", "Sandton City", "Sandton City Mall, Sandton"),
                    Destination("\uD83C\uDFD9\uFE0F", "Waterfall City", "Waterfall City, Midrand"),
                    Destination("\uD83D\uDECD\uFE0F", "Mall of Africa", "Mall of Africa, Midrand"),
                    Destination("\u2615", "Rosebank", "Rosebank Mall, Rosebank"),
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(destinations) { dest ->
                        DestinationRow(dest) {
                            // TODO: set destination + pop back with result
                            nav.pop()
                        }
                    }
                }
            }
        }
    }
}

private data class Destination(
    val emoji: String,
    val name: String,
    val address: String,
)

@Composable
private fun DestinationRow(dest: Destination, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EtaxiColors.Ink700),
            contentAlignment = Alignment.Center,
        ) {
            Text(dest.emoji, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(dest.name, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
            Spacer(Modifier.height(2.dp))
            Text(dest.address, style = MaterialTheme.typography.bodySmall, color = EtaxiColors.BoneMuted)
        }
    }
}
