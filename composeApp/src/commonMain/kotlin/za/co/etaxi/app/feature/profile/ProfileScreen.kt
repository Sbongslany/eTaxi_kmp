package za.co.etaxi.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.components.ConfirmDialog
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.UserRole
import za.co.etaxi.app.domain.repository.SessionRepository
import za.co.etaxi.app.feature.auth.roleSelection.RoleSelectionScreen
import za.co.etaxi.app.feature.rider.payment.PaymentMethodsScreen

object ProfileScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val session: SessionRepository = koinInject()
        val scope = rememberCoroutineScope()
        val user by session.currentUser.collectAsState(initial = null)
        val role by session.selectedRole.collectAsState(initial = null)

        var showLogoutConfirm by remember { mutableStateOf(false) }
        var showSwitchConfirm by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 24.dp),
        ) {
            Text("Profile", style = MaterialTheme.typography.displayMedium, color = EtaxiColors.Ink)

            Spacer(Modifier.height(24.dp))

            // User card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EtaxiColors.Power),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(user?.initials ?: "—", style = MaterialTheme.typography.headlineLarge, color = EtaxiColors.InkOnPower)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(user?.displayName ?: "Guest", style = MaterialTheme.typography.titleLarge, color = EtaxiColors.Ink)
                    Spacer(Modifier.height(4.dp))
                    Text(user?.phone ?: "", style = MaterialTheme.typography.bodySmall, color = EtaxiColors.InkMuted)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = EtaxiColors.Power, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${user?.rating ?: 5.0} • ${user?.totalTrips ?: 0} trips",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtaxiColors.InkMuted,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SettingRow(
                Icons.Rounded.CreditCard, "Payment methods",
                onClick = { nav.push(PaymentMethodsScreen) },
            )
            SettingRow(Icons.Rounded.Notifications, "Notifications")
            SettingRow(Icons.Rounded.Security,   "Security")
            SettingRow(Icons.Rounded.Language,   "Language", value = "English")
            SettingRow(Icons.Rounded.HelpOutline,"Help & support")

            Spacer(Modifier.height(16.dp))

            SettingRow(
                Icons.Rounded.SwapHoriz,
                if (role == UserRole.DRIVER) "Switch to rider mode" else "Switch to driver mode",
                tint = EtaxiColors.Power,
                onClick = { showSwitchConfirm = true },
            )

            Spacer(Modifier.height(8.dp))

            SettingRow(
                Icons.Rounded.Logout,
                "Log out",
                tint = EtaxiColors.Danger,
                onClick = { showLogoutConfirm = true },
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "eTaxi v0.1.0 — Johannesburg",
                style = MaterialTheme.typography.bodySmall,
                color = EtaxiColors.InkFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        ConfirmDialog(
            visible = showLogoutConfirm,
            title = "Log out?",
            message = "You'll need to sign in again to book or accept rides.",
            confirmLabel = "Log out",
            confirmTone = EtaxiColors.Danger,
            onDismiss = { showLogoutConfirm = false },
            onConfirm = {
                showLogoutConfirm = false
                scope.launch {
                    session.logout()
                    nav.replaceAll(RoleSelectionScreen)
                }
            },
        )

        ConfirmDialog(
            visible = showSwitchConfirm,
            title = if (role == UserRole.DRIVER) "Switch to rider mode?" else "Switch to driver mode?",
            message = "You'll be signed out and taken back to the role selection screen.",
            confirmLabel = "Switch",
            confirmTone = EtaxiColors.Power,
            onDismiss = { showSwitchConfirm = false },
            onConfirm = {
                showSwitchConfirm = false
                scope.launch {
                    session.logout()
                    nav.replaceAll(RoleSelectionScreen)
                }
            },
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    tint: androidx.compose.ui.graphics.Color = EtaxiColors.Ink,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = tint, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = EtaxiColors.InkMuted)
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = EtaxiColors.InkFaint, modifier = Modifier.size(18.dp))
    }
}
