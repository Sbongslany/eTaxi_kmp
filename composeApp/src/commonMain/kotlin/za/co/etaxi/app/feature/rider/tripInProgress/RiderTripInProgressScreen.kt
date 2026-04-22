package za.co.etaxi.app.feature.rider.tripInProgress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import za.co.etaxi.app.core.components.HeroStatusPill
import za.co.etaxi.app.core.map.EtaxiMap
import za.co.etaxi.app.core.map.MapState
import za.co.etaxi.app.core.map.defaultJoburgCamera
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Trip in progress — finding driver state.
 * Reference screenshot 11.
 *
 * Map fills background. Bottom sheet has:
 *   • Green "Finding driver…" status pill with live countdown
 *   • Progress bar track
 *   • "Connecting you with a driver…" status line
 *   • Stat card: ARRIVING IN + FARE
 *   • Three action buttons: Call | Chat | Share
 *   • Full-width hollow red Cancel Ride
 */
data class RiderTripInProgressScreen(val tripId: String) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        Box(Modifier.fillMaxSize().background(EtaxiColors.Ink900)) {
            EtaxiMap(
                state = MapState(camera = defaultJoburgCamera()),
                modifier = Modifier.fillMaxSize(),
            )

            // Bottom sheet content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(EtaxiColors.Ink900)
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
            ) {
                // Top status row: big pill with timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(EtaxiColors.PowerGlow)
                        .border(1.dp, EtaxiColors.Power.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(EtaxiColors.Power))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Finding driver…",
                        style = MaterialTheme.typography.titleMedium,
                        color = EtaxiColors.Power,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "59s",
                        style = MaterialTheme.typography.titleMedium,
                        color = EtaxiColors.BoneMuted,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Thin progress stripe (static 30% mock)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.Ink700),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.3f)
                            .clip(CircleShape)
                            .background(EtaxiColors.Power),
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(EtaxiColors.PowerGlow),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Connecting you with a driver…",
                        style = MaterialTheme.typography.titleMedium,
                        color = EtaxiColors.Bone,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Stat card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EtaxiColors.Ink700)
                        .padding(18.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "ARRIVING IN",
                            style = MaterialTheme.typography.labelMedium,
                            color = EtaxiColors.BoneFaint,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "75",
                                style = MaterialTheme.typography.displaySmall,
                                color = EtaxiColors.Power,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "min",
                                style = MaterialTheme.typography.titleMedium,
                                color = EtaxiColors.BoneMuted,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "FARE",
                            style = MaterialTheme.typography.labelMedium,
                            color = EtaxiColors.BoneFaint,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "R453",
                            style = MaterialTheme.typography.displaySmall,
                            color = EtaxiColors.Bone,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionBtn(
                        icon = Icons.Rounded.Call,
                        tint = EtaxiColors.Bone,
                        label = "Call",
                        modifier = Modifier.weight(1f),
                    )
                    ActionBtn(
                        icon = Icons.Rounded.ChatBubble,
                        tint = EtaxiColors.Bone,
                        label = "Chat",
                        modifier = Modifier.weight(1f),
                    )
                    ActionBtn(
                        icon = Icons.Rounded.Share,
                        tint = EtaxiColors.Power,
                        label = "Share",
                        labelColor = EtaxiColors.Power,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Cancel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, EtaxiColors.Flare.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .clickable { nav.pop() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cancel Ride", style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Flare)
                }
            }
        }
    }
}

@Composable
private fun ActionBtn(
    icon: ImageVector,
    tint: Color,
    label: String,
    labelColor: Color = EtaxiColors.Bone,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(EtaxiColors.Ink700)
            .clickable { /* TODO */ }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = labelColor)
    }
}
