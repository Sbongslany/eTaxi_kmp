package za.co.etaxi.app.feature.driver

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.feature.driver.earnings.DriverEarningsScreen
import za.co.etaxi.app.feature.driver.history.DriverHistoryScreen
import za.co.etaxi.app.feature.driver.home.DriverHomeScreen
import za.co.etaxi.app.feature.notifications.NotificationsScreen
import za.co.etaxi.app.feature.profile.ProfileScreen

private enum class DriverTab(val label: String, val icon: ImageVector) {
    Dashboard("Drive", Icons.Rounded.Dashboard),
    Earnings("Earnings", Icons.Rounded.Payments),
    History("Trips", Icons.Rounded.History),
    Inbox("Inbox", Icons.Rounded.Notifications),
    Profile("Profile", Icons.Rounded.Person),
}

object DriverShellScreen : Screen {
    @Composable
    override fun Content() {
        var tab by remember { mutableStateOf(DriverTab.Dashboard) }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    DriverTab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EtaxiColors.InkOnPower,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor   = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = EtaxiColors.InkMuted,
                                unselectedTextColor = EtaxiColors.InkMuted,
                            ),
                        )
                    }
                }
            },
        ) { inner ->
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                        slideInVertically { it / 12 }) togetherWith
                        (fadeOut() + slideOutVertically { -it / 12 })
                },
                modifier = Modifier.fillMaxSize().padding(inner),
                label = "driver-tabs",
            ) { selected ->
                when (selected) {
                    DriverTab.Dashboard -> DriverHomeScreen.Content()
                    DriverTab.Earnings  -> DriverEarningsScreen.Content()
                    DriverTab.History   -> DriverHistoryScreen.Content()
                    DriverTab.Inbox     -> NotificationsScreen.Content()
                    DriverTab.Profile   -> ProfileScreen.Content()
                }
            }
        }
    }
}
