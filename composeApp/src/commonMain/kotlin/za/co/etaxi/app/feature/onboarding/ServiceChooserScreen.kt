package za.co.etaxi.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import za.co.etaxi.app.core.components.ServiceCard
import za.co.etaxi.app.core.navigation.HomeScreen
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Service chooser — the first screen a newly-registered rider sees.
 *
 * Modelled directly on reference screenshot 1:
 *   • Lime logo tile at top center
 *   • Big display headline across two lines
 *   • Explainer subtitle
 *   • Standard Ride card with lime accent + POPULAR badge
 *   • Custom Hire card with indigo accent + FLAT RATE badge
 *   • Ghost "Skip — use Standard for now" at the bottom
 *
 * Writes the chosen mode to session, then replaces stack with HomeScreen.
 * For now mode is stored locally in this screen; wire to SessionRepository
 * in a later patch.
 */
object ServiceChooserScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo tile
            Box(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .size(84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(EtaxiColors.Power),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "eT",
                    style = MaterialTheme.typography.displaySmall,
                    color = EtaxiColors.PowerInk,
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "How do you want\nto get around?",
                style = MaterialTheme.typography.displaySmall,
                color = EtaxiColors.Bone,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Choose your default service. You can always switch later.",
                style = MaterialTheme.typography.bodyMedium,
                color = EtaxiColors.BoneMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            ServiceCard(
                icon = Icons.Rounded.Bolt,
                title = "Standard Ride",
                subtitle = "On-demand metered trips",
                badge = "Popular",
                bullets = listOf(
                    "Pay per km + time",
                    "Driver arrives in minutes",
                    "Economy · Comfort · XL · Ladies",
                ),
                accent = EtaxiColors.Power,
                onClick = {
                    // TODO: persist mode to session
                    nav.replaceAll(HomeScreen)
                },
            )

            Spacer(Modifier.height(18.dp))

            ServiceCard(
                icon = Icons.Rounded.CalendarMonth,
                title = "Custom Hire",
                subtitle = "Flat-rate hourly & daily packages",
                badge = "Flat rate",
                bullets = listOf(
                    "Fixed price — no surprises",
                    "Hourly · Half day · Full day · Weekend",
                    "Perfect for long trips & events",
                ),
                accent = EtaxiColors.Hire,
                onClick = {
                    // TODO: persist mode to session
                    nav.replaceAll(HomeScreen)
                },
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Skip — use Standard for now",
                style = MaterialTheme.typography.bodyMedium,
                color = EtaxiColors.BoneFaint,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { nav.replaceAll(HomeScreen) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}
