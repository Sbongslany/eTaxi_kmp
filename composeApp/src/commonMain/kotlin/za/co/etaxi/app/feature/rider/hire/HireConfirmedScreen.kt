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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import za.co.etaxi.app.core.components.HeroStatusPill
import za.co.etaxi.app.core.components.TintedIconTile
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Hire confirmed — reference screenshots 7 & 14.
 * Giant countdown display, detail stack card, finding-your-driver
 * status, "what to expect" numbered steps, cancel booking at bottom.
 */
object HireConfirmedScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 24.dp),
        ) {
            HeroStatusPill(
                label = "Hire Confirmed",
                accent = EtaxiColors.Hire,
                showPulsingDot = true,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(36.dp))

            // Giant countdown
            Text(
                "1d 3h 58m",
                style = MaterialTheme.typography.displayLarge,
                color = EtaxiColors.Bone,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "until pickup",
                style = MaterialTheme.typography.bodyLarge,
                color = EtaxiColors.BoneMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(36.dp))

            // Details stack
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(EtaxiColors.Ink700)
                    .padding(16.dp),
            ) {
                DetailRow(
                    icon = Icons.Rounded.CalendarToday,
                    tint = EtaxiColors.Hire,
                    label = "Start",
                    value = "Fri, 27 Mar · 03:33",
                )
                DetailDivider()
                DetailRow(
                    icon = Icons.Rounded.Schedule,
                    tint = EtaxiColors.Amber,
                    label = "Duration",
                    value = "1 hr",
                )
                DetailDivider()
                DetailRow(
                    icon = Icons.Rounded.Navigation,
                    tint = EtaxiColors.Power,
                    label = "Pickup",
                    value = "29 Pear Street",
                )
                DetailDivider()
                DetailRow(
                    icon = Icons.Rounded.PersonPin,
                    tint = EtaxiColors.Hire,
                    label = "Destination",
                    value = "Isambane Street",
                )
                DetailDivider()
                DetailRow(
                    icon = Icons.Rounded.Paid,
                    tint = EtaxiColors.Amber,
                    label = "Total Fare",
                    value = "R155",
                )
            }

            Spacer(Modifier.height(18.dp))

            // Finding your driver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(EtaxiColors.HireGlow)
                    .border(1.dp, EtaxiColors.Hire.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Sync, null, tint = EtaxiColors.Hire, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Finding your driver", style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
                    Text(
                        "A driver will be assigned before your pickup time",
                        style = MaterialTheme.typography.bodySmall,
                        color = EtaxiColors.BoneMuted,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // What to expect
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(EtaxiColors.Ink700)
                    .padding(18.dp),
            ) {
                Text(
                    "WHAT TO EXPECT",
                    style = MaterialTheme.typography.labelMedium,
                    color = EtaxiColors.BoneFaint,
                )
                Spacer(Modifier.height(14.dp))
                StepRow("1", "You'll be notified when your driver is on the way")
                Spacer(Modifier.height(14.dp))
                StepRow("2", "Driver arrives at your pickup location")
                Spacer(Modifier.height(14.dp))
                StepRow("3", "Confirm with the driver to start your hire")
            }

            Spacer(Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, EtaxiColors.Flare.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { nav.pop() }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cancel Booking", style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Flare)
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TintedIconTile(icon = icon, tint = tint, size = 40.dp, cornerRadius = 11.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = EtaxiColors.BoneFaint)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Bone)
        }
    }
}

@Composable
private fun DetailDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(EtaxiColors.Ink500.copy(alpha = 0.4f)))
}

@Composable
private fun StepRow(num: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(EtaxiColors.HireGlow),
            contentAlignment = Alignment.Center,
        ) {
            Text(num, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Hire)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = EtaxiColors.Bone,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}
