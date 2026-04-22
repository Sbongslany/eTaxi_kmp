package za.co.etaxi.app.feature.rider.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Search
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
import org.koin.compose.koinInject
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapCamera
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.rider.destinationSearch.DestinationSearchScreen
import za.co.etaxi.app.feature.rider.hire.CustomHireScreen
import za.co.etaxi.app.feature.rider.profile.ProfileScreen

/**
 * Rider home — reference screenshot 3.
 *
 * Top:   eTaxi wordmark + avatar.
 * Below: pickup address pill (tappable to change pickup).
 * Map:   fullscreen behind, shows user position + recent nearby drivers.
 * Float: "📅 Hire" chip in indigo — shortcut to Custom Hire flow.
 * Sheet: Good evening greeting, "Where to?" big title, Search destination bar,
 *        horizontal chips (Home / Work / Airport / Shopping).
 */
object RiderHomeScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val user by session.currentUser.collectAsState(initial = null)
        val name = "there"
        val initials = "X"

        Box(Modifier.fillMaxSize().background(EtaxiColors.Ink900)) {
            EtaxiMap(
                state = MapState(camera = defaultJoburgCamera()),
                modifier = Modifier.fillMaxSize(),
            )

            // Top chrome: wordmark + avatar + address
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "eTaxi",
                        style = MaterialTheme.typography.displaySmall,
                        color = EtaxiColors.Power,
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EtaxiColors.Power)
                            .clickable { nav.push(ProfileScreen) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = EtaxiColors.PowerInk,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Pickup address pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(EtaxiColors.Ink900.copy(alpha = 0.92f))
                        .border(1.dp, EtaxiColors.Ink500.copy(alpha = 0.6f), CircleShape)
                        .clickable { nav.push(DestinationSearchScreen) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(10.dp).clip(CircleShape).background(EtaxiColors.Power),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("\uD83D\uDCCD", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "29 Pear Street, Protea Glen",
                        style = MaterialTheme.typography.titleMedium,
                        color = EtaxiColors.Bone,
                        maxLines = 1,
                    )
                }
            }

            // Floating "Hire" chip (Custom Hire shortcut)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp, bottom = 320.dp)
                    .clip(CircleShape)
                    .background(EtaxiColors.Hire)
                    .clickable { nav.push(CustomHireScreen) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = EtaxiColors.Bone,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Hire", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
            }

            // Bottom sheet: greeting + search + quick chips
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(EtaxiColors.Ink900)
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 44.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.Ink500),
                )
                Spacer(Modifier.height(14.dp))

                Text(
                    "Good evening, $name \uD83D\uDC4B",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EtaxiColors.BoneMuted,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Where to?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EtaxiColors.Bone,
                )

                Spacer(Modifier.height(16.dp))

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(EtaxiColors.Ink700)
                        .clickable { nav.push(DestinationSearchScreen) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        tint = EtaxiColors.Power,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Search destination…",
                        style = MaterialTheme.typography.titleMedium,
                        color = EtaxiColors.BoneFaint,
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Quick destination chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuickChip(emoji = "\uD83C\uDFE0", label = "Home") {
                        nav.push(DestinationSearchScreen)
                    }
                    QuickChip(emoji = "\uD83D\uDCBC", label = "Work") {
                        nav.push(DestinationSearchScreen)
                    }
                    QuickChip(emoji = "\u2708\uFE0F", label = "Airport") {
                        nav.push(DestinationSearchScreen)
                    }
                    QuickChip(emoji = "\uD83D\uDED2", label = "Shopping") {
                        nav.push(DestinationSearchScreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickChip(emoji: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(EtaxiColors.Ink700)
            .border(1.dp, EtaxiColors.Ink500.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
    }
}
