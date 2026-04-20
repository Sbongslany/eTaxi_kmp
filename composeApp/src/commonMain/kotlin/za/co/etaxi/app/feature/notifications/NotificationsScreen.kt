package za.co.etaxi.app.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import za.co.etaxi.app.core.theme.EtaxiColors
import za.co.etaxi.app.domain.model.AppNotification
import za.co.etaxi.app.domain.model.NotificationKind
import za.co.etaxi.app.domain.repository.NotificationRepository

object NotificationsScreen : Screen {
    @Composable
    override fun Content() {
        val repo: NotificationRepository = koinInject()
        val scope = rememberCoroutineScope()
        val items by repo.all().collectAsState(initial = emptyList())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Inbox", style = MaterialTheme.typography.displayMedium, color = EtaxiColors.Ink)
                        Spacer(Modifier.height(6.dp))
                        Text("Updates, promos, and trip receipts.", style = MaterialTheme.typography.bodyLarge, color = EtaxiColors.InkMuted)
                    }
                    if (items.any { !it.read }) {
                        Text(
                            "Mark all read",
                            style = MaterialTheme.typography.labelMedium,
                            color = EtaxiColors.Power,
                            modifier = Modifier.clickable { scope.launch { repo.markAllRead() } },
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
            items(items) { n ->
                NotificationRow(n) { scope.launch { repo.markRead(n.id) } }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, onTap: () -> Unit) {
    val (icon, tint) = when (n.kind) {
        NotificationKind.TRIP    -> Icons.Rounded.DirectionsCar to EtaxiColors.Power
        NotificationKind.PAYMENT -> Icons.Rounded.CreditCard    to EtaxiColors.Success
        NotificationKind.PROMO   -> Icons.Rounded.Campaign      to EtaxiColors.Warning
        NotificationKind.SYSTEM  -> Icons.Rounded.Info          to EtaxiColors.InkMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, EtaxiColors.SlidesEdge, RoundedCornerShape(16.dp))
            .clickable(onClick = onTap)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(n.title, style = MaterialTheme.typography.titleMedium, color = EtaxiColors.Ink)
            Spacer(Modifier.height(4.dp))
            Text(n.body, style = MaterialTheme.typography.bodySmall, color = EtaxiColors.InkMuted)
        }
        if (!n.read) {
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(8.dp).clip(CircleShape).background(EtaxiColors.Power))
        }
    }
}
