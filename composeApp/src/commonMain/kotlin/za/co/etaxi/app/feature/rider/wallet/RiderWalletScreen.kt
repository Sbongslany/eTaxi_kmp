package za.co.etaxi.app.feature.rider.wallet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
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
import za.co.etaxi.app.core.components.TintedIconTile
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Wallet — reference screenshot 8.
 * Headline "Wallet"; Card/Cash toggle below PAYMENT METHOD label;
 * detail card below showing the active payment method with 3 bullet
 * features marked by green check-in-circle icons.
 */
object RiderWalletScreen : Screen {
    @Composable
    override fun Content() {
        var mode by remember { mutableStateOf(PayMode.CARD) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(64.dp))

            Text("Wallet", style = MaterialTheme.typography.displaySmall, color = EtaxiColors.Bone)
            Spacer(Modifier.height(6.dp))
            Text(
                "Choose your default payment method",
                style = MaterialTheme.typography.bodyMedium,
                color = EtaxiColors.BoneMuted,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "PAYMENT METHOD",
                style = MaterialTheme.typography.labelMedium,
                color = EtaxiColors.BoneFaint,
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(EtaxiColors.Ink700)
                    .padding(4.dp),
            ) {
                PayModeSeg(
                    label = "Card",
                    icon = Icons.Rounded.CreditCard,
                    selected = mode == PayMode.CARD,
                    onClick = { mode = PayMode.CARD },
                    modifier = Modifier.weight(1f),
                )
                PayModeSeg(
                    label = "Cash",
                    icon = Icons.Rounded.Payments,
                    selected = mode == PayMode.CASH,
                    onClick = { mode = PayMode.CASH },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Detail card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(EtaxiColors.Ink700)
                    .padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    TintedIconTile(
                        icon = if (mode == PayMode.CARD) Icons.Rounded.CreditCard else Icons.Rounded.Payments,
                        tint = EtaxiColors.Power,
                        size = 52.dp,
                        cornerRadius = 14.dp,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (mode == PayMode.CARD) "Card Payment" else "Cash Payment",
                            style = MaterialTheme.typography.titleLarge,
                            color = EtaxiColors.Bone,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (mode == PayMode.CARD)
                                "Pay securely by card. Full card management coming soon."
                            else
                                "Pay the driver in cash at the end of your trip.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EtaxiColors.BoneMuted,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(EtaxiColors.Ink500.copy(alpha = 0.5f)))
                Spacer(Modifier.height(14.dp))

                val bullets = if (mode == PayMode.CARD) listOf(
                    "Secure online payment",
                    "No cash needed",
                    "Instant payment on trip completion",
                ) else listOf(
                    "Pay at the end of the trip",
                    "No cards needed",
                    "Exact change is appreciated",
                )
                bullets.forEach { bullet ->
                    BulletRow(bullet)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private enum class PayMode { CARD, CASH }

@Composable
private fun PayModeSeg(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        if (selected) EtaxiColors.Power else Color.Transparent,
        label = "segbg",
    )
    val fg by animateColorAsState(
        if (selected) EtaxiColors.PowerInk else EtaxiColors.BoneMuted,
        label = "segfg",
    )
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(interaction, indication = null, onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = fg)
    }
}

@Composable
private fun BulletRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(EtaxiColors.PowerGlow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, null, tint = EtaxiColors.Power, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = EtaxiColors.BoneMuted)
    }
}
