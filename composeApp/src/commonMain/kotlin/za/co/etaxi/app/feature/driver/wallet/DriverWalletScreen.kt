package za.co.etaxi.app.feature.driver.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.EtaxiPrimaryButton
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.core.util.formatRand
import za.co.etaxi.app.domain.model.WalletTransaction
import za.co.etaxi.app.domain.model.WalletTxReason
import za.co.etaxi.app.domain.model.WalletTxType
import za.co.etaxi.app.domain.repository.DriverRepository

object DriverWalletScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val repo: DriverRepository = koinInject()

        var balance by remember { mutableStateOf(0) }
        var transactions by remember { mutableStateOf<List<WalletTransaction>>(emptyList()) }

        LaunchedEffect(Unit) {
            balance = repo.earningsSummary().walletCents
            transactions = repo.walletTransactions()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { nav.pop() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.ArrowBack, "back", tint = EtaxiColors.Ink)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Wallet", style = MaterialTheme.typography.headlineLarge, color = EtaxiColors.Ink)
                }

                Spacer(Modifier.height(24.dp))

                // Balance card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(EtaxiColors.Power)
                        .padding(24.dp),
                ) {
                    Text(
                        "AVAILABLE BALANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = EtaxiColors.InkOnPower.copy(alpha = 0.75f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        formatRand(balance),
                        style = MaterialTheme.typography.displayLarge,
                        color = EtaxiColors.InkOnPower,
                    )
                    Spacer(Modifier.height(16.dp))
                    EtaxiPrimaryButton(
                        text = "Cash out",
                        onClick = { /* stub */ },
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = EtaxiColors.InkFaint,
                )
                Spacer(Modifier.height(8.dp))
            }

            items(transactions) { tx ->
                TransactionRow(tx)
                Spacer(Modifier.height(8.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Payouts settle to your bank in 1–2 business days. Driver keeps 80% of the fare; the rest covers platform fees and SARS VAT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EtaxiColors.InkFaint,
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    val icon: ImageVector = when (tx.reason) {
        WalletTxReason.TRIP_EARNING -> Icons.Rounded.DirectionsCar
        WalletTxReason.PAYOUT       -> Icons.Rounded.AccountBalance
        WalletTxReason.BONUS        -> Icons.Rounded.AutoAwesome
        WalletTxReason.ADJUSTMENT   -> Icons.Rounded.ArrowUpward
    }
    val tint = when (tx.type) {
        WalletTxType.CREDIT -> EtaxiColors.Success
        WalletTxType.DEBIT  -> EtaxiColors.InkMuted
    }
    val amountText = (if (tx.type == WalletTxType.CREDIT) "+" else "−") + formatRand(tx.amountCents.toInt())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                tx.description,
                style = MaterialTheme.typography.titleMedium,
                color = EtaxiColors.Ink,
                maxLines = 2,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                reasonLabel(tx.reason),
                style = MaterialTheme.typography.labelSmall,
                color = EtaxiColors.InkMuted,
            )
        }
        Text(
            amountText,
            style = MaterialTheme.typography.titleMedium,
            color = tint,
        )
    }
}

private fun reasonLabel(r: WalletTxReason): String = when (r) {
    WalletTxReason.TRIP_EARNING -> "Trip earning"
    WalletTxReason.PAYOUT       -> "Payout"
    WalletTxReason.BONUS        -> "Bonus"
    WalletTxReason.ADJUSTMENT   -> "Adjustment"
}
