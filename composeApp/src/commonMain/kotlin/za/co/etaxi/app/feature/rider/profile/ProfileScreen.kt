package za.co.etaxi.app.feature.rider.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContactSupport
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Shield
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
import za.co.etaxi.app.core.components.TintedIconTile
import za.co.etaxi.app.core.theme.EtaxiColors

/**
 * Profile — reference screenshot 9.
 * Three sections: ACCOUNT, SAFETY, PREFERENCES.
 * Each row: tinted icon tile + title + subtitle + chevron. Optional
 * NEW badge (Promo Codes).
 * Sign Out is its own destructive row at the bottom.
 */
object ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EtaxiColors.Ink900)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 120.dp),
        ) {
            SectionHeader("ACCOUNT")
            RowItem(
                icon = Icons.Rounded.Person,
                tint = EtaxiColors.Info,
                title = "Personal Information",
                subtitle = "mike@gmail.com",
                onClick = { /* TODO */ },
            )
            RowItem(
                icon = Icons.Rounded.Notifications,
                tint = EtaxiColors.Amber,
                title = "Notifications",
                subtitle = "Manage alerts and push notifications",
                onClick = { /* TODO */ },
            )
            RowItem(
                icon = Icons.Rounded.CreditCard,
                tint = EtaxiColors.Power,
                title = "Payment Methods",
                subtitle = "Card",
                onClick = { /* TODO */ },
            )
            RowItem(
                icon = Icons.Rounded.Redeem,
                tint = EtaxiColors.Plum,
                title = "Promo Codes",
                subtitle = "Redeem discount codes",
                badge = "New",
                badgeColor = EtaxiColors.Power,
                onClick = { /* TODO */ },
            )

            Spacer(Modifier.height(28.dp))
            SectionHeader("SAFETY")
            RowItem(
                icon = Icons.Rounded.Shield,
                tint = EtaxiColors.Ruby,
                title = "Safety Settings",
                subtitle = "Emergency contacts, panic button",
                onClick = { /* TODO */ },
            )
            RowItem(
                icon = Icons.Rounded.Group,
                tint = EtaxiColors.Info,
                title = "Emergency Contacts",
                subtitle = "0 contacts added",
                onClick = { /* TODO */ },
            )

            Spacer(Modifier.height(28.dp))
            SectionHeader("PREFERENCES")
            RowItem(
                icon = Icons.Rounded.Language,
                tint = EtaxiColors.BoneMuted,
                title = "Language",
                subtitle = "English",
                onClick = { /* TODO */ },
            )
            RowItem(
                icon = Icons.Rounded.ContactSupport,
                tint = EtaxiColors.BoneMuted,
                title = "Help & Support",
                subtitle = "FAQs, contact us, report an issue",
                onClick = { /* TODO */ },
            )
            RowItem(
                icon = Icons.Rounded.Description,
                tint = EtaxiColors.BoneMuted,
                title = "Terms & Privacy",
                subtitle = "Read our policies",
                onClick = { /* TODO */ },
            )

            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(EtaxiColors.Ink500.copy(alpha = 0.5f)))
            Spacer(Modifier.height(8.dp))

            // Sign out
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { /* TODO: session.logout() */ }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TintedIconTile(icon = Icons.Rounded.Logout, tint = EtaxiColors.Flare)
                Spacer(Modifier.width(14.dp))
                Text("Sign Out", style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Flare)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = EtaxiColors.BoneFaint,
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun RowItem(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = EtaxiColors.Power,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TintedIconTile(icon = icon, tint = tint)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Bone)
                badge?.let {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(it.uppercase(), style = MaterialTheme.typography.labelSmall, color = badgeColor)
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.BoneMuted)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = EtaxiColors.BoneFaint)
    }
    Spacer(Modifier.height(6.dp))
}
