package za.co.etaxi.app.feature.driver.earnings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.domain.repository.DriverEarnings
import za.co.etaxi.app.domain.repository.DriverRepository
import za.co.etaxi.app.feature.driver.documents.DriverDocumentsScreen
import za.co.etaxi.app.feature.driver.vehicles.DriverVehiclesScreen
import za.co.etaxi.app.feature.driver.wallet.DriverWalletScreen

object DriverEarningsScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()
        val scope = rememberCoroutineScope()
        var summary by remember { mutableStateOf<DriverEarnings?>(null) }

        LaunchedEffect(Unit) { scope.launch { summary = repo.earningsSummary() } }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 24.dp),
        ) {
            Text("Earnings", style = MaterialTheme.typography.displayMedium, color = EtaxiColors.Ink)

            Spacer(Modifier.height(24.dp))

            // Big wallet card — tappable to open full wallet screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(EtaxiColors.Power)
                    .clickable { nav.push(DriverWalletScreen) }
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Wallet balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = EtaxiColors.InkOnPower.copy(alpha = 0.7f),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            summary?.let { formatRand(it.walletCents) } ?: "—",
                            style = MaterialTheme.typography.displayLarge,
                            color = EtaxiColors.InkOnPower,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Tap to see activity & cash out",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtaxiColors.InkOnPower.copy(alpha = 0.7f),
                        )
                    }
                    Icon(Icons.Rounded.ArrowForward, null, tint = EtaxiColors.InkOnPower)
                }
            }

            Spacer(Modifier.height(20.dp))

            summary?.let { s ->
                PeriodCard("Today",     s.todayCents, s.todayTrips)
                Spacer(Modifier.height(12.dp))
                PeriodCard("This week", s.weekCents,  s.weekTrips)
                Spacer(Modifier.height(12.dp))
                PeriodCard("This month",s.monthCents, s.monthTrips)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "MY DRIVING",
                style = MaterialTheme.typography.labelSmall,
                color = EtaxiColors.InkFaint,
            )
            Spacer(Modifier.height(8.dp))
            ShortcutRow("Vehicles", "Manage cars & set active", onClick = { nav.push(DriverVehiclesScreen) })
            Spacer(Modifier.height(10.dp))
            ShortcutRow("Documents", "Licence, PrDP, insurance & more", onClick = { nav.push(DriverDocumentsScreen) })

            Spacer(Modifier.height(24.dp))

            Text(
                "Fares are credited to your wallet once a trip is paid. 80% driver share. Payouts weekly.",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkFaint,
            )
        }
    }
}

@Composable
private fun ShortcutRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = EtaxiColors.InkMuted)
        }
        Icon(Icons.Rounded.ArrowForward, null, tint = EtaxiColors.InkMuted)
    }
}

@Composable
private fun PeriodCard(label: String, cents: Int, trips: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(18.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = EtaxiColors.InkMuted)
            Spacer(Modifier.height(6.dp))
            Text(formatRand(cents), style = MaterialTheme.typography.headlineLarge, color = EtaxiColors.Ink)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(trips.toString(), style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Power)
            Text("trips", style = MaterialTheme.typography.labelSmall, color = EtaxiColors.InkFaint)
        }
    }
}
